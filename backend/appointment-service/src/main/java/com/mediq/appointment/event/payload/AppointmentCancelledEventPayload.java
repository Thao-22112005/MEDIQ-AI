package com.mediq.appointment.event.payload;

import java.util.UUID;

public record AppointmentCancelledEventPayload(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        String reason
) {}
