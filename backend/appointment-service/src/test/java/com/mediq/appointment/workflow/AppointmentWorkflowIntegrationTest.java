package com.mediq.appointment.workflow;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentHistory;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.event.EventPublisher;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.AppointmentMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.service.AppointmentServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentWorkflowIntegrationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private EventPublisher eventPublisher;

    private AppointmentServiceImpl appointmentService;

    private UUID patientId;
    private UUID slotId;
    private UUID doctorId;
    private UUID clinicId;
    private UUID specialtyId;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        AppointmentValidator validator = new AppointmentValidator();
        AppointmentMapper mapper = mock(AppointmentMapper.class);
        lenient().when(mapper.toAppointmentResponse(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            return new AppointmentResponse(
                    a.getAppointmentId(),
                    a.getPatientId(),
                    a.getSlotId(),
                    a.getDoctorId(),
                    a.getClinicId(),
                    a.getSpecialtyId(),
                    a.getRoomId(),
                    a.getAppointmentDate(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus(),
                    a.getCancellationReason(),
                    a.getCreatedAt(),
                    a.getUpdatedAt()
            );
        });

        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                appointmentHistoryRepository,
                schedulingClient,
                mapper,
                validator,
                eventPublisher
        );

        patientId = UUID.randomUUID();
        slotId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        roomId = UUID.randomUUID();
    }

    @Test
    @DisplayName("End-to-End WF-04 & WF-05: Create Pending Appointment -> Confirm -> Slot Booked")
    void completeBookingAndConfirmationFlow_Success() {
        // Step 1: Slot exists and is AVAILABLE in scheduling service
        SchedulingClient.SlotSnapshotDto availableSlot = new SchedulingClient.SlotSnapshotDto(
                slotId,
                UUID.randomUUID(),
                doctorId,
                clinicId,
                specialtyId,
                roomId,
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                "AVAILABLE"
        );
        when(schedulingClient.getSlot(slotId)).thenReturn(Optional.of(availableSlot));
        when(schedulingClient.holdSlot(slotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            if (a.getAppointmentId() == null) {
                a.setAppointmentId(UUID.randomUUID());
                a.setCreatedAt(Instant.now());
                a.setUpdatedAt(Instant.now());
            }
            return a;
        });

        // Execute: Create appointment
        CreateAppointmentRequest createRequest = new CreateAppointmentRequest(patientId, slotId);
        AppointmentResponse created = appointmentService.createAppointment(createRequest);

        assertThat(created).isNotNull();
        assertThat(created.status()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(created.slotId()).isEqualTo(slotId);
        assertThat(created.doctorId()).isEqualTo(doctorId);

        // Verify HOLD was invoked on scheduling service
        verify(schedulingClient).holdSlot(slotId);
        verify(appointmentHistoryRepository).save(any(AppointmentHistory.class));

        // Step 2: Confirm appointment
        UUID appointmentId = created.appointmentId();
        Appointment pendingAppointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .slotId(slotId)
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .appointmentDate(availableSlot.date())
                .startTime(availableSlot.startTime())
                .endTime(availableSlot.endTime())
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(pendingAppointment));
        when(schedulingClient.bookSlot(slotId)).thenReturn(true);

        AppointmentResponse confirmed = appointmentService.confirmAppointment(appointmentId);

        assertThat(confirmed.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(schedulingClient).bookSlot(slotId);
        verify(eventPublisher, times(2)).publish(any()); // 1 for Created, 1 for Confirmed
    }

    @Test
    @DisplayName("End-to-End Booking Failure: Book Slot fails -> Abort confirmation, remains PENDING")
    void bookingFailureOnBookSlotError() {
        UUID appointmentId = UUID.randomUUID();
        Appointment pendingAppointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .slotId(slotId)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(pendingAppointment));
        when(schedulingClient.bookSlot(slotId)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.confirmAppointment(appointmentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SLOT_BOOK_FAILED);

        assertThat(pendingAppointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("End-to-End Cancellation: Patient cancels confirmed appointment -> Slot released in scheduling service")
    void cancelConfirmedAppointment_ReleasesSlot() {
        UUID appointmentId = UUID.randomUUID();
        Appointment confirmedAppointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .slotId(slotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(confirmedAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        appointmentService.cancelAppointment(appointmentId, new CancelAppointmentRequest("Personal conflict"));

        assertThat(confirmedAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(confirmedAppointment.getCancellationReason()).isEqualTo("Personal conflict");
        verify(schedulingClient).releaseSlot(slotId);
        verify(eventPublisher).publish(any());
    }
}
