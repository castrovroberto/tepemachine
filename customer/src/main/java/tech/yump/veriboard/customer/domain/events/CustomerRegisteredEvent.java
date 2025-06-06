package tech.yump.veriboard.customer.domain.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Domain event published when a customer is successfully registered.
 * Used for event-driven processing of notifications and other side effects.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class CustomerRegisteredEvent extends CustomerEvent {
    
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String correlationId;
    
    public CustomerRegisteredEvent(Integer customerId, String firstName, String lastName, 
                                 String email, String correlationId) {
        super("CUSTOMER_REGISTERED", customerId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.correlationId = correlationId;
    }
} 