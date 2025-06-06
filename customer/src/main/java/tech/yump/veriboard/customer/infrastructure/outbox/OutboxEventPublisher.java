package tech.yump.veriboard.customer.infrastructure.outbox;

import tech.yump.veriboard.customer.domain.events.CustomerEvent;

/**
 * Publisher interface for outbox events.
 * Implements the Outbox pattern for reliable event publishing.
 */
public interface OutboxEventPublisher {
    
    /**
     * Publishes a domain event to the outbox.
     * Events are stored in the same transaction as business data.
     */
    void publish(CustomerEvent event);
} 