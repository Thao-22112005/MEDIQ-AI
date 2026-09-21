package com.mediq.appointment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Appointment not found"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid appointment status transition"),
    SLOT_HOLD_FAILED(HttpStatus.CONFLICT, "Failed to hold slot in scheduling service"),
    SLOT_BOOK_FAILED(HttpStatus.CONFLICT, "Failed to book slot in scheduling service"),
    CANCELLATION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "Cancellation reason is required when cancelling an appointment"),
    RESCHEDULE_SAME_SLOT(HttpStatus.BAD_REQUEST, "Cannot reschedule to the same slot"),
    CANNOT_RESCHEDULE(HttpStatus.CONFLICT, "Appointment cannot be rescheduled in its current status"),
    REPLACEMENT_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Replacement proposal not found"),
    ACTIVE_PROPOSAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Appointment already has an active replacement proposal pending response"),
    PROPOSED_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "Proposed slot not found in scheduling service"),
    PROPOSED_SLOT_NOT_AVAILABLE(HttpStatus.CONFLICT, "Proposed slot is no longer available in scheduling service"),
    PROPOSAL_EXPIRED(HttpStatus.BAD_REQUEST, "Replacement proposal has expired"),
    INVALID_PROPOSAL_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid replacement proposal status transition"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
