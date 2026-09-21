package com.mediq.appointment.service;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.request.CreateReplacementProposalRequest;
import com.mediq.appointment.dto.request.RescheduleProposalRequest;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentHistory;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.entity.ReplacementProposal;
import com.mediq.appointment.entity.ReplacementProposalStatus;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.ReplacementProposalMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.repository.ReplacementProposalRepository;
import com.mediq.appointment.validation.ReplacementProposalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplacementProposalServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private ReplacementProposalRepository replacementProposalRepository;

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private com.mediq.appointment.event.EventPublisher eventPublisher;

    private ReplacementProposalValidator validator;
    private ReplacementProposalMapper mapper;
    private ReplacementProposalServiceImpl proposalService;

    private UUID appointmentId;
    private UUID originalDoctorId;
    private UUID proposedDoctorId;
    private UUID proposedSlotId;
    private UUID proposedClinicId;
    private UUID proposedSpecialtyId;
    private UUID proposedRoomId;
    private LocalDate proposedDate;
    private LocalTime proposedStartTime;
    private LocalTime proposedEndTime;

    private Appointment confirmedAppointment;
    private SchedulingClient.SlotSnapshotDto proposedSlotSnapshot;

    @BeforeEach
    void setUp() {
        validator = new ReplacementProposalValidator();
        mapper = new ReplacementProposalMapper();
        proposalService = new ReplacementProposalServiceImpl(
                appointmentRepository,
                appointmentHistoryRepository,
                replacementProposalRepository,
                schedulingClient,
                validator,
                mapper,
                eventPublisher
        );


        appointmentId = UUID.randomUUID();
        originalDoctorId = UUID.randomUUID();
        proposedDoctorId = UUID.randomUUID();
        proposedSlotId = UUID.randomUUID();
        proposedClinicId = UUID.randomUUID();
        proposedSpecialtyId = UUID.randomUUID();
        proposedRoomId = UUID.randomUUID();
        proposedDate = LocalDate.now().plusDays(2);
        proposedStartTime = LocalTime.of(10, 0);
        proposedEndTime = LocalTime.of(10, 30);

        confirmedAppointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(UUID.randomUUID())
                .doctorId(originalDoctorId)
                .clinicId(UUID.randomUUID())
                .specialtyId(proposedSpecialtyId)
                .roomId(UUID.randomUUID())
                .slotId(UUID.randomUUID())
                .appointmentDate(proposedDate)
                .startTime(proposedStartTime)
                .endTime(proposedEndTime)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        proposedSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                proposedSlotId,
                UUID.randomUUID(),
                proposedDoctorId,
                proposedClinicId,
                proposedSpecialtyId,
                proposedRoomId,
                proposedDate,
                proposedStartTime,
                proposedEndTime,
                "AVAILABLE"
        );
    }

    @Test
    @DisplayName("Should create replacement proposal with 24h expiration and NOT hold slot (BR-REPLACEMENT-002, 004)")
    void shouldCreateReplacementProposalSuccessfully() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(confirmedAppointment));
        when(replacementProposalRepository.findFirstByAppointment_AppointmentIdAndStatus(
                appointmentId, ReplacementProposalStatus.PENDING)).thenReturn(Optional.empty());
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));

        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(invocation -> {
            ReplacementProposal rp = invocation.getArgument(0);
            rp.setProposalId(UUID.randomUUID());
            return rp;
        });

        CreateReplacementProposalRequest request = new CreateReplacementProposalRequest(proposedSlotId);
        ReplacementProposalResponse response = proposalService.createProposal(appointmentId, request);

        assertNotNull(response);
        assertEquals(appointmentId, response.appointmentId());
        assertEquals(originalDoctorId, response.originalDoctorId());
        assertEquals(proposedDoctorId, response.proposedDoctorId());
        assertEquals(proposedSlotId, response.proposedSlotId());
        assertEquals(ReplacementProposalStatus.PENDING, response.status());

        // BR-REPLACEMENT-004: 24-hour expiration verification
        Instant expectedMinExpiresAt = Instant.now().plus(23, ChronoUnit.HOURS);
        Instant expectedMaxExpiresAt = Instant.now().plus(25, ChronoUnit.HOURS);
        assertTrue(response.expiresAt().isAfter(expectedMinExpiresAt) && response.expiresAt().isBefore(expectedMaxExpiresAt));

        // BR-REPLACEMENT-002: MUST NOT HOLD proposed slot!
        verify(schedulingClient, never()).holdSlot(any());
        verify(replacementProposalRepository).save(any(ReplacementProposal.class));
    }

    @Test
    @DisplayName("Should reject proposal creation when active PENDING proposal already exists")
    void shouldRejectWhenActiveProposalAlreadyExists() {
        ReplacementProposal existingProposal = ReplacementProposal.builder()
                .proposalId(UUID.randomUUID())
                .status(ReplacementProposalStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(confirmedAppointment));
        when(replacementProposalRepository.findFirstByAppointment_AppointmentIdAndStatus(
                appointmentId, ReplacementProposalStatus.PENDING)).thenReturn(Optional.of(existingProposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));

        CreateReplacementProposalRequest request = new CreateReplacementProposalRequest(proposedSlotId);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.createProposal(appointmentId, request));
        assertEquals(ErrorCode.ACTIVE_PROPOSAL_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject proposal creation when appointment is CANCELLED")
    void shouldRejectWhenAppointmentCancelled() {
        confirmedAppointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(confirmedAppointment));
        when(replacementProposalRepository.findFirstByAppointment_AppointmentIdAndStatus(
                appointmentId, ReplacementProposalStatus.PENDING)).thenReturn(Optional.empty());
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));

        CreateReplacementProposalRequest request = new CreateReplacementProposalRequest(proposedSlotId);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.createProposal(appointmentId, request));
        assertEquals(ErrorCode.INVALID_STATUS_TRANSITION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject proposal creation when proposed slot is not found")
    void shouldRejectWhenProposedSlotNotFound() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(confirmedAppointment));
        when(replacementProposalRepository.findFirstByAppointment_AppointmentIdAndStatus(
                appointmentId, ReplacementProposalStatus.PENDING)).thenReturn(Optional.empty());
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.empty());

        CreateReplacementProposalRequest request = new CreateReplacementProposalRequest(proposedSlotId);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.createProposal(appointmentId, request));
        assertEquals(ErrorCode.PROPOSED_SLOT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should get proposal by ID successfully")
    void shouldGetProposalByIdSuccessfully() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .originalDoctorId(originalDoctorId)
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(proposedSpecialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(proposedDate)
                .proposedStartTime(proposedStartTime)
                .proposedEndTime(proposedEndTime)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(replacementProposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        ReplacementProposalResponse response = proposalService.getProposalById(appointmentId, proposalId);

        assertNotNull(response);
        assertEquals(proposalId, response.proposalId());
        assertEquals(appointmentId, response.appointmentId());
    }

    @Test
    @DisplayName("Should reject getting proposal when appointment ID does not match")
    void shouldRejectGetProposalWhenAppointmentMismatch() {
        UUID proposalId = UUID.randomUUID();
        UUID otherAppointmentId = UUID.randomUUID();
        Appointment otherAppointment = Appointment.builder().appointmentId(otherAppointmentId).build();

        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(otherAppointment)
                .build();

        when(replacementProposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.getProposalById(appointmentId, proposalId));
        assertEquals(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should expire overdue pending proposals while keeping Appointment unchanged (BR-REPLACEMENT-004, 005)")
    void shouldExpireOverduePendingProposals() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal overdueProposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByStatusAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(overdueProposal));

        int expiredCount = proposalService.expirePendingProposals();

        assertEquals(1, expiredCount);
        assertEquals(ReplacementProposalStatus.EXPIRED, overdueProposal.getStatus());
        verify(replacementProposalRepository).save(overdueProposal);

        // BR-REPLACEMENT-005: Underlying appointment is NOT cancelled or modified!
        assertEquals(AppointmentStatus.CONFIRMED, confirmedAppointment.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept proposal successfully: hold slot, book slot, release old slot, reassign appointment and audit (WF-05A)")
    void shouldAcceptProposalSuccessfully() {
        UUID proposalId = UUID.randomUUID();
        UUID oldSlotId = confirmedAppointment.getSlotId();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .originalDoctorId(originalDoctorId)
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(proposedSpecialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(proposedDate)
                .proposedStartTime(proposedStartTime)
                .proposedEndTime(proposedEndTime)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));
        when(schedulingClient.holdSlot(proposedSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(proposedSlotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        ReplacementProposalResponse response = proposalService.acceptProposal(proposalId);

        assertNotNull(response);
        assertEquals(ReplacementProposalStatus.ACCEPTED, response.status());
        assertNotNull(response.respondedAt());

        // Verify slot state machine actions
        verify(schedulingClient).holdSlot(proposedSlotId);
        verify(schedulingClient).bookSlot(proposedSlotId);
        verify(schedulingClient).releaseSlot(oldSlotId);

        // Verify appointment reassigned
        assertEquals(proposedDoctorId, confirmedAppointment.getDoctorId());
        assertEquals(proposedClinicId, confirmedAppointment.getClinicId());
        assertEquals(proposedRoomId, confirmedAppointment.getRoomId());
        assertEquals(proposedSlotId, confirmedAppointment.getSlotId());
        assertEquals(proposedDate, confirmedAppointment.getAppointmentDate());
        assertEquals(proposedStartTime, confirmedAppointment.getStartTime());
        assertEquals(proposedEndTime, confirmedAppointment.getEndTime());
        verify(appointmentRepository).save(confirmedAppointment);

        // Verify audit trail recorded
        verify(appointmentHistoryRepository).save(any(AppointmentHistory.class));
        verify(replacementProposalRepository).save(proposal);
    }

    @Test
    @DisplayName("Should reject accept when proposal is not in PENDING status")
    void shouldRejectAcceptWhenProposalNotPending() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.ACCEPTED)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.acceptProposal(proposalId));
        assertEquals(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION, ex.getErrorCode());
        verify(schedulingClient, never()).holdSlot(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject accept when proposal is expired and mark it EXPIRED (BR-REPLACEMENT-004)")
    void shouldRejectAcceptWhenProposalExpired() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.acceptProposal(proposalId));
        assertEquals(ErrorCode.PROPOSAL_EXPIRED, ex.getErrorCode());
        assertEquals(ReplacementProposalStatus.EXPIRED, proposal.getStatus());
        verify(replacementProposalRepository).save(proposal);
        verify(schedulingClient, never()).holdSlot(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject accept and keep appointment unchanged when proposed slot is not available (BR-REPLACEMENT-007, 008)")
    void shouldRejectAcceptWhenProposedSlotNotAvailable() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .proposedSlotId(proposedSlotId)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        SchedulingClient.SlotSnapshotDto bookedSlot = new SchedulingClient.SlotSnapshotDto(
                proposedSlotId, UUID.randomUUID(), proposedDoctorId, proposedClinicId,
                proposedSpecialtyId, proposedRoomId, proposedDate, proposedStartTime, proposedEndTime,
                "BOOKED"
        );

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(bookedSlot));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.acceptProposal(proposalId));
        assertEquals(ErrorCode.PROPOSED_SLOT_NOT_AVAILABLE, ex.getErrorCode());

        // BR-REPLACEMENT-008: Appointment stays unchanged!
        assertEquals(originalDoctorId, confirmedAppointment.getDoctorId());
        verify(appointmentRepository, never()).save(any());
        verify(schedulingClient, never()).holdSlot(any());
        verify(schedulingClient, never()).releaseSlot(any());
        assertEquals(ReplacementProposalStatus.PENDING, proposal.getStatus());
    }

    @Test
    @DisplayName("Should reject accept and keep appointment unchanged when holdSlot fails (BR-REPLACEMENT-008)")
    void shouldRejectAcceptWhenHoldSlotFails() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .proposedSlotId(proposedSlotId)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));
        when(schedulingClient.holdSlot(proposedSlotId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.acceptProposal(proposalId));
        assertEquals(ErrorCode.PROPOSED_SLOT_NOT_AVAILABLE, ex.getErrorCode());

        // BR-REPLACEMENT-008: Appointment stays unchanged!
        assertEquals(originalDoctorId, confirmedAppointment.getDoctorId());
        verify(appointmentRepository, never()).save(any());
        verify(schedulingClient, never()).bookSlot(any());
        verify(schedulingClient, never()).releaseSlot(any());
        assertEquals(ReplacementProposalStatus.PENDING, proposal.getStatus());
    }

    @Test
    @DisplayName("Should compensate by releasing held proposed slot when bookSlot fails")
    void shouldCompensateWhenBookSlotFails() {
        UUID proposalId = UUID.randomUUID();
        UUID oldSlotId = confirmedAppointment.getSlotId();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .proposedSlotId(proposedSlotId)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(proposedSlotSnapshot));
        when(schedulingClient.holdSlot(proposedSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(proposedSlotId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.acceptProposal(proposalId));
        assertEquals(ErrorCode.SLOT_BOOK_FAILED, ex.getErrorCode());

        // Compensation: proposed slot must be released
        verify(schedulingClient).releaseSlot(proposedSlotId);
        // Old slot must NOT be released
        verify(schedulingClient, never()).releaseSlot(oldSlotId);
        // Appointment unchanged
        assertEquals(originalDoctorId, confirmedAppointment.getDoctorId());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reschedule proposal successfully: hold new slot, book new slot, release old slot, update appointment, and proposal = RESCHEDULED (WF-05B)")
    void shouldRescheduleProposalSuccessfully() {
        UUID proposalId = UUID.randomUUID();
        UUID oldSlotId = confirmedAppointment.getSlotId();
        UUID newSlotId = UUID.randomUUID();
        UUID newDoctorId = UUID.randomUUID();
        UUID newClinicId = UUID.randomUUID();
        UUID newRoomId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(5);
        LocalTime newStartTime = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(14, 30);

        SchedulingClient.SlotSnapshotDto newSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                newSlotId, UUID.randomUUID(), newDoctorId, newClinicId,
                proposedSpecialtyId, newRoomId, newDate, newStartTime, newEndTime,
                "AVAILABLE"
        );

        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .originalDoctorId(originalDoctorId)
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(proposedSpecialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(proposedDate)
                .proposedStartTime(proposedStartTime)
                .proposedEndTime(proposedEndTime)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        RescheduleProposalRequest request = new RescheduleProposalRequest(newSlotId, "Patient prefers afternoon");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(newSlotId)).thenReturn(Optional.of(newSlotSnapshot));
        when(schedulingClient.holdSlot(newSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(newSlotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        ReplacementProposalResponse response = proposalService.rescheduleProposal(proposalId, request);

        assertNotNull(response);
        assertEquals(ReplacementProposalStatus.RESCHEDULED, response.status());
        assertNotNull(response.respondedAt());

        // Verify slot operations
        verify(schedulingClient).holdSlot(newSlotId);
        verify(schedulingClient).bookSlot(newSlotId);
        verify(schedulingClient).releaseSlot(oldSlotId);

        // Verify appointment reassigned to new slot
        assertEquals(newDoctorId, confirmedAppointment.getDoctorId());
        assertEquals(newClinicId, confirmedAppointment.getClinicId());
        assertEquals(newRoomId, confirmedAppointment.getRoomId());
        assertEquals(newSlotId, confirmedAppointment.getSlotId());
        assertEquals(newDate, confirmedAppointment.getAppointmentDate());
        assertEquals(newStartTime, confirmedAppointment.getStartTime());
        assertEquals(newEndTime, confirmedAppointment.getEndTime());
        verify(appointmentRepository).save(confirmedAppointment);

        // Verify audit history and proposal saved
        verify(appointmentHistoryRepository).save(any(AppointmentHistory.class));
        verify(replacementProposalRepository).save(proposal);
    }

    @Test
    @DisplayName("Should reject reschedule when proposal is not PENDING")
    void shouldRejectRescheduleWhenProposalNotPending() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.ACCEPTED)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        RescheduleProposalRequest request = new RescheduleProposalRequest(UUID.randomUUID(), "Change slot");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.rescheduleProposal(proposalId, request));
        assertEquals(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION, ex.getErrorCode());
        verify(schedulingClient, never()).holdSlot(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject reschedule when proposal is expired and mark it EXPIRED (BR-REPLACEMENT-004)")
    void shouldRejectRescheduleWhenProposalExpired() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        RescheduleProposalRequest request = new RescheduleProposalRequest(UUID.randomUUID(), "Change slot");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.rescheduleProposal(proposalId, request));
        assertEquals(ErrorCode.PROPOSAL_EXPIRED, ex.getErrorCode());
        assertEquals(ReplacementProposalStatus.EXPIRED, proposal.getStatus());
        verify(replacementProposalRepository).save(proposal);
        verify(schedulingClient, never()).holdSlot(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject reschedule when new slot is identical to current slot")
    void shouldRejectRescheduleWhenNewSlotMatchesCurrent() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        RescheduleProposalRequest request = new RescheduleProposalRequest(confirmedAppointment.getSlotId(), "Same slot");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.rescheduleProposal(proposalId, request));
        assertEquals(ErrorCode.CANNOT_RESCHEDULE, ex.getErrorCode());
        verify(schedulingClient, never()).holdSlot(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject reschedule when hold new slot fails and keep appointment unchanged")
    void shouldRejectRescheduleWhenHoldNewSlotFails() {
        UUID proposalId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        SchedulingClient.SlotSnapshotDto newSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                newSlotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                proposedSpecialtyId, UUID.randomUUID(), proposedDate, proposedStartTime, proposedEndTime,
                "AVAILABLE"
        );

        RescheduleProposalRequest request = new RescheduleProposalRequest(newSlotId, "Different slot");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(newSlotId)).thenReturn(Optional.of(newSlotSnapshot));
        when(schedulingClient.holdSlot(newSlotId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.rescheduleProposal(proposalId, request));
        assertEquals(ErrorCode.SLOT_HOLD_FAILED, ex.getErrorCode());

        // Appointment and proposal stay unchanged
        assertEquals(originalDoctorId, confirmedAppointment.getDoctorId());
        assertEquals(ReplacementProposalStatus.PENDING, proposal.getStatus());
        verify(appointmentRepository, never()).save(any());
        verify(schedulingClient, never()).bookSlot(any());
        verify(schedulingClient, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("Should compensate by releasing new slot when booking fails during reschedule")
    void shouldCompensateAndReleaseNewSlotWhenBookFailsDuringReschedule() {
        UUID proposalId = UUID.randomUUID();
        UUID oldSlotId = confirmedAppointment.getSlotId();
        UUID newSlotId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        SchedulingClient.SlotSnapshotDto newSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                newSlotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                proposedSpecialtyId, UUID.randomUUID(), proposedDate, proposedStartTime, proposedEndTime,
                "AVAILABLE"
        );

        RescheduleProposalRequest request = new RescheduleProposalRequest(newSlotId, "Different slot");

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(newSlotId)).thenReturn(Optional.of(newSlotSnapshot));
        when(schedulingClient.holdSlot(newSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(newSlotId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.rescheduleProposal(proposalId, request));
        assertEquals(ErrorCode.SLOT_BOOK_FAILED, ex.getErrorCode());

        // Compensation: release new slot
        verify(schedulingClient).releaseSlot(newSlotId);
        // Old slot must NOT be released
        verify(schedulingClient, never()).releaseSlot(oldSlotId);
        // Appointment unchanged
        assertEquals(originalDoctorId, confirmedAppointment.getDoctorId());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel proposal successfully: update status = CANCELLED, set respondedAt, keep appointment unchanged (WF-05C)")
    void shouldCancelProposalSuccessfully() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .originalDoctorId(originalDoctorId)
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(proposedSpecialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(proposedDate)
                .proposedStartTime(proposedStartTime)
                .proposedEndTime(proposedEndTime)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        ReplacementProposalResponse response = proposalService.cancelProposal(proposalId);

        assertNotNull(response);
        assertEquals(ReplacementProposalStatus.CANCELLED, response.status());
        assertNotNull(response.respondedAt());
        assertEquals(ReplacementProposalStatus.CANCELLED, proposal.getStatus());

        // WF-05C: Appointment is NOT automatically cancelled!
        assertEquals(AppointmentStatus.CONFIRMED, confirmedAppointment.getStatus());
        verify(appointmentRepository, never()).save(any());
        verify(schedulingClient, never()).releaseSlot(any());
        verify(replacementProposalRepository).save(proposal);
    }

    @Test
    @DisplayName("Should reject cancel when proposal is not PENDING")
    void shouldRejectCancelWhenProposalNotPending() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.ACCEPTED)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.cancelProposal(proposalId));
        assertEquals(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION, ex.getErrorCode());
        verify(replacementProposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject cancel when proposal is expired and mark it EXPIRED (BR-REPLACEMENT-004)")
    void shouldRejectCancelWhenProposalExpired() {
        UUID proposalId = UUID.randomUUID();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(confirmedAppointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> proposalService.cancelProposal(proposalId));
        assertEquals(ErrorCode.PROPOSAL_EXPIRED, ex.getErrorCode());
        assertEquals(ReplacementProposalStatus.EXPIRED, proposal.getStatus());
        verify(replacementProposalRepository).save(proposal);
    }
}



