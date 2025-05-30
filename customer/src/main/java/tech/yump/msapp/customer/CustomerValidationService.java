package tech.yump.msapp.customer;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomerValidationService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$"
    );
    
    private final CustomerRepository customerRepository;
    
    public CustomerValidationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    public void validateCustomerRegistration(CustomerRegistrationRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (!StringUtils.hasText(request.firstName())) {
            errors.add("First name is required");
        }
        
        if (!StringUtils.hasText(request.lastName())) {
            errors.add("Last name is required");
        }
        
        if (!StringUtils.hasText(request.email())) {
            errors.add("Email is required");
        } else {
            // Validate email format first
            boolean isValidFormat = EMAIL_PATTERN.matcher(request.email()).matches();
            if (!isValidFormat) {
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
} 