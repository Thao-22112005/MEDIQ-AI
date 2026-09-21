package com.mediq.appointment.event;

public interface EventPublisher {
    <T> void publish(EventEnvelope<T> event);
}
