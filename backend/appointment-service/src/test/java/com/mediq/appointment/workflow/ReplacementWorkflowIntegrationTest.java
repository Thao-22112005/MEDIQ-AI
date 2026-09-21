package com.mediq.appointment.workflow;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.entity.*;
import com.mediq.appointment.event.EventPublisher;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.ReplacementProposalMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.repository.ReplacementProposalRepository;
import com.mediq.appointment.service.ReplacementProposalServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplacementWorkflowIntegrationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private ReplacementProposalRepository replacementProposalRepository;

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private EventPublisher eventPublisher;

    private ReplacementProposalServiceImpl proposalService;

    private UUID originalDoctorId;
    private UUID proposedDoctorId;
    private UUID originalClinicId;
    private UUID proposedClinicId;
    private UUID specialtyId;
    private UUID proposedRoomId;
    private UUID oldSlotId;
    private UUID proposedSlotId;

    @BeforeEach
    void setUp() {
        ReplacementProposalValidator validator = new ReplacementProposalValidator();
        ReplacementProposalMapper mapper = new ReplacementProposalMapper();

        proposalService = new ReplacementProposalServiceImpl(
                appointmentRepository,
                appointmentHistoryRepository,
                replacementProposalRepository,
                schedulingClient,
                validator,
                mapper,
                eventPublisher
        );

        originalDoctorId = UUID.randomUUID();
        proposedDoctorId = UUID.randomUUID();
        originalClinicId = UUID.randomUUID();
        proposedClinicId = UUID.randomUUID(); // Different clinic (cross-clinic test)
        specialtyId = UUID.randomUUID();
        proposedRoomId = UUID.randomUUID();
        oldSlotId = UUID.randomUUID();
        proposedSlotId = UUID.randomUUID();
    }

    @Test
    @DisplayName("End-to-End WF-06 & WF-07: Proposal Creation -> Patient Accept -> Reassign & Revalidate Slot")
    void completeReplacementAcceptanceWorkflow_Success() {
        UUID appointmentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(UUID.randomUUID())
                .doctorId(originalDoctorId)
                .clinicId(originalClinicId)
                .roomId(UUID.randomUUID())
                .specialtyId(specialtyId)
                .slotId(oldSlotId)
                .appointmentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(appointment)
                .originalDoctorId(originalDoctorId)
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(specialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(LocalDate.now().plusDays(3))
                .proposedStartTime(LocalTime.of(14, 0))
                .proposedEndTime(LocalTime.of(14, 30))
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        SchedulingClient.SlotSnapshotDto slotSnapshot = new SchedulingClient.SlotSnapshotDto(
                proposedSlotId,
                UUID.randomUUID(),
                proposedDoctorId,
                proposedClinicId,
                specialtyId,
                proposedRoomId,
                LocalDate.now().plusDays(3),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                "AVAILABLE"
        );

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(slotSnapshot));
        when(schedulingClient.holdSlot(proposedSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(proposedSlotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        // Patient accepts proposal
        ReplacementProposalResponse response = proposalService.acceptProposal(proposalId);

        assertThat(response.status()).isEqualTo(ReplacementProposalStatus.ACCEPTED);
        // Verify slot state machine operations
        verify(schedulingClient).holdSlot(proposedSlotId);
        verify(schedulingClient).bookSlot(proposedSlotId);
        verify(schedulingClient).releaseSlot(oldSlotId);

        // Verify appointment was safely reassigned to replacement doctor at new clinic
        assertThat(appointment.getDoctorId()).isEqualTo(proposedDoctorId);
        assertThat(appointment.getClinicId()).isEqualTo(proposedClinicId);
        assertThat(appointment.getSlotId()).isEqualTo(proposedSlotId);
        assertThat(appointment.getStartTime()).isEqualTo(LocalTime.of(14, 0));

        // Verify audit history saved
        verify(appointmentHistoryRepository).save(any(AppointmentHistory.class));
        // Verify domain events published
        verify(eventPublisher, times(2)).publish(any()); // 1 for ReplacementProposal.Accepted, 1 for Appointment.Reassigned
    }

    @Test
    @DisplayName("End-to-End BR-REPLACEMENT-008: Proposed slot taken -> Abort accept, Appointment remains unchanged")
    void proposedSlotTaken_KeepsAppointmentUnchanged() {
        UUID appointmentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .doctorId(originalDoctorId)
                .clinicId(originalClinicId)
                .slotId(oldSlotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(appointment)
                .proposedSlotId(proposedSlotId)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        // Slot is already BOOKED by another patient
        SchedulingClient.SlotSnapshotDto bookedSnapshot = new SchedulingClient.SlotSnapshotDto(
                proposedSlotId,
                UUID.randomUUID(),
                proposedDoctorId,
                proposedClinicId,
                specialtyId,
                proposedRoomId,
                LocalDate.now().plusDays(3),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                "BOOKED"
        );

        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenReturn(Optional.of(proposal));
        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(bookedSnapshot));

        assertThatThrownBy(() -> proposalService.acceptProposal(proposalId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROPOSED_SLOT_NOT_AVAILABLE);

        // Crucial verification: Appointment still has original doctor, clinic, and slot
        assertThat(appointment.getDoctorId()).isEqualTo(originalDoctorId);
        assertThat(appointment.getClinicId()).isEqualTo(originalClinicId);
        assertThat(appointment.getSlotId()).isEqualTo(oldSlotId);
        assertThat(proposal.getStatus()).isEqualTo(ReplacementProposalStatus.PENDING);
        verify(schedulingClient, never()).bookSlot(any());
        verify(schedulingClient, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("End-to-End BR-REPLACEMENT-004 & BR-REPLACEMENT-005: Proposal Expiration marks EXPIRED but keeps Appointment intact")
    void expirePendingProposals_MarksExpiredKeepsAppointment() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .doctorId(originalDoctorId)
                .clinicId(originalClinicId)
                .slotId(oldSlotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        ReplacementProposal expiredProposal = ReplacementProposal.builder()
                .proposalId(UUID.randomUUID())
                .appointment(appointment)
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .build();

        when(replacementProposalRepository.findByStatusAndExpiresAtBefore(eq(ReplacementProposalStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(expiredProposal));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        int expiredCount = proposalService.expirePendingProposals();

        assertThat(expiredCount).isEqualTo(1);
        assertThat(expiredProposal.getStatus()).isEqualTo(ReplacementProposalStatus.EXPIRED);
        // BR-REPLACEMENT-005: Appointment is unchanged!
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getDoctorId()).isEqualTo(originalDoctorId);
    }
}
