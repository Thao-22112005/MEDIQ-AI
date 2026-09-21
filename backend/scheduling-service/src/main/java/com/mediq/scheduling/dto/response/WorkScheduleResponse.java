package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.WorkScheduleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record WorkScheduleResponse(
        UUID scheduleId,
        UUID doctorId,
        UUID clinicId,
        UUID specialtyId,
        UUID roomId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        WorkScheduleStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
