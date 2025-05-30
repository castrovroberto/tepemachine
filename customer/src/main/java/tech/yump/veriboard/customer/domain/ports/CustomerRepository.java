package tech.yump.veriboard.customer.domain.ports;

import tech.yump.veriboard.customer.domain.Customer;

import java.util.Optional;

/**
 * Port interface for customer persistence operations.
 * This defines the contract that infrastructure adapters must implement.
 */
public interface CustomerRepository {
    
    /**
     * Saves a customer and returns the saved instance with assigned ID.
     * @param customer the customer to save
     * @return the saved customer with ID
     */
    Customer save(Customer customer);
    
    /**
     * Finds a customer by email address.
     * @param email the email to search for
     * @return Optional containing the customer if found, empty otherwise
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Finds a customer by ID.
     * @param id the customer ID
     * @return Optional containing the customer if found, empty otherwise
     */
    Optional<Customer> findById(Integer id);
} 