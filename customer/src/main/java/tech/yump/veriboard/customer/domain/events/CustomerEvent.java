package tech.yump.veriboard.customer.domain.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all customer domain events.
 * Implements the Domain Event pattern for event-driven architecture.
 */
@Data
@EqualsAndHashCode
public abstract class CustomerEvent {
    
    private final String eventId;
    private final String eventType;
    private final LocalDateTime occurredAt;
    private final Integer customerId;
    
    protected CustomerEvent(String eventType, Integer customerId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.customerId = customerId;
        this.occurredAt = LocalDateTime.now();
    }
    
    /**
     * Returns the aggregate root ID (customer ID) for this event.
     */
    public String getAggregateId() {
        return customerId != null ? customerId.toString() : null;
    }
} 