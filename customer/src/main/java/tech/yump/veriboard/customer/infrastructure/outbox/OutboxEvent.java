package tech.yump.veriboard.customer.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbox event entity for implementing the Outbox pattern.
 * Ensures reliable event publishing by storing events in the same transaction as business data.
 */
@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    
    @Id
    private String eventId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false)
    private String aggregateId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;
    
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;
    
    private LocalDateTime processedAt;
    
    private String correlationId;
    
    @Version
    private Long version;
} 