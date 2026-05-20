package tech.yump.veriboard.customer.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.yump.veriboard.amqp.RabbitMQMessageProducer;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublishingHelper {

    private final JpaOutboxEventRepository outboxEventRepository;
    private final RabbitMQMessageProducer messageProducer;

    private static final String EXCHANGE = "internal.exchange";
    private static final String ROUTING_KEY = "internal.notification.routing-key";

    // REQUIRES_NEW ensures each event commits independently; a failure here does not roll back siblings.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishAndMark(OutboxEvent event) {
        try {
            messageProducer.publish(event.getEventData(), EXCHANGE, ROUTING_KEY);
            event.setProcessed(true);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
        } catch (AmqpException e) {
            log.error("Failed to publish outbox event {} to RabbitMQ; will retry on next poll", event.getEventId(), e);
        }
    }
}
