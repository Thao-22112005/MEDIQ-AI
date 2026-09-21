package com.mediq.appointment.service;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.AppointmentMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.validation.AppointmentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private com.mediq.appointment.event.EventPublisher eventPublisher;

    private AppointmentValidator appointmentValidator;
    private AppointmentServiceImpl appointmentService;

    private UUID patientId;
    private UUID slotId;
    private UUID doctorId;
    private UUID clinicId;
    private UUID specialtyId;
    private UUID roomId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private SchedulingClient.SlotSnapshotDto slotSnapshot;

    @BeforeEach
    void setUp() {
        appointmentValidator = new AppointmentValidator();
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                appointmentHistoryRepository,
                schedulingClient,
                appointmentMapper,
                appointmentValidator,
                eventPublisher
        );

        patientId = UUID.randomUUID();
        slotId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        appointmentDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(9, 0);
        endTime = LocalTime.of(9, 30);

        slotSnapshot = new SchedulingClient.SlotSnapshotDto(
                slotId, UUID.randomUUID(), doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, "AVAILABLE"
        );
    }

    @Test
    @DisplayName("Should create appointment successfully preserving snapshot context (BR-APPOINTMENT-002, WF-02)")
    void shouldCreateAppointmentSuccessfully() {
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .patientId(patientId)
                .slotId(slotId)
                .build();

        when(schedulingClient.getSlot(slotId)).thenReturn(Optional.of(slotSnapshot));
        when(schedulingClient.holdSlot(slotId)).thenReturn(true);

        UUID appointmentId = UUID.randomUUID();
        Appointment savedAppointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .slotId(slotId)
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .appointmentDate(appointmentDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, slotId, doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, AppointmentStatus.PENDING, null,
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(savedAppointment)).thenReturn(expectedResponse);

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.PENDING, response.status());
        assertEquals(doctorId, response.doctorId());
        assertEquals(clinicId, response.clinicId());
        verify(schedulingClient).holdSlot(slotId);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should reject appointment creation when slot holding fails")
    void shouldRejectWhenSlotHoldingFails() {
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .patientId(patientId)
                .slotId(slotId)
                .build();

        when(schedulingClient.getSlot(slotId)).thenReturn(Optional.of(slotSnapshot));
        when(schedulingClient.holdSlot(slotId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> appointmentService.createAppointment(request));
        assertEquals(ErrorCode.SLOT_HOLD_FAILED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should confirm appointment successfully and book slot (WF-02)")
    void shouldConfirmAppointmentSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .slotId(slotId)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingClient.bookSlot(slotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, slotId, doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, AppointmentStatus.CONFIRMED, null,
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(appointment)).thenReturn(expectedResponse);

        AppointmentResponse response = appointmentService.confirmAppointment(appointmentId);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        verify(schedulingClient).bookSlot(slotId);
    }

    @Test
    @DisplayName("Should reject confirming when appointment is not PENDING")
    void shouldRejectConfirmWhenNotPending() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        BusinessException ex = assertThrows(BusinessException.class, () -> appointmentService.confirmAppointment(appointmentId));
        assertEquals(ErrorCode.INVALID_STATUS_TRANSITION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should cancel appointment and release slot")
    void shouldCancelAppointmentSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .slotId(slotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, slotId, doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, AppointmentStatus.CANCELLED, "Patient personal reason",
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(appointment)).thenReturn(expectedResponse);

        CancelAppointmentRequest request = new CancelAppointmentRequest("Patient personal reason");
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED, response.status());
        verify(schedulingClient).releaseSlot(slotId);
    }

    @Test
    @DisplayName("Should reject cancelling when cancellation reason is blank")
    void shouldRejectCancelWhenReasonIsBlank() {
        UUID appointmentId = UUID.randomUUID();
        CancelAppointmentRequest request = new CancelAppointmentRequest("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> appointmentService.cancelAppointment(appointmentId, request));
        assertEquals(ErrorCode.CANCELLATION_REASON_REQUIRED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should complete confirmed appointment successfully")
    void shouldCompleteAppointmentSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, slotId, doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, AppointmentStatus.COMPLETED, null,
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(appointment)).thenReturn(expectedResponse);

        AppointmentResponse response = appointmentService.completeAppointment(appointmentId);

        assertNotNull(response);
        assertEquals(AppointmentStatus.COMPLETED, response.status());
    }

    @Test
    @DisplayName("Should mark confirmed appointment as NO_SHOW (BR-APPOINTMENT-003)")
    void shouldMarkNoShowSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, slotId, doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, AppointmentStatus.NO_SHOW, null,
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(appointment)).thenReturn(expectedResponse);

        AppointmentResponse response = appointmentService.markNoShow(appointmentId);

        assertNotNull(response);
        assertEquals(AppointmentStatus.NO_SHOW, response.status());
    }

    @Test
    @DisplayName("Should reject marking NO_SHOW when appointment is not CONFIRMED (BR-APPOINTMENT-003)")
    void shouldRejectNoShowWhenNotConfirmed() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        BusinessException ex = assertThrows(BusinessException.class, () -> appointmentService.markNoShow(appointmentId));
        assertEquals(ErrorCode.INVALID_STATUS_TRANSITION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reschedule appointment successfully and update snapshot context")
    void shouldRescheduleAppointmentSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        UUID newDoctorId = UUID.randomUUID();
        UUID newClinicId = UUID.randomUUID();
        UUID newRoomId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(2);
        LocalTime newStart = LocalTime.of(14, 0);
        LocalTime newEnd = LocalTime.of(14, 30);

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .slotId(slotId)
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .appointmentDate(appointmentDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        SchedulingClient.SlotSnapshotDto newSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                newSlotId, UUID.randomUUID(), newDoctorId, newClinicId, specialtyId, newRoomId,
                newDate, newStart, newEnd, "AVAILABLE"
        );

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingClient.getSlot(newSlotId)).thenReturn(Optional.of(newSlotSnapshot));
        when(schedulingClient.holdSlot(newSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(newSlotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse expectedResponse = new AppointmentResponse(
                appointmentId, patientId, newSlotId, newDoctorId, newClinicId, specialtyId, newRoomId,
                newDate, newStart, newEnd, AppointmentStatus.CONFIRMED, null,
                Instant.now(), Instant.now()
        );
        when(appointmentMapper.toAppointmentResponse(any(Appointment.class))).thenReturn(expectedResponse);

        com.mediq.appointment.dto.request.RescheduleAppointmentRequest request =
                new com.mediq.appointment.dto.request.RescheduleAppointmentRequest(newSlotId, "Schedule conflict", "PATIENT");

        AppointmentResponse response = appointmentService.rescheduleAppointment(appointmentId, request);

        assertNotNull(response);
        assertEquals(newSlotId, response.slotId());
        assertEquals(newDoctorId, response.doctorId());
        verify(schedulingClient).holdSlot(newSlotId);
        verify(schedulingClient).bookSlot(newSlotId);
        verify(schedulingClient).releaseSlot(slotId);
        verify(appointmentHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should reject reschedule when targeting the exact same slot")
    void shouldRejectRescheduleWhenSameSlot() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .slotId(slotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        com.mediq.appointment.dto.request.RescheduleAppointmentRequest request =
                new com.mediq.appointment.dto.request.RescheduleAppointmentRequest(slotId, "Same slot test", "PATIENT");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request));
        assertEquals(ErrorCode.RESCHEDULE_SAME_SLOT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject reschedule when appointment is in CANCELLED status")
    void shouldRejectRescheduleWhenCancelled() {
        UUID appointmentId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .slotId(slotId)
                .status(AppointmentStatus.CANCELLED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        com.mediq.appointment.dto.request.RescheduleAppointmentRequest request =
                new com.mediq.appointment.dto.request.RescheduleAppointmentRequest(newSlotId, "Reason", "PATIENT");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request));
        assertEquals(ErrorCode.CANNOT_RESCHEDULE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should compensate and release new slot when booking new slot fails during reschedule")
    void shouldCompensateWhenBookingNewSlotFails() {
        UUID appointmentId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .slotId(slotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        SchedulingClient.SlotSnapshotDto newSlotSnapshot = new SchedulingClient.SlotSnapshotDto(
                newSlotId, UUID.randomUUID(), doctorId, clinicId, specialtyId, roomId,
                appointmentDate, startTime, endTime, "AVAILABLE"
        );

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingClient.getSlot(newSlotId)).thenReturn(Optional.of(newSlotSnapshot));
        when(schedulingClient.holdSlot(newSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(newSlotId)).thenReturn(false);

        com.mediq.appointment.dto.request.RescheduleAppointmentRequest request =
                new com.mediq.appointment.dto.request.RescheduleAppointmentRequest(newSlotId, "Reason", "PATIENT");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request));
        assertEquals(ErrorCode.SLOT_BOOK_FAILED, ex.getErrorCode());
        verify(schedulingClient).releaseSlot(newSlotId);
    }
}
