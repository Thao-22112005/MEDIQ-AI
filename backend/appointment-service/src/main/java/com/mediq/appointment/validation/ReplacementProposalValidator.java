package com.mediq.appointment.validation;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.request.CreateReplacementProposalRequest;
import com.mediq.appointment.dto.request.RescheduleProposalRequest;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.entity.ReplacementProposal;
import com.mediq.appointment.entity.ReplacementProposalStatus;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReplacementProposalValidator {

    public void validateCreate(
            CreateReplacementProposalRequest request,
            Appointment appointment,
            boolean hasActivePendingProposal,
            SchedulingClient.SlotSnapshotDto slotSnapshot
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getProposedSlotId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Proposed Slot ID is required");
        }
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND, "Appointment not found");
        }
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot create replacement proposal for appointment in status: " + appointment.getStatus());
        }
        if (hasActivePendingProposal) {
            throw new BusinessException(ErrorCode.ACTIVE_PROPOSAL_ALREADY_EXISTS,
                    "Appointment already has an active replacement proposal awaiting patient response");
        }
        if (slotSnapshot == null) {
            throw new BusinessException(ErrorCode.PROPOSED_SLOT_NOT_FOUND,
                    "Proposed slot not found in scheduling service with ID: " + request.getProposedSlotId());
        }
    }

    public void validateReschedule(
            ReplacementProposal proposal,
            RescheduleProposalRequest request,
            UUID currentSlotId
    ) {
        if (proposal == null) {
            throw new BusinessException(ErrorCode.REPLACEMENT_PROPOSAL_NOT_FOUND, "Replacement proposal not found");
        }
        if (proposal.getStatus() != ReplacementProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION,
                    "Only PENDING proposals can be rescheduled (current status: " + proposal.getStatus() + ")");
        }
        if (request == null || request.getNewSlotId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "New Slot ID is required for rescheduling");
        }
        if (request.getNewSlotId().equals(currentSlotId)) {
            throw new BusinessException(ErrorCode.CANNOT_RESCHEDULE, "New slot must be different from current slot");
        }
    }
}

