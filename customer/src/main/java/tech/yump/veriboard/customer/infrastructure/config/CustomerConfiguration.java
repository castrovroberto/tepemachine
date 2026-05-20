package tech.yump.veriboard.customer.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.yump.veriboard.customer.application.CustomerService;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;
import tech.yump.veriboard.customer.infrastructure.outbox.OutboxEventPublisher;

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
            OutboxEventPublisher outboxEventPublisher) {

        return new CustomerService(
                customerRepository,
                validationService,
                fraudCheckService,
                outboxEventPublisher);
    }
} 