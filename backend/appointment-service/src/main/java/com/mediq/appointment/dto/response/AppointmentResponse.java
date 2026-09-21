package com.mediq.appointment.dto.response;

import com.mediq.appointment.entity.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID appointmentId,
        UUID patientId,
        UUID slotId,
        UUID doctorId,
        UUID clinicId,
        UUID specialtyId,
        UUID roomId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {
}
