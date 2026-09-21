package com.mediq.appointment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
public class SchedulingClientImpl implements SchedulingClient {

    private final RestClient restClient;

    public SchedulingClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${services.scheduling-service.url:http://localhost:8082}") String schedulingServiceUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(schedulingServiceUrl).build();
    }

    @Override
    public Optional<SlotSnapshotDto> getSlot(UUID slotId) {
        try {
            SlotSnapshotDto slot = restClient.get()
                    .uri("/slots/{id}", slotId)
                    .retrieve()
                    .body(SlotSnapshotDto.class);
            return Optional.ofNullable(slot);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean holdSlot(UUID slotId) {
        try {
            restClient.post()
                    .uri("/slots/{id}/hold", slotId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean bookSlot(UUID slotId) {
        try {
            restClient.post()
                    .uri("/slots/{id}/book", slotId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean releaseSlot(UUID slotId) {
        try {
            restClient.post()
                    .uri("/slots/{id}/release", slotId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
