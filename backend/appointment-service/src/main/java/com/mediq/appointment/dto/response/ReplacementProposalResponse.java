package com.mediq.appointment.dto.response;

import com.mediq.appointment.entity.ReplacementProposalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReplacementProposalResponse(
        UUID proposalId,
        UUID appointmentId,
        UUID originalDoctorId,
        UUID proposedDoctorId,
        UUID proposedClinicId,
        UUID proposedSpecialtyId,
        UUID proposedRoomId,
        UUID proposedSlotId,
        LocalDate proposedDate,
        LocalTime proposedStartTime,
        LocalTime proposedEndTime,
        ReplacementProposalStatus status,
        Instant expiresAt,
        Instant respondedAt,
        Instant createdAt,
        Instant updatedAt
) {}
