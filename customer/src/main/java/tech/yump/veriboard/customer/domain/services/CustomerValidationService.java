package tech.yump.veriboard.customer.domain.services;

import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;

public class CustomerValidationService {

    private final CustomerRepository customerRepository;

    public CustomerValidationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Format and presence checks are enforced by Bean Validation on the DTO.
    // This service owns only business-level rules that require external state.
    public void validateCustomerRegistration(CustomerRegistrationRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomerValidationException("Email is already registered");
        }
    }
}