package tech.yump.veriboard.customer.application.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;

import java.util.List;
import java.util.Optional;

/**
 * CQRS Query Service for customer read operations.
 * Separates read concerns from write concerns for better scalability.
 * 
 * In a full CQRS implementation, this could use:
 * - Read-only database replicas
 * - Denormalized read models
 * - Eventual consistency from command side
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class CustomerQueryService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerQueryService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * Finds a customer by ID.
     * Optimized for read performance.
     */
    public Optional<Customer> findCustomerById(Integer customerId) {
        log.debug("Querying customer by ID: {}", customerId);
        return customerRepository.findById(customerId);
    }
    
    /**
     * Finds customers by email pattern.
     * Could be optimized with specialized indexes.
     */
    public List<Customer> findCustomersByEmailPattern(String emailPattern) {
        log.debug("Querying customers by email pattern: {}", emailPattern);
        // This would be implemented with proper repository method
        // return customerRepository.findByEmailContaining(emailPattern);
        return List.of(); // Placeholder
    }
    
    /**
     * Gets customer summary for dashboard views.
     * Example of a denormalized read model.
     */
    public CustomerSummary getCustomerSummary(Integer customerId) {
        log.debug("Getting customer summary for ID: {}", customerId);
        
        Optional<Customer> customer = customerRepository.findById(customerId);
        
        if (customer.isPresent()) {
            Customer c = customer.get();
            return CustomerSummary.builder()
                .customerId(c.getId())
                .fullName(c.getFirstName() + " " + c.getLastName())
                .email(c.getEmail())
                .registrationStatus("ACTIVE") // Could come from saga state
                .riskLevel("LOW") // Could come from fraud service
                .build();
        }
        
        return null;
    }
} 