package tech.yump.veriboard.customer.infrastructure.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;
import tech.yump.veriboard.clients.fraud.FraudClient;

import java.util.concurrent.CompletableFuture;

/**
 * Resilient adapter implementing the fraud check service with multiple resilience patterns:
 * - Circuit Breaker: Prevents cascading failures
 * - Retry: Handles transient failures
 * - Time Limiter: Prevents hanging requests
 * - Fallback: Provides graceful degradation
 */
@Component
@Primary
@Slf4j
public class ResilientFraudCheckServiceAdapter implements FraudCheckService {
    
    private final FraudClient fraudClient;
    
    public ResilientFraudCheckServiceAdapter(FraudClient fraudClient) {
        this.fraudClient = fraudClient;
    }
    
    @Override
    @CircuitBreaker(name = "fraud-service", fallbackMethod = "fallbackFraudCheck")
    @Retry(name = "fraud-service")
    @TimeLimiter(name = "fraud-service")
    public boolean isFraudulent(Integer customerId) {
        log.debug("Checking fraud for customer: {}", customerId);
        
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            FraudCheckResponse response = fraudClient.isFraudster(customerId);
            boolean isFraudulent = response.isFraudster();
            log.debug("Fraud check result for customer {}: {}", customerId, isFraudulent);
            return isFraudulent;
        });
        
        return future.join();
    }
    
    /**
     * Fallback method when fraud service is unavailable.
     * Implements risk-based assessment for graceful degradation.
     */
    public boolean fallbackFraudCheck(Integer customerId, Exception ex) {
        log.warn("Fraud service unavailable for customer {}. Using fallback assessment. Error: {}", 
                customerId, ex.getMessage());
        
        // Implement risk-based fallback logic
        // For demo purposes, treating as low-risk for known customer patterns
        boolean isHighRisk = isHighRiskCustomerId(customerId);
        
        log.info("Fallback fraud assessment for customer {}: {}", customerId, 
                isHighRisk ? "HIGH_RISK" : "LOW_RISK");
        
        return isHighRisk;
    }
    
    /**
     * Simple heuristic for risk assessment during fallback.
     * In production, this could check customer patterns, location, etc.
     */
    private boolean isHighRiskCustomerId(Integer customerId) {
        // Example: Flag customers with suspicious ID patterns
        return customerId != null && (customerId < 0 || customerId > 999999);
    }
} 