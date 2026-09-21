package com.mediq.appointment.event.payload;

import java.util.UUID;

public record ReplacementProposalCancelledEventPayload(
        UUID proposalId,
        UUID appointmentId
) {}
