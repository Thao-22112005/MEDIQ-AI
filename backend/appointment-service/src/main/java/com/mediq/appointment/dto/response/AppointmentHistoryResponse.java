package com.mediq.appointment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AppointmentHistoryResponse(
        UUID historyId,
        UUID appointmentId,
        String action,
        String oldValue,
        String newValue,
        String changedBy,
        Instant changedAt
) {
}
