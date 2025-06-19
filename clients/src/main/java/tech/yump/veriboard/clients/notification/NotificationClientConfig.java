package tech.yump.veriboard.clients.notification;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration for Notification service.
 * Provides timeout configurations for reliable communication.
 */
@Configuration
public class NotificationClientConfig {

    /**
     * Configure timeouts for the Notification client.
     * Notifications are typically less time-sensitive than fraud checks.
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
            3000,  // connect timeout (3 seconds)
            8000   // read timeout (8 seconds)
        );
    }
} 