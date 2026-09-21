package com.mediq.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediq.appointment.dto.request.CancelAppointmentRequest;
import com.mediq.appointment.dto.request.CreateAppointmentRequest;
import com.mediq.appointment.dto.request.RescheduleAppointmentRequest;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.exception.GlobalExceptionHandler;
import com.mediq.appointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        AppointmentController controller = new AppointmentController(appointmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /appointments creates appointment and returns 201 Created")
    void createAppointment_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        CreateAppointmentRequest request = new CreateAppointmentRequest(patientId, slotId);

        AppointmentResponse response = new AppointmentResponse(
                appointmentId, patientId, slotId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.PENDING,
                null, Instant.now(), Instant.now()
        );

        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /appointments/{id} returns 200 OK")
    void getAppointmentById_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse response = new AppointmentResponse(
                appointmentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.CONFIRMED,
                null, Instant.now(), Instant.now()
        );

        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(response);

        mockMvc.perform(get("/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /appointments/{id}/confirm confirms appointment and returns 200 OK")
    void confirmAppointment_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse response = new AppointmentResponse(
                appointmentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.CONFIRMED,
                null, Instant.now(), Instant.now()
        );

        when(appointmentService.confirmAppointment(appointmentId)).thenReturn(response);

        mockMvc.perform(post("/appointments/{id}/confirm", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /appointments/{id}/cancel cancels appointment and returns 200 OK")
    void cancelAppointment_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        CancelAppointmentRequest request = new CancelAppointmentRequest("Schedule conflict");

        AppointmentResponse response = new AppointmentResponse(
                appointmentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.CANCELLED,
                "Schedule conflict", Instant.now(), Instant.now()
        );

        when(appointmentService.cancelAppointment(eq(appointmentId), any(CancelAppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /appointments/{id}/reschedule reschedules appointment and returns 200 OK")
    void rescheduleAppointment_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(newSlotId, "Doctor suggested", "PATIENT");

        AppointmentResponse response = new AppointmentResponse(
                appointmentId, UUID.randomUUID(), newSlotId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.CONFIRMED,
                null, Instant.now(), Instant.now()
        );

        when(appointmentService.rescheduleAppointment(eq(appointmentId), any(RescheduleAppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/appointments/{id}/reschedule", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(newSlotId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
