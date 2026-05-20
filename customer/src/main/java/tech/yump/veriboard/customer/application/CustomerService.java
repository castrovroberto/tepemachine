package tech.yump.veriboard.customer.application;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerValidationService validationService;
    private final FraudCheckService fraudCheckService;
    private final OutboxEventPublisher outboxEventPublisher;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerValidationService validationService,
            FraudCheckService fraudCheckService,
            OutboxEventPublisher outboxEventPublisher) {
        this.customerRepository = customerRepository;
        this.validationService = validationService;
        this.fraudCheckService = fraudCheckService;
        this.outboxEventPublisher = outboxEventPublisher;
    }

    @Transactional
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        validationService.validateCustomerRegistration(request);

        Customer savedCustomer = customerRepository.save(request.toCustomer());

        boolean isFraudulent = fraudCheckService.isFraudulent(savedCustomer.getId());
        if (isFraudulent) {
            throw new CustomerFraudException("Customer registration blocked due to fraud detection");
        }

        // Write to outbox in the same transaction — RabbitMQ publish happens async via poller
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                UUID.randomUUID().toString()
        );
        outboxEventPublisher.publish(event);

        log.info("Customer {} registered; outbox event queued for notification", savedCustomer.getId());
        return savedCustomer;
    }
} 