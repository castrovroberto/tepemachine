package tech.yump.veriboard.customer.domain.ports;

/**
 * Port interface for fraud checking operations.
 * This defines the contract for external fraud detection services.
 */
public interface FraudCheckService {
    
    /**
     * Checks if a customer ID is associated with fraudulent activity.
     * @param customerId the customer ID to check
     * @return true if fraudulent, false otherwise
     */
    boolean isFraudulent(Integer customerId);
} 