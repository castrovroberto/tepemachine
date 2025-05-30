package tech.yump.msapp.customer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerValidationService validationService;
    private final FraudCheckService fraudCheckService;
    private final NotificationPublisher notificationPublisher;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerValidationService validationService,
            FraudCheckService fraudCheckService,
            NotificationPublisher notificationPublisher) {
        this.customerRepository = customerRepository;
        this.validationService = validationService;
        this.fraudCheckService = fraudCheckService;
        this.notificationPublisher = notificationPublisher;
    }

    public Customer registerCustomer(CustomerRegistrationRequest request) {
        log.info("Starting customer registration for email: {}", request.email());
        
        // Step 1: Validate the request
        validationService.validateCustomerRegistration(request);
        
        // Step 2: Create and save customer
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        
        Customer savedCustomer = customerRepository.saveAndFlush(customer);
        log.info("Customer saved with ID: {}", savedCustomer.getId());
        
        // Step 3: Perform fraud check
        boolean isFraudulent = fraudCheckService.isFraudulent(savedCustomer.getId());
        if (isFraudulent) {
            // In a real system, you might want to mark the customer as suspicious
            // rather than throwing an exception, but for this demo we'll throw
            log.warn("Fraud detected for customer ID: {}", savedCustomer.getId());
            throw new CustomerFraudException("Customer registration blocked due to fraud detection");
        }
        
        // Step 4: Send welcome notification
        try {
            notificationPublisher.publishWelcomeNotification(savedCustomer);
            log.info("Welcome notification sent for customer ID: {}", savedCustomer.getId());
        } catch (Exception e) {
            // Log the error but don't fail the registration
            log.error("Failed to send welcome notification for customer ID: {}", 
                     savedCustomer.getId(), e);
        }
        
        return savedCustomer;
    }
}
