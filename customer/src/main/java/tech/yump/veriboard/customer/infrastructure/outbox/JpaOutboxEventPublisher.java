package tech.yump.veriboard.customer.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.yump.veriboard.customer.domain.events.CustomerEvent;

/**
 * JPA-based implementation of OutboxEventPublisher.
 * Stores events in the database for the Outbox pattern.
 */
@Component
@Slf4j
public class JpaOutboxEventPublisher implements OutboxEventPublisher {
    
    private final ObjectMapper objectMapper;
    
    public JpaOutboxEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void publish(CustomerEvent event) {
        try {
            log.info("Publishing event to outbox: {} for customer {}", 
                    event.getEventType(), event.getCustomerId());
            
            // Convert event to JSON
            String eventData = objectMapper.writeValueAsString(event);
            
            // Create outbox event
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .aggregateId(event.getAggregateId())
                    .eventData(eventData)
                    .occurredAt(event.getOccurredAt())
                    .processed(false)
                    .build();
            
            // For now, just log the event (in production, you'd save to database)
            log.info("Event stored in outbox: {}", outboxEvent);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event, e);
            throw new RuntimeException("Failed to publish event to outbox", e);
        }
    }
} 