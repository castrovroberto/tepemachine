package tech.yump.veriboard.customer.domain.ports;

import tech.yump.veriboard.customer.domain.Customer;

/**
 * Port interface for notification operations.
 * This defines the contract for sending notifications to customers.
 */
public interface NotificationService {
    
    /**
     * Sends a welcome notification to a newly registered customer.
     * @param customer the customer to send the notification to
     */
    void sendWelcomeNotification(Customer customer);
} 