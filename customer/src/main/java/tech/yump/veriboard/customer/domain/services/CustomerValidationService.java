package tech.yump.veriboard.customer.domain.services;

import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain service for customer validation logic.
 * Contains pure business rules without external dependencies.
 */
public class CustomerValidationService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$"
    );
    
    private final CustomerRepository customerRepository;
    
    public CustomerValidationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * Validates a customer registration request according to business rules.
     * @param request the registration request to validate
     * @throws CustomerValidationException if validation fails
     */
    public void validateCustomerRegistration(CustomerRegistrationRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (isBlankOrNull(request.firstName())) {
            errors.add("First name is required");
        }
        
        if (isBlankOrNull(request.lastName())) {
            errors.add("Last name is required");
        }
        
        if (isBlankOrNull(request.email())) {
            errors.add("Email is required");
        } else {
            // Validate email format first
            if (!isValidEmailFormat(request.email())) {
                errors.add("Email format is invalid");
            } else {
                // Only check uniqueness if format is valid
                if (customerRepository.findByEmail(request.email()).isPresent()) {
                    errors.add("Email is already registered");
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new CustomerValidationException(String.join(", ", errors));
        }
    }
    
    private boolean isBlankOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private boolean isValidEmailFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
} 