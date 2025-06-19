package tech.yump.veriboard.clients.fraud;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration for Fraud service.
 * Provides timeout configurations and can be extended with Resilience4j patterns.
 */
@Configuration
public class FraudClientConfig {

    /**
     * Configure timeouts for the Fraud client.
     * These work in conjunction with Resilience4j patterns applied at the service layer.
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
            5000,  // connect timeout (5 seconds)
            10000  // read timeout (10 seconds)
        );
    }
} 