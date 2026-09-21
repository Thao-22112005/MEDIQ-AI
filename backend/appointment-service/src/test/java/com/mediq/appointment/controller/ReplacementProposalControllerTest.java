package com.mediq.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediq.appointment.dto.request.CreateReplacementProposalRequest;
import com.mediq.appointment.dto.request.RescheduleProposalRequest;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.entity.ReplacementProposalStatus;
import com.mediq.appointment.exception.GlobalExceptionHandler;
import com.mediq.appointment.service.ReplacementProposalService;
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
class ReplacementProposalControllerTest {

    @Mock
    private ReplacementProposalService replacementProposalService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        ReplacementProposalController controller = new ReplacementProposalController(replacementProposalService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /appointments/{id}/replacement-proposals creates proposal and returns 201 Created")
    void createProposal_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();
        UUID proposedSlotId = UUID.randomUUID();
        CreateReplacementProposalRequest request = new CreateReplacementProposalRequest(proposedSlotId);

        ReplacementProposalResponse response = new ReplacementProposalResponse(
                proposalId, appointmentId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), proposedSlotId,
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                ReplacementProposalStatus.PENDING, Instant.now().plusSeconds(86400),
                null, Instant.now(), Instant.now()
        );

        when(replacementProposalService.createProposal(eq(appointmentId), any(CreateReplacementProposalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/appointments/{id}/replacement-proposals", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proposalId").value(proposalId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /appointments/{id}/replacement-proposals/{proposalId} returns 200 OK")
    void getProposalById_Success() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();

        ReplacementProposalResponse response = new ReplacementProposalResponse(
                proposalId, appointmentId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                ReplacementProposalStatus.PENDING, Instant.now().plusSeconds(86400),
                null, Instant.now(), Instant.now()
        );

        when(replacementProposalService.getProposalById(appointmentId, proposalId)).thenReturn(response);

        mockMvc.perform(get("/appointments/{id}/replacement-proposals/{proposalId}", appointmentId, proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposalId").value(proposalId.toString()));
    }

    @Test
    @DisplayName("POST /replacement-proposals/{id}/accept accepts proposal and returns 200 OK")
    void acceptProposal_Success() throws Exception {
        UUID proposalId = UUID.randomUUID();

        ReplacementProposalResponse response = new ReplacementProposalResponse(
                proposalId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                ReplacementProposalStatus.ACCEPTED, Instant.now().plusSeconds(86400),
                Instant.now(), Instant.now(), Instant.now()
        );

        when(replacementProposalService.acceptProposal(proposalId)).thenReturn(response);

        mockMvc.perform(post("/replacement-proposals/{id}/accept", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("POST /replacement-proposals/{id}/reschedule reschedules proposal and returns 200 OK")
    void rescheduleProposal_Success() throws Exception {
        UUID proposalId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        RescheduleProposalRequest request = new RescheduleProposalRequest(newSlotId, "Prefer morning");

        ReplacementProposalResponse response = new ReplacementProposalResponse(
                proposalId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), newSlotId,
                LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(10, 30),
                ReplacementProposalStatus.RESCHEDULED, Instant.now().plusSeconds(86400),
                Instant.now(), Instant.now(), Instant.now()
        );

        when(replacementProposalService.rescheduleProposal(eq(proposalId), any(RescheduleProposalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/replacement-proposals/{id}/reschedule", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESCHEDULED"));
    }

    @Test
    @DisplayName("POST /replacement-proposals/{id}/cancel cancels proposal and returns 200 OK")
    void cancelProposal_Success() throws Exception {
        UUID proposalId = UUID.randomUUID();

        ReplacementProposalResponse response = new ReplacementProposalResponse(
                proposalId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                ReplacementProposalStatus.CANCELLED, Instant.now().plusSeconds(86400),
                Instant.now(), Instant.now(), Instant.now()
        );

        when(replacementProposalService.cancelProposal(proposalId)).thenReturn(response);

        mockMvc.perform(post("/replacement-proposals/{id}/cancel", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
