package com.mediq.appointment.event.payload;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRescheduledEventPayload(
        UUID appointmentId,
        UUID oldSlotId,
        UUID newSlotId,
        LocalDate newDate,
        LocalTime newStartTime,
        LocalTime newEndTime
) {}
