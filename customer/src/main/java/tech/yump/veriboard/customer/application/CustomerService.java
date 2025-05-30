package tech.yump.veriboard.customer.application;

import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerFraudException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.ports.NotificationService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;

/**
 * Application service for customer registration use case.
 * Orchestrates domain services and external ports to fulfill business requirements.
 */
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerValidationService validationService;
    private final FraudCheckService fraudCheckService;
    private final NotificationService notificationService;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerValidationService validationService,
            FraudCheckService fraudCheckService,
            NotificationService notificationService) {
        this.customerRepository = customerRepository;
        this.validationService = validationService;
        this.fraudCheckService = fraudCheckService;
        this.notificationService = notificationService;
    }

    /**
     * Registers a new customer following the complete business process.
     * 
     * @param request the customer registration request
     * @return the registered customer with assigned ID
     * @throws CustomerValidationException if validation fails
     * @throws CustomerFraudException if fraud is detected
     */
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        // Step 1: Validate the request according to business rules
        validationService.validateCustomerRegistration(request);
        
        // Step 2: Create and persist the customer
        Customer customer = request.toCustomer();
        Customer savedCustomer = customerRepository.save(customer);
        
        // Step 3: Perform fraud check on the saved customer
        boolean isFraudulent = fraudCheckService.isFraudulent(savedCustomer.getId());
        if (isFraudulent) {
            throw new CustomerFraudException("Customer registration blocked due to fraud detection");
        }
        
        // Step 4: Send welcome notification (best effort - don't fail registration)
        try {
            notificationService.sendWelcomeNotification(savedCustomer);
        } catch (Exception e) {
            // Log the error but don't fail the registration
            // In a real application, you might want to queue this for retry
            System.err.println("Failed to send welcome notification for customer " + 
                             savedCustomer.getId() + ": " + e.getMessage());
        }
        
        return savedCustomer;
    }
} 