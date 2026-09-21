package com.mediq.appointment.event.payload;

import java.util.UUID;

public record ReplacementProposalAcceptedEventPayload(
        UUID proposalId,
        UUID appointmentId,
        UUID replacementDoctorId,
        UUID newSlotId
) {}
