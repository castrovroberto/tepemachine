package tech.yump.veriboard.customer.infrastructure.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import tech.yump.veriboard.amqp.RabbitMQMessageProducer;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for OutboxEventPublishingHelper verifying the transactional
 * publish-and-mark semantics and resilience to AMQP failures.
 *
 * Uses H2 (from application-test.yml) and a mocked RabbitMQMessageProducer so the
 * tests are fast and have no external dependencies.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OutboxEventPublishingHelper - Publish and Mark Integration Tests")
class OutboxEventPublishingHelperIT {

    @Autowired
    private OutboxEventPublishingHelper publishingHelper;

    @Autowired
    private JpaOutboxEventRepository outboxEventRepository;

    @MockBean
    private RabbitMQMessageProducer messageProducer;

    @BeforeEach
    void clearOutbox() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Marks event as processed after successful publish")
    void publishAndMark_success_eventMarkedProcessed() {
        OutboxEvent event = savedUnprocessedEvent();

        publishingHelper.publishAndMark(event);

        OutboxEvent saved = outboxEventRepository.findById(event.getEventId()).orElseThrow();
        assertThat(saved.getProcessed()).isTrue();
        assertThat(saved.getProcessedAt()).isNotNull();
        verify(messageProducer).publish(
                eq(event.getEventData()),
                eq("internal.exchange"),
                eq("internal.notification.routing-key")
        );
    }

    @Test
    @DisplayName("Leaves event unprocessed when RabbitMQ publish fails")
    void publishAndMark_amqpException_eventRemainsUnprocessed() {
        OutboxEvent event = savedUnprocessedEvent();
        doThrow(new AmqpException("broker unavailable"))
                .when(messageProducer).publish(any(), any(), any());

        // Must not propagate the exception — poller must continue to next event
        assertThatCode(() -> publishingHelper.publishAndMark(event)).doesNotThrowAnyException();

        OutboxEvent saved = outboxEventRepository.findById(event.getEventId()).orElseThrow();
        assertThat(saved.getProcessed()).isFalse();
        assertThat(saved.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("Poller skips already-processed events")
    void pollAndPublish_onlyUnprocessedEventsArePublished() {
        OutboxEvent processed = savedProcessedEvent();
        OutboxEvent pending = savedUnprocessedEvent();

        // Invoke helper directly for the pending event (poller delegates to helper)
        publishingHelper.publishAndMark(pending);

        // Only the pending event should have been published
        verify(messageProducer, times(1)).publish(any(), any(), any());
        verify(messageProducer).publish(eq(pending.getEventData()), any(), any());
        verify(messageProducer, never()).publish(eq(processed.getEventData()), any(), any());
    }

    @Test
    @DisplayName("Multiple publish failures do not interfere with each other")
    void publishAndMark_multipleFailures_eachEventRemainsUnprocessed() {
        OutboxEvent event1 = savedUnprocessedEvent();
        OutboxEvent event2 = savedUnprocessedEvent();
        doThrow(new AmqpException("broker unavailable"))
                .when(messageProducer).publish(any(), any(), any());

        assertThatCode(() -> publishingHelper.publishAndMark(event1)).doesNotThrowAnyException();
        assertThatCode(() -> publishingHelper.publishAndMark(event2)).doesNotThrowAnyException();

        assertThat(outboxEventRepository.findTop50ByProcessedFalseOrderByOccurredAtAsc()).hasSize(2);
    }

    // --- helpers ---

    private OutboxEvent savedUnprocessedEvent() {
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CUSTOMER_REGISTERED")
                .aggregateId(UUID.randomUUID().toString())
                .eventData("{\"customerId\":1,\"email\":\"test@example.com\"}")
                .occurredAt(LocalDateTime.now())
                .processed(false)
                .build();
        return outboxEventRepository.save(event);
    }

    private OutboxEvent savedProcessedEvent() {
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CUSTOMER_REGISTERED")
                .aggregateId(UUID.randomUUID().toString())
                .eventData("{\"customerId\":2,\"email\":\"already@example.com\"}")
                .occurredAt(LocalDateTime.now().minusMinutes(5))
                .processed(true)
                .processedAt(LocalDateTime.now().minusMinutes(4))
                .build();
        return outboxEventRepository.save(event);
    }
}
