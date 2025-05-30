package tech.yump.veriboard.customer.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.yump.veriboard.customer.application.CustomerService;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.ports.NotificationService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;

/**
 * Configuration class that wires together the hexagonal architecture components.
 * This is where the dependency inversion principle is applied:
 * - Domain layer defines interfaces (ports)
 * - Infrastructure layer provides implementations (adapters)
 * - Configuration layer wires them together
 */
@Configuration
public class CustomerConfiguration {

    @Bean
    public CustomerValidationService customerValidationService(CustomerRepository customerRepository) {
        return new CustomerValidationService(customerRepository);
    }

    @Bean
    public CustomerService customerService(
            CustomerRepository customerRepository,
            CustomerValidationService validationService,
            FraudCheckService fraudCheckService,
            NotificationService notificationService) {
        
        return new CustomerService(
                customerRepository,
                validationService,
                fraudCheckService,
                notificationService);
    }
} 