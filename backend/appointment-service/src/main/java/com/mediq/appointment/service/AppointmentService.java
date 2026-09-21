package com.mediq.appointment.service;

import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentHistoryResponse;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    AppointmentResponse getAppointmentById(UUID appointmentId);

    List<AppointmentResponse> getAppointments(UUID patientId, UUID doctorId, UUID clinicId, LocalDate date, AppointmentStatus status);

    AppointmentResponse confirmAppointment(UUID appointmentId);

    AppointmentResponse cancelAppointment(UUID appointmentId, CancelAppointmentRequest request);

    AppointmentResponse rescheduleAppointment(UUID appointmentId, com.mediq.appointment.dto.request.RescheduleAppointmentRequest request);

    AppointmentResponse completeAppointment(UUID appointmentId);

    AppointmentResponse markNoShow(UUID appointmentId);

    List<AppointmentHistoryResponse> getAppointmentHistory(UUID appointmentId);
}
