package com.mediq.appointment.event.payload;

import java.util.UUID;

public record AppointmentReassignedEventPayload(
        UUID appointmentId,
        UUID oldDoctorId,
        UUID newDoctorId,
        UUID oldClinicId,
        UUID newClinicId,
        UUID oldSlotId,
        UUID newSlotId
) {}
