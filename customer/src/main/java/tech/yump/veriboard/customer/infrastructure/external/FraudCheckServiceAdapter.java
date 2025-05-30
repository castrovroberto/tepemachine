package tech.yump.veriboard.customer.infrastructure.external;

import org.springframework.stereotype.Component;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;
import tech.yump.veriboard.clients.fraud.FraudClient;

/**
 * Adapter that implements the domain FraudCheckService port using Feign client.
 * This bridges the domain layer with external fraud detection service.
 */
@Component
public class FraudCheckServiceAdapter implements FraudCheckService {
    
    private final FraudClient fraudClient;
    
    public FraudCheckServiceAdapter(FraudClient fraudClient) {
        this.fraudClient = fraudClient;
    }
    
    @Override
    public boolean isFraudulent(Integer customerId) {
        FraudCheckResponse response = fraudClient.isFraudster(customerId);
        return response.isFraudster();
    }
} 