package com.mediq.appointment.event.payload;

import java.time.Instant;
import java.util.UUID;

public record ReplacementProposalExpiredEventPayload(
        UUID proposalId,
        UUID appointmentId,
        Instant expiredAt
) {}
