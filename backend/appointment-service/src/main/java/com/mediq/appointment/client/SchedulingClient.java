package com.mediq.appointment.client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface SchedulingClient {

    record SlotSnapshotDto(
            UUID slotId,
            UUID scheduleId,
            UUID doctorId,
            UUID clinicId,
            UUID specialtyId,
            UUID roomId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String status
    ) {}

    Optional<SlotSnapshotDto> getSlot(UUID slotId);

    boolean holdSlot(UUID slotId);

    boolean bookSlot(UUID slotId);

    boolean releaseSlot(UUID slotId);
}
