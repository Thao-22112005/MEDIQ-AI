package com.mediq.appointment.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SchedulingClientImplTest {

    private MockRestServiceServer mockServer;
    private SchedulingClientImpl schedulingClient;

    private static final String BASE_URL = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        schedulingClient = new SchedulingClientImpl(builder, BASE_URL);
    }

    @Test
    @DisplayName("getSlot returns SlotSnapshotDto when slot exists")
    void getSlot_Success() {
        UUID slotId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        String jsonResponse = String.format("""
                {
                    "slotId": "%s",
                    "scheduleId": "%s",
                    "doctorId": "%s",
                    "clinicId": "%s",
                    "specialtyId": "%s",
                    "roomId": "%s",
                    "date": "2026-09-25",
                    "startTime": "09:00:00",
                    "endTime": "09:30:00",
                    "status": "AVAILABLE"
                }
                """, slotId, scheduleId, doctorId, clinicId, specialtyId, roomId);

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<SchedulingClient.SlotSnapshotDto> result = schedulingClient.getSlot(slotId);

        assertThat(result).isPresent();
        SchedulingClient.SlotSnapshotDto dto = result.get();
        assertThat(dto.slotId()).isEqualTo(slotId);
        assertThat(dto.scheduleId()).isEqualTo(scheduleId);
        assertThat(dto.doctorId()).isEqualTo(doctorId);
        assertThat(dto.clinicId()).isEqualTo(clinicId);
        assertThat(dto.specialtyId()).isEqualTo(specialtyId);
        assertThat(dto.roomId()).isEqualTo(roomId);
        assertThat(dto.date()).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(dto.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(dto.endTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(dto.status()).isEqualTo("AVAILABLE");
        mockServer.verify();
    }

    @Test
    @DisplayName("getSlot returns empty when slot not found (404)")
    void getSlot_NotFound() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<SchedulingClient.SlotSnapshotDto> result = schedulingClient.getSlot(slotId);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("getSlot returns empty on server error (500)")
    void getSlot_ServerError() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Optional<SchedulingClient.SlotSnapshotDto> result = schedulingClient.getSlot(slotId);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("holdSlot returns true on 200 OK")
    void holdSlot_Success() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/hold"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        boolean result = schedulingClient.holdSlot(slotId);

        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("holdSlot returns false on 409 Conflict")
    void holdSlot_Conflict() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/hold"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        boolean result = schedulingClient.holdSlot(slotId);

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("bookSlot returns true on 200 OK")
    void bookSlot_Success() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/book"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        boolean result = schedulingClient.bookSlot(slotId);

        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("bookSlot returns false on failure")
    void bookSlot_Failure() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/book"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        boolean result = schedulingClient.bookSlot(slotId);

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("releaseSlot returns true on 200 OK")
    void releaseSlot_Success() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/release"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        boolean result = schedulingClient.releaseSlot(slotId);

        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("releaseSlot returns false when server error occurs")
    void releaseSlot_ServerError() {
        UUID slotId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/slots/" + slotId + "/release"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = schedulingClient.releaseSlot(slotId);

        assertThat(result).isFalse();
        mockServer.verify();
    }
}
