package tech.yump.veriboard.customer.application.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.events.CustomerRegisteredEvent;
import tech.yump.veriboard.customer.domain.exceptions.CustomerFraudException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;
import tech.yump.veriboard.customer.infrastructure.outbox.OutboxEventPublisher;

import java.util.UUID;

/**
 * Orchestration-based Saga for customer registration process.
 * Coordinates the distributed transaction across Customer, Fraud, and Notification services.
 * 
 * Saga Steps:
 * 1. Validate customer data
 * 2. Save customer (compensatable)
 * 3. Check fraud (compensatable)
 * 4. Publish registration event (triggers notification)
 */
@Service
@Slf4j
public class CustomerRegistrationSaga {
    
    private final CustomerRepository customerRepository;
    private final CustomerValidationService validationService;
    private final FraudCheckService fraudCheckService;
    private final OutboxEventPublisher eventPublisher;
    
    public CustomerRegistrationSaga(
            CustomerRepository customerRepository,
            CustomerValidationService validationService,
            FraudCheckService fraudCheckService,
            OutboxEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.validationService = validationService;
        this.fraudCheckService = fraudCheckService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Executes the customer registration saga.
     * Uses database transactions to ensure consistency with outbox pattern.
     */
    @Transactional
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        String correlationId = UUID.randomUUID().toString();
        
        log.info("Starting customer registration saga with correlation ID: {}", correlationId);
        
        try {
            // Step 1: Validate customer data
            validationService.validateCustomerRegistration(request);
            
            // Step 2: Save customer (this can be compensated)
            Customer customer = request.toCustomer();
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer saved successfully: {}", savedCustomer.getId());
            
            // Step 3: Check fraud (synchronous for now, but with resilience)
            boolean isFraudulent = fraudCheckService.isFraudulent(savedCustomer.getId());
            if (isFraudulent) {
                // Compensate: mark customer as suspended instead of deleting
                compensateCustomerCreation(savedCustomer.getId(), correlationId);
                throw new CustomerFraudException(
                    "Customer registration blocked due to fraud detection");
            }
            
            // Step 4: Publish event for async notification processing
            CustomerRegisteredEvent event = new CustomerRegisteredEvent(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                correlationId
            );
            
            eventPublisher.publish(event);
            
            log.info("Customer registration saga completed successfully for customer: {}", 
                    savedCustomer.getId());
            
            return savedCustomer;
            
        } catch (Exception e) {
            log.error("Customer registration saga failed with correlation ID: {}. Error: {}", 
                     correlationId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Compensation logic for customer creation.
     * Instead of deleting, we mark as suspended for audit purposes.
     */
    private void compensateCustomerCreation(Integer customerId, String correlationId) {
        log.warn("Compensating customer creation for customer: {} (correlation: {})", 
                customerId, correlationId);
        
        // In a real implementation, you might:
        // 1. Mark customer as SUSPENDED
        // 2. Add compensation event to outbox
        // 3. Trigger cleanup processes
        
        // For now, log the compensation action
        log.info("Customer {} marked for compensation due to failed saga", customerId);
    }
} 