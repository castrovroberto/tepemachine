package tech.yump.msapp.customer;

import org.springframework.stereotype.Service;
import tech.yump.msapp.clients.fraud.FraudCheckResponse;
import tech.yump.msapp.clients.fraud.FraudClient;

@Service
public class RemoteFraudCheckService implements FraudCheckService {
    
    private final FraudClient fraudClient;
    
    public RemoteFraudCheckService(FraudClient fraudClient) {
        this.fraudClient = fraudClient;
    }
    
    @Override
    public boolean isFraudulent(Integer customerId) {
        FraudCheckResponse response = fraudClient.isFraudster(customerId);
        return response.isFraudster();
    }
} 