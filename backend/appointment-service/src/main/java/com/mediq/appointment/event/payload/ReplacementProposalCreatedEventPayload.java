package com.mediq.appointment.event.payload;

import java.time.Instant;
import java.util.UUID;

public record ReplacementProposalCreatedEventPayload(
        UUID proposalId,
        UUID appointmentId,
        UUID affectedDoctorId,
        UUID proposedDoctorId,
        UUID proposedSlotId,
        Instant expiresAt
) {}
