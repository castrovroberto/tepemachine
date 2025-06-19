package tech.yump.veriboard.notification;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "api/v1/notification")
@AllArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @CircuitBreaker(name = "notification-processing", fallbackMethod = "fallbackNotification")
    @Retry(name = "notification-processing")
    @TimeLimiter(name = "notification-processing")
    public CompletableFuture<Void> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        log.info("Processing notification request: {}", notificationRequest);
        
        return CompletableFuture.runAsync(() -> {
            notificationService.send(notificationRequest);
            log.debug("Notification processed successfully for customer: {}", notificationRequest.toCustomerId());
        });
    }
    
    /**
     * Fallback method for notification processing when service is degraded.
     * Logs the failure and provides graceful degradation.
     */
    public CompletableFuture<Void> fallbackNotification(NotificationRequest notificationRequest, Exception ex) {
        log.warn("Notification service degraded for customer {}. Notification will be queued for retry. Error: {}", 
                notificationRequest.toCustomerId(), ex.getMessage());
        
        // In a real implementation, you might:
        // 1. Store the notification in a dead letter queue
        // 2. Send to an alternative notification channel
        // 3. Schedule for later retry
        
        return CompletableFuture.completedFuture(null);
    }
}
