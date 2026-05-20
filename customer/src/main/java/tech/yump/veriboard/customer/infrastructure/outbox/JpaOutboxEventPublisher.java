package tech.yump.veriboard.customer.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.yump.veriboard.customer.domain.events.CustomerEvent;

@Component
@Slf4j
public class JpaOutboxEventPublisher implements OutboxEventPublisher {

    private final JpaOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public JpaOutboxEventPublisher(JpaOutboxEventRepository outboxEventRepository,
                                   ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CustomerEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .aggregateId(event.getAggregateId())
                    .eventData(eventData)
                    .occurredAt(event.getOccurredAt())
                    .processed(false)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox event persisted: type={} customerId={}", event.getEventType(), event.getCustomerId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event for customer {}", event.getCustomerId(), e);
            throw new RuntimeException("Failed to write event to outbox", e);
        }
    }
} 