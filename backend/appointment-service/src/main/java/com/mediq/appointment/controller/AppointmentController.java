package com.mediq.appointment.controller;

import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentHistoryResponse;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable("id") UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listAppointments(
            @RequestParam(value = "patientId", required = false) UUID patientId,
            @RequestParam(value = "doctorId", required = false) UUID doctorId,
            @RequestParam(value = "clinicId", required = false) UUID clinicId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) AppointmentStatus status
    ) {
        return ResponseEntity.ok(appointmentService.getAppointments(patientId, doctorId, clinicId, date, status));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable("id") UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(appointmentId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable("id") UUID appointmentId,
            @RequestBody CancelAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId, request));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable("id") UUID appointmentId,
            @RequestBody com.mediq.appointment.dto.request.RescheduleAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(appointmentId, request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable("id") UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.completeAppointment(appointmentId));
    }

    @PostMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@PathVariable("id") UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.markNoShow(appointmentId));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AppointmentHistoryResponse>> getAppointmentHistory(@PathVariable("id") UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentHistory(appointmentId));
    }
}
