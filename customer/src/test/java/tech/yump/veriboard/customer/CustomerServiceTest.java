package tech.yump.veriboard.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.customer.application.CustomerService;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.events.CustomerRegisteredEvent;
import tech.yump.veriboard.customer.domain.exceptions.CustomerFraudException;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;
import tech.yump.veriboard.customer.infrastructure.outbox.OutboxEventPublisher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerValidationService validationService;

    @Mock
    private FraudCheckService fraudCheckService;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRegistrationRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRegistrationRequest("John", "Doe", "john.doe@example.com");
        savedCustomer = new Customer(1, "John", "Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should successfully register customer and write outbox event")
    void shouldSuccessfullyRegisterCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        doNothing().when(outboxEventPublisher).publish(any(CustomerRegisteredEvent.class));

        Customer result = customerService.registerCustomer(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository).save(any(Customer.class));
        verify(fraudCheckService).isFraudulent(savedCustomer.getId());
        verify(outboxEventPublisher).publish(any(CustomerRegisteredEvent.class));
    }

    @Test
    @DisplayName("Should throw CustomerValidationException when validation fails")
    void shouldThrowExceptionWhenValidationFails() {
        doThrow(new CustomerValidationException("Email is required"))
                .when(validationService).validateCustomerRegistration(validRequest);

        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
                .isInstanceOf(CustomerValidationException.class)
                .hasMessage("Email is required");

        verify(customerRepository, never()).save(any());
        verify(fraudCheckService, never()).isFraudulent(any());
        verify(outboxEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw CustomerFraudException when fraud is detected")
    void shouldThrowExceptionWhenFraudDetected() {
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(true);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);

        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
                .isInstanceOf(CustomerFraudException.class)
                .hasMessage("Customer registration blocked due to fraud detection");

        verify(outboxEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should propagate repository failure")
    void shouldHandleRepositoryFailure() {
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        when(customerRepository.save(any(Customer.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(fraudCheckService, never()).isFraudulent(any());
        verify(outboxEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should build customer object correctly from request")
    void shouldBuildCustomerObjectCorrectly() {
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);

        customerService.registerCustomer(validRequest);

        verify(customerRepository).save(argThat(customer ->
                customer.getFirstName().equals("John") &&
                customer.getLastName().equals("Doe") &&
                customer.getEmail().equals("john.doe@example.com") &&
                customer.getId() == null
        ));
    }

    @Test
    @DisplayName("Should publish outbox event with correct customer data")
    void shouldPublishOutboxEventWithCorrectCustomerData() {
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);

        customerService.registerCustomer(validRequest);

        verify(outboxEventPublisher).publish(argThat(event ->
                event instanceof CustomerRegisteredEvent e &&
                e.getCustomerId().equals(1) &&
                e.getEmail().equals("john.doe@example.com") &&
                e.getFirstName().equals("John")
        ));
    }
}
