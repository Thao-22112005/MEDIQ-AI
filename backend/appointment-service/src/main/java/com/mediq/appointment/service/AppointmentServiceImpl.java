package com.mediq.appointment.service;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.request.RescheduleAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentHistoryResponse;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentHistory;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.event.EventEnvelope;
import com.mediq.appointment.event.EventPublisher;
import com.mediq.appointment.event.payload.AppointmentCancelledEventPayload;
import com.mediq.appointment.event.payload.AppointmentConfirmedEventPayload;
import com.mediq.appointment.event.payload.AppointmentCreatedEventPayload;
import com.mediq.appointment.event.payload.AppointmentRescheduledEventPayload;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.AppointmentMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.validation.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository appointmentHistoryRepository;
    private final SchedulingClient schedulingClient;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentValidator appointmentValidator;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        appointmentValidator.validateCreate(request);

        // Fetch slot snapshot from Scheduling Service
        SchedulingClient.SlotSnapshotDto slot = schedulingClient.getSlot(request.getSlotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                        "Slot not found in scheduling service with ID: " + request.getSlotId()));

        // Call Scheduling Service to HOLD the slot
        boolean held = schedulingClient.holdSlot(request.getSlotId());
        if (!held) {
            throw new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                    "Failed to hold slot in scheduling service (slot may already be held or booked)");
        }

        // BR-APPOINTMENT-002: Save snapshot context
        Appointment appointment = Appointment.builder()
                .patientId(request.getPatientId())
                .slotId(slot.slotId())
                .doctorId(slot.doctorId())
                .clinicId(slot.clinicId())
                .specialtyId(slot.specialtyId())
                .roomId(slot.roomId())
                .appointmentDate(slot.date())
                .startTime(slot.startTime())
                .endTime(slot.endTime())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        recordHistory(savedAppointment, "CREATED", null, AppointmentStatus.PENDING.name(), "SYSTEM");

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Created",
                "appointment-service",
                new AppointmentCreatedEventPayload(
                        savedAppointment.getAppointmentId(),
                        savedAppointment.getPatientId(),
                        savedAppointment.getDoctorId(),
                        savedAppointment.getClinicId(),
                        savedAppointment.getSlotId(),
                        savedAppointment.getAppointmentDate(),
                        savedAppointment.getStartTime(),
                        savedAppointment.getEndTime()
                )
        ));

        return appointmentMapper.toAppointmentResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAppointments(UUID patientId, UUID doctorId, UUID clinicId, LocalDate date, AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findAll();

        List<Appointment> filtered = appointments.stream()
                .filter(a -> patientId == null || a.getPatientId().equals(patientId))
                .filter(a -> doctorId == null || a.getDoctorId().equals(doctorId))
                .filter(a -> clinicId == null || a.getClinicId().equals(clinicId))
                .filter(a -> date == null || a.getAppointmentDate().equals(date))
                .filter(a -> status == null || a.getStatus() == status)
                .toList();

        return appointmentMapper.toAppointmentResponseList(filtered);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only PENDING appointments can be confirmed (current status: " + appointment.getStatus() + ")");
        }

        // Call Scheduling Service to BOOK the slot
        boolean booked = schedulingClient.bookSlot(appointment.getSlotId());
        if (!booked) {
            throw new BusinessException(ErrorCode.SLOT_BOOK_FAILED,
                    "Failed to book slot in scheduling service");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        recordHistory(updatedAppointment, "CONFIRMED", oldStatus.name(), AppointmentStatus.CONFIRMED.name(), "SYSTEM");

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Confirmed",
                "appointment-service",
                new AppointmentConfirmedEventPayload(
                        updatedAppointment.getAppointmentId(),
                        updatedAppointment.getPatientId(),
                        updatedAppointment.getDoctorId(),
                        updatedAppointment.getClinicId(),
                        updatedAppointment.getSlotId(),
                        updatedAppointment.getAppointmentDate()
                )
        ));

        return appointmentMapper.toAppointmentResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, CancelAppointmentRequest request) {
        appointmentValidator.validateCancel(request);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot cancel appointment with status: " + appointment.getStatus());
        }

        // Release slot in scheduling service
        schedulingClient.releaseSlot(appointment.getSlotId());

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.getReason().trim());
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        recordHistory(updatedAppointment, "CANCELLED", oldStatus.name(), AppointmentStatus.CANCELLED.name(), "PATIENT");

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Cancelled",
                "appointment-service",
                new AppointmentCancelledEventPayload(
                        updatedAppointment.getAppointmentId(),
                        updatedAppointment.getPatientId(),
                        updatedAppointment.getDoctorId(),
                        updatedAppointment.getSlotId(),
                        updatedAppointment.getCancellationReason()
                )
        ));

        return appointmentMapper.toAppointmentResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(UUID appointmentId, RescheduleAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        appointmentValidator.validateReschedule(request, appointment.getSlotId());

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.CANNOT_RESCHEDULE,
                    "Cannot reschedule appointment with status: " + appointment.getStatus());
        }

        UUID oldSlotId = appointment.getSlotId();
        UUID newSlotId = request.getNewSlotId();

        // 1. Fetch new slot snapshot from Scheduling Service
        SchedulingClient.SlotSnapshotDto newSlot = schedulingClient.getSlot(newSlotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                        "New slot not found in scheduling service with ID: " + newSlotId));

        // 2. HOLD new slot in scheduling service
        boolean held = schedulingClient.holdSlot(newSlotId);
        if (!held) {
            throw new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                    "Failed to hold new slot for rescheduling: " + newSlotId);
        }

        // 3. BOOK new slot
        boolean booked = schedulingClient.bookSlot(newSlotId);
        if (!booked) {
            // Compensation: release new slot
            schedulingClient.releaseSlot(newSlotId);
            throw new BusinessException(ErrorCode.SLOT_BOOK_FAILED,
                    "Failed to book new slot in scheduling service");
        }

        // 4. RELEASE old slot in scheduling service
        schedulingClient.releaseSlot(oldSlotId);

        // 5. Update appointment snapshot context
        appointment.setSlotId(newSlot.slotId());
        appointment.setDoctorId(newSlot.doctorId());
        appointment.setClinicId(newSlot.clinicId());
        appointment.setSpecialtyId(newSlot.specialtyId());
        appointment.setRoomId(newSlot.roomId());
        appointment.setAppointmentDate(newSlot.date());
        appointment.setStartTime(newSlot.startTime());
        appointment.setEndTime(newSlot.endTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // 6. Record history
        String changedBy = (request.getRescheduledBy() != null && !request.getRescheduledBy().trim().isEmpty())
                ? request.getRescheduledBy().trim()
                : "PATIENT";
        String reasonSuffix = (request.getReason() != null && !request.getReason().trim().isEmpty())
                ? " (" + request.getReason().trim() + ")"
                : "";
        recordHistory(updatedAppointment, "RESCHEDULED", "Slot: " + oldSlotId, "Slot: " + newSlotId + reasonSuffix, changedBy);

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Rescheduled",
                "appointment-service",
                new AppointmentRescheduledEventPayload(
                        updatedAppointment.getAppointmentId(),
                        oldSlotId,
                        newSlotId,
                        updatedAppointment.getAppointmentDate(),
                        updatedAppointment.getStartTime(),
                        updatedAppointment.getEndTime()
                )
        ));

        return appointmentMapper.toAppointmentResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only CONFIRMED appointments can be completed (current status: " + appointment.getStatus() + ")");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        recordHistory(updatedAppointment, "COMPLETED", oldStatus.name(), AppointmentStatus.COMPLETED.name(), "DOCTOR");

        return appointmentMapper.toAppointmentResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse markNoShow(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        // BR-APPOINTMENT-003: NO_SHOW belongs strictly to Appointment, allowed only from CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only CONFIRMED appointments can be marked as NO_SHOW (current status: " + appointment.getStatus() + ")");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        recordHistory(updatedAppointment, "NO_SHOW", oldStatus.name(), AppointmentStatus.NO_SHOW.name(), "STAFF");

        return appointmentMapper.toAppointmentResponse(updatedAppointment);
    }

    @Override
    public List<AppointmentHistoryResponse> getAppointmentHistory(UUID appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                    "Appointment not found with ID: " + appointmentId);
        }
        List<AppointmentHistory> histories = appointmentHistoryRepository
                .findByAppointment_AppointmentIdOrderByChangedAtAsc(appointmentId);
        return appointmentMapper.toHistoryResponseList(histories);
    }

    private void recordHistory(Appointment appointment, String action, String oldValue, String newValue, String changedBy) {
        AppointmentHistory history = AppointmentHistory.builder()
                .historyId(UUID.randomUUID())
                .appointment(appointment)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .build();
        appointmentHistoryRepository.save(history);
    }
}
