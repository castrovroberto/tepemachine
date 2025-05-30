package tech.yump.msapp.customer;

public class CustomerFraudException extends RuntimeException {
    
    public CustomerFraudException(String message) {
        super(message);
    }
    
    public CustomerFraudException(String message, Throwable cause) {
        super(message, cause);
    }
} 