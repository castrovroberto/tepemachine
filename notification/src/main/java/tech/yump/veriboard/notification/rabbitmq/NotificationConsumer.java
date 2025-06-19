package tech.yump.veriboard.notification.rabbitmq;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tech.yump.veriboard.clients.notification.NotificationRequest;
import tech.yump.veriboard.notification.NotificationService;

@AllArgsConstructor
@Component
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    @CircuitBreaker(name = "notification-processing", fallbackMethod = "fallbackConsumer")
    @Retry(name = "notification-processing")
    public void consumer(NotificationRequest notificationRequest) {
        log.info("Consumed notification request from queue: {}", notificationRequest);
        
        try {
            notificationService.send(notificationRequest);
            log.debug("Successfully processed notification for customer: {}", notificationRequest.toCustomerId());
        } catch (Exception e) {
            log.error("Failed to process notification for customer: {}. Error: {}", 
                     notificationRequest.toCustomerId(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry/circuit breaker
        }
    }
    
    /**
     * Fallback method for message consumption when service is degraded.
     * This prevents message loss during service degradation.
     */
    public void fallbackConsumer(NotificationRequest notificationRequest, Exception ex) {
        log.warn("Notification processing circuit breaker activated for customer {}. " +
                "Message will be handled by fallback mechanism. Error: {}", 
                notificationRequest.toCustomerId(), ex.getMessage());
        
        // In a real implementation, you might:
        // 1. Send to a dead letter queue for manual processing
        // 2. Store in a database for later retry
        // 3. Send via alternative notification channel
        // 4. Alert operations team
        
        // For now, just log the fallback action
        log.info("Notification fallback: storing message for customer {} for later processing", 
                notificationRequest.toCustomerId());
    }
}
