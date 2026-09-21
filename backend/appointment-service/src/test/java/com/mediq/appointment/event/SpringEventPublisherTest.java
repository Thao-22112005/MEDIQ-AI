package com.mediq.appointment.event;

import com.mediq.appointment.event.payload.AppointmentCreatedEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SpringEventPublisher springEventPublisher;

    @Test
    @DisplayName("Should publish EventEnvelope matching standard format (D023)")
    void shouldPublishEventEnvelope() {
        UUID appointmentId = UUID.randomUUID();
        AppointmentCreatedEventPayload payload = new AppointmentCreatedEventPayload(
                appointmentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30)
        );

        EventEnvelope<AppointmentCreatedEventPayload> event = EventEnvelope.of(
                "Appointment.Created",
                "appointment-service",
                payload
        );

        springEventPublisher.publish(event);

        verify(applicationEventPublisher).publishEvent(argThat((EventEnvelope<?> e) -> {
            assertThat(e.eventId()).isNotNull();
            assertThat(e.eventType()).isEqualTo("Appointment.Created");
            assertThat(e.source()).isEqualTo("appointment-service");
            assertThat(e.version()).isEqualTo(1);
            assertThat(e.occurredAt()).isNotNull();
            assertThat(e.payload()).isEqualTo(payload);
            return true;
        }));
    }
}
