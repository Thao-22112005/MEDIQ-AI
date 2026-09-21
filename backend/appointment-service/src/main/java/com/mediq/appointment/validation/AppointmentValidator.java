package com.mediq.appointment.validation;

import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AppointmentValidator {

    public void validateCreate(CreateAppointmentRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getPatientId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Patient ID is required");
        }
        if (request.getSlotId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Slot ID is required");
        }
    }

    public void validateCancel(CancelAppointmentRequest request) {
        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.CANCELLATION_REASON_REQUIRED,
                    "Cancellation reason is required when cancelling an appointment");
        }
    }

    public void validateReschedule(com.mediq.appointment.dto.request.RescheduleAppointmentRequest request, java.util.UUID currentSlotId) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getNewSlotId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "New Slot ID is required");
        }
        if (request.getNewSlotId().equals(currentSlotId)) {
            throw new BusinessException(ErrorCode.RESCHEDULE_SAME_SLOT,
                    "Cannot reschedule to the same slot");
        }
    }
}
