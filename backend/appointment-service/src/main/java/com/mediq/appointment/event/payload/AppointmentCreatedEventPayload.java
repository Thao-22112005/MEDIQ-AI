package com.mediq.appointment.event.payload;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentCreatedEventPayload(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID clinicId,
        UUID slotId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime
) {}
