package com.mediq.appointment.event.payload;

import java.util.UUID;

public record ReplacementProposalRescheduledEventPayload(
        UUID proposalId,
        UUID appointmentId,
        UUID newSlotId
) {}
