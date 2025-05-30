package tech.yump.veriboard.customer.domain.exceptions;

/**
 * Domain exception for customer validation failures.
 * Thrown when customer data doesn't meet business rules.
 */
public class CustomerValidationException extends RuntimeException {
    
    public CustomerValidationException(String message) {
        super(message);
    }
    
    public CustomerValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 