package com.mediq.appointment.event.payload;

import java.time.LocalDate;
import java.util.UUID;

public record AppointmentConfirmedEventPayload(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID clinicId,
        UUID slotId,
        LocalDate appointmentDate
) {}
