package tech.yump.veriboard.customer.domain.exceptions;

/**
 * Domain exception for fraud detection.
 * Thrown when a customer registration is blocked due to fraud.
 */
public class CustomerFraudException extends RuntimeException {
    
    public CustomerFraudException(String message) {
        super(message);
    }
    
    public CustomerFraudException(String message, Throwable cause) {
        super(message, cause);
    }
} 