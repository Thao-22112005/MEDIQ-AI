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
import com.mediq.appointment.event.EventEnvelope;
import com.mediq.appointment.event.EventPublisher;
import com.mediq.appointment.event.payload.AppointmentReassignedEventPayload;
import com.mediq.appointment.event.payload.AppointmentRescheduledEventPayload;
import com.mediq.appointment.event.payload.ReplacementProposalAcceptedEventPayload;
import com.mediq.appointment.event.payload.ReplacementProposalCancelledEventPayload;
import com.mediq.appointment.event.payload.ReplacementProposalCreatedEventPayload;
import com.mediq.appointment.event.payload.ReplacementProposalExpiredEventPayload;
import com.mediq.appointment.event.payload.ReplacementProposalRescheduledEventPayload;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.ReplacementProposalMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.repository.ReplacementProposalRepository;
import com.mediq.appointment.validation.ReplacementProposalValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplacementProposalServiceImpl implements ReplacementProposalService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository appointmentHistoryRepository;
    private final ReplacementProposalRepository replacementProposalRepository;
    private final SchedulingClient schedulingClient;
    private final ReplacementProposalValidator validator;
    private final ReplacementProposalMapper mapper;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public ReplacementProposalResponse createProposal(UUID appointmentId, CreateReplacementProposalRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId));

        boolean hasActivePending = replacementProposalRepository
                .findFirstByAppointment_AppointmentIdAndStatus(appointmentId, ReplacementProposalStatus.PENDING)
                .isPresent();

        SchedulingClient.SlotSnapshotDto slotSnapshot = null;
        if (request != null && request.getProposedSlotId() != null) {
            slotSnapshot = schedulingClient.getSlot(request.getProposedSlotId()).orElse(null);
        }

        validator.validateCreate(request, appointment, hasActivePending, slotSnapshot);

        // BR-REPLACEMENT-002: Do NOT hold proposed slot in 24h waiting period
        // BR-REPLACEMENT-004: Patient has 24 hours to respond
        Instant now = Instant.now();
        ReplacementProposal proposal = ReplacementProposal.builder()
                .appointment(appointment)
                .originalDoctorId(appointment.getDoctorId())
                .proposedDoctorId(slotSnapshot.doctorId())
                .proposedClinicId(slotSnapshot.clinicId())
                .proposedSpecialtyId(slotSnapshot.specialtyId())
                .proposedRoomId(slotSnapshot.roomId())
                .proposedSlotId(slotSnapshot.slotId())
                .proposedDate(slotSnapshot.date())
                .proposedStartTime(slotSnapshot.startTime())
                .proposedEndTime(slotSnapshot.endTime())
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .createdAt(now)
                .updatedAt(now)
                .build();

        ReplacementProposal saved = replacementProposalRepository.save(proposal);

        eventPublisher.publish(EventEnvelope.of(
                "ReplacementProposal.Created",
                "appointment-service",
                new ReplacementProposalCreatedEventPayload(
                        saved.getProposalId(),
                        appointment.getAppointmentId(),
                        saved.getOriginalDoctorId(),
                        saved.getProposedDoctorId(),
                        saved.getProposedSlotId(),
                        saved.getExpiresAt()
                )
        ));

        return mapper.toResponse(saved);
    }

    @Override
    public ReplacementProposalResponse getProposalById(UUID appointmentId, UUID proposalId) {
        ReplacementProposal proposal = replacementProposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND,
                        "Replacement proposal not found with ID: " + proposalId));

        if (!proposal.getAppointment().getAppointmentId().equals(appointmentId)) {
            throw new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND,
                    "Proposal does not belong to appointment ID: " + appointmentId);
        }

        return mapper.toResponse(proposal);
    }

    @Override
    public List<ReplacementProposalResponse> listProposalsByAppointment(UUID appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND,
                    "Appointment not found with ID: " + appointmentId);
        }

        List<ReplacementProposal> list = replacementProposalRepository
                .findByAppointment_AppointmentIdOrderByCreatedAtDesc(appointmentId);
        return mapper.toResponseList(list);
    }

    @Override
    @Transactional
    public int expirePendingProposals() {
        // BR-REPLACEMENT-004 & BR-REPLACEMENT-005:
        // Transition expired pending proposals to EXPIRED. Keep underlying Appointment unchanged!
        List<ReplacementProposal> expiredProposals = replacementProposalRepository
                .findByStatusAndExpiresAtBefore(ReplacementProposalStatus.PENDING, Instant.now());

        for (ReplacementProposal proposal : expiredProposals) {
            proposal.setStatus(ReplacementProposalStatus.EXPIRED);
            replacementProposalRepository.save(proposal);

            eventPublisher.publish(EventEnvelope.of(
                    "ReplacementProposal.Expired",
                    "appointment-service",
                    new ReplacementProposalExpiredEventPayload(
                            proposal.getProposalId(),
                            proposal.getAppointment().getAppointmentId(),
                            proposal.getExpiresAt()
                    )
            ));
        }

        return expiredProposals.size();
    }

    @Override
    @Transactional
    public ReplacementProposalResponse acceptProposal(UUID proposalId) {
        ReplacementProposal proposal = replacementProposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND,
                        "Replacement proposal not found with ID: " + proposalId));

        // 1. Check status == PENDING
        if (proposal.getStatus() != ReplacementProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION,
                    "Only PENDING proposals can be accepted (current status: " + proposal.getStatus() + ")");
        }

        // 2. Check not expired (BR-REPLACEMENT-004)
        Instant now = Instant.now();
        if (proposal.getExpiresAt().isBefore(now)) {
            proposal.setStatus(ReplacementProposalStatus.EXPIRED);
            replacementProposalRepository.save(proposal);
            throw new BusinessException(ErrorCode.PROPOSAL_EXPIRED,
                    "Replacement proposal has expired (24h response window ended)");
        }

        Appointment appointment = proposal.getAppointment();

        // 3. Revalidate proposed slot from scheduling service (BR-REPLACEMENT-007)
        SchedulingClient.SlotSnapshotDto slotSnapshot = schedulingClient.getSlot(proposal.getProposedSlotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPOSED_SLOT_NOT_FOUND,
                        "Proposed slot no longer exists in scheduling service"));

        if (!"AVAILABLE".equalsIgnoreCase(slotSnapshot.status())) {
            // BR-REPLACEMENT-008: Slot is no longer available -> keep Appointment unchanged!
            throw new BusinessException(ErrorCode.PROPOSED_SLOT_NOT_AVAILABLE,
                    "Proposed slot is no longer available. Appointment remains unchanged.");
        }

        // 4. HOLD proposed slot
        boolean held = schedulingClient.holdSlot(proposal.getProposedSlotId());
        if (!held) {
            // BR-REPLACEMENT-008: Failed to hold slot -> keep Appointment unchanged!
            throw new BusinessException(ErrorCode.PROPOSED_SLOT_NOT_AVAILABLE,
                    "Failed to hold proposed slot. Slot may have been taken by another patient.");
        }

        // 5. BOOK new slot
        boolean booked = schedulingClient.bookSlot(proposal.getProposedSlotId());
        if (!booked) {
            // Compensate: release new slot
            schedulingClient.releaseSlot(proposal.getProposedSlotId());
            throw new BusinessException(ErrorCode.SLOT_BOOK_FAILED,
                    "Failed to book proposed slot in scheduling service");
        }

        // 6. RELEASE old slot in scheduling service
        UUID oldSlotId = appointment.getSlotId();
        try {
            schedulingClient.releaseSlot(oldSlotId);
        } catch (Exception e) {
            // Log warning but proceed since new slot is safely booked
        }

        // 7. Reassign Appointment context (WF-05A)
        UUID originalDoctorId = appointment.getDoctorId();
        UUID originalClinicId = appointment.getClinicId();

        appointment.setDoctorId(proposal.getProposedDoctorId());
        appointment.setClinicId(proposal.getProposedClinicId());
        appointment.setSpecialtyId(proposal.getProposedSpecialtyId());
        appointment.setRoomId(proposal.getProposedRoomId());
        appointment.setSlotId(proposal.getProposedSlotId());
        appointment.setAppointmentDate(proposal.getProposedDate());
        appointment.setStartTime(proposal.getProposedStartTime());
        appointment.setEndTime(proposal.getProposedEndTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // 8. Proposal = ACCEPTED
        proposal.setStatus(ReplacementProposalStatus.ACCEPTED);
        proposal.setRespondedAt(now);
        ReplacementProposal updatedProposal = replacementProposalRepository.save(proposal);

        // 9. History
        recordHistory(
                appointment,
                "REASSIGNED",
                "Doctor: " + originalDoctorId + ", Clinic: " + originalClinicId + ", Slot: " + oldSlotId,
                "Doctor: " + proposal.getProposedDoctorId() + ", Clinic: " + proposal.getProposedClinicId() + ", Slot: " + proposal.getProposedSlotId() + " (Proposal: " + proposalId + ")",
                "PATIENT"
        );

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Reassigned",
                "appointment-service",
                new AppointmentReassignedEventPayload(
                        appointment.getAppointmentId(),
                        originalDoctorId,
                        proposal.getProposedDoctorId(),
                        originalClinicId,
                        proposal.getProposedClinicId(),
                        oldSlotId,
                        proposal.getProposedSlotId()
                )
        ));

        eventPublisher.publish(EventEnvelope.of(
                "ReplacementProposal.Accepted",
                "appointment-service",
                new ReplacementProposalAcceptedEventPayload(
                        updatedProposal.getProposalId(),
                        appointment.getAppointmentId(),
                        proposal.getProposedDoctorId(),
                        proposal.getProposedSlotId()
                )
        ));

        return mapper.toResponse(updatedProposal);
    }

    @Override
    @Transactional
    public ReplacementProposalResponse rescheduleProposal(UUID proposalId, RescheduleProposalRequest request) {
        ReplacementProposal proposal = replacementProposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND,
                        "Replacement proposal not found with ID: " + proposalId));

        Appointment appointment = proposal.getAppointment();
        validator.validateReschedule(proposal, request, appointment.getSlotId());

        Instant now = Instant.now();
        if (proposal.getExpiresAt().isBefore(now)) {
            proposal.setStatus(ReplacementProposalStatus.EXPIRED);
            replacementProposalRepository.save(proposal);
            throw new BusinessException(ErrorCode.PROPOSAL_EXPIRED,
                    "Replacement proposal has expired (24h response window ended)");
        }

        UUID newSlotId = request.getNewSlotId();
        SchedulingClient.SlotSnapshotDto newSlot = schedulingClient.getSlot(newSlotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                        "New slot not found in scheduling service with ID: " + newSlotId));

        // 1. HOLD new slot
        boolean held = schedulingClient.holdSlot(newSlotId);
        if (!held) {
            throw new BusinessException(ErrorCode.SLOT_HOLD_FAILED,
                    "Failed to hold new slot for rescheduling: " + newSlotId);
        }

        // 2. BOOK new slot
        boolean booked;
        try {
            booked = schedulingClient.bookSlot(newSlotId);
        } catch (Exception e) {
            schedulingClient.releaseSlot(newSlotId);
            throw new BusinessException(ErrorCode.SLOT_BOOK_FAILED, e.getMessage());
        }
        if (!booked) {
            schedulingClient.releaseSlot(newSlotId);
            throw new BusinessException(ErrorCode.SLOT_BOOK_FAILED,
                    "Failed to book new slot in scheduling service");
        }


        // 3. RELEASE old slot
        UUID oldSlotId = appointment.getSlotId();
        try {
            schedulingClient.releaseSlot(oldSlotId);
        } catch (Exception e) {
            // Log warning
        }

        // 4. Update Appointment
        UUID oldDoctorId = appointment.getDoctorId();
        UUID oldClinicId = appointment.getClinicId();

        appointment.setDoctorId(newSlot.doctorId());
        appointment.setClinicId(newSlot.clinicId());
        appointment.setSpecialtyId(newSlot.specialtyId());
        appointment.setRoomId(newSlot.roomId());
        appointment.setSlotId(newSlot.slotId());
        appointment.setAppointmentDate(newSlot.date());
        appointment.setStartTime(newSlot.startTime());
        appointment.setEndTime(newSlot.endTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // 5. Update Proposal
        proposal.setStatus(ReplacementProposalStatus.RESCHEDULED);
        proposal.setRespondedAt(now);
        ReplacementProposal updatedProposal = replacementProposalRepository.save(proposal);

        // 6. Record Audit Trail
        String reasonStr = (request.getReason() != null && !request.getReason().isBlank()) ? " Reason: " + request.getReason().trim() : "";
        recordHistory(
                appointment,
                "RESCHEDULED",
                "Doctor: " + oldDoctorId + ", Clinic: " + oldClinicId + ", Slot: " + oldSlotId,
                "Doctor: " + newSlot.doctorId() + ", Clinic: " + newSlot.clinicId() + ", Slot: " + newSlot.slotId() + " (Proposal: " + proposalId + ")" + reasonStr,
                "PATIENT"
        );

        eventPublisher.publish(EventEnvelope.of(
                "Appointment.Rescheduled",
                "appointment-service",
                new AppointmentRescheduledEventPayload(
                        appointment.getAppointmentId(),
                        oldSlotId,
                        newSlot.slotId(),
                        appointment.getAppointmentDate(),
                        appointment.getStartTime(),
                        appointment.getEndTime()
                )
        ));

        eventPublisher.publish(EventEnvelope.of(
                "ReplacementProposal.Rescheduled",
                "appointment-service",
                new ReplacementProposalRescheduledEventPayload(
                        updatedProposal.getProposalId(),
                        appointment.getAppointmentId(),
                        newSlot.slotId()
                )
        ));

        return mapper.toResponse(updatedProposal);
    }

    @Override
    @Transactional
    public ReplacementProposalResponse cancelProposal(UUID proposalId) {
        ReplacementProposal proposal = replacementProposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND,
                        "Replacement proposal not found with ID: " + proposalId));

        if (proposal.getStatus() != ReplacementProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION,
                    "Only PENDING proposals can be cancelled (current status: " + proposal.getStatus() + ")");
        }

        Instant now = Instant.now();
        if (proposal.getExpiresAt().isBefore(now)) {
            proposal.setStatus(ReplacementProposalStatus.EXPIRED);
            replacementProposalRepository.save(proposal);
            throw new BusinessException(ErrorCode.PROPOSAL_EXPIRED,
                    "Replacement proposal has expired (24h response window ended)");
        }

        proposal.setStatus(ReplacementProposalStatus.CANCELLED);
        proposal.setRespondedAt(now);
        ReplacementProposal updatedProposal = replacementProposalRepository.save(proposal);

        // WF-05C: Appointment is NOT automatically cancelled. Patient can cancel appointment separately if desired.

        eventPublisher.publish(EventEnvelope.of(
                "ReplacementProposal.Cancelled",
                "appointment-service",
                new ReplacementProposalCancelledEventPayload(
                        updatedProposal.getProposalId(),
                        updatedProposal.getAppointment().getAppointmentId()
                )
        ));

        return mapper.toResponse(updatedProposal);
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
