package tech.yump.veriboard.customer.domain;

/**
 * Value object representing a customer registration request.
 * Contains the essential data needed to register a new customer.
 */
public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
    
    /**
     * Creates a Customer domain object from this request.
     * @return a new Customer instance
     */
    public Customer toCustomer() {
        return new Customer(firstName, lastName, email);
    }
} 