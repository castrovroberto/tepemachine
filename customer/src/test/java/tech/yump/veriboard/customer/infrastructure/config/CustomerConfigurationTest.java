package tech.yump.veriboard.customer.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.customer.application.CustomerService;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.ports.FraudCheckService;
import tech.yump.veriboard.customer.domain.ports.NotificationService;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Configuration Tests")
class CustomerConfigurationTest {

    private CustomerConfiguration customerConfiguration;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FraudCheckService fraudCheckService;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        customerConfiguration = new CustomerConfiguration();
    }

    @Test
    @DisplayName("Should create CustomerValidationService bean")
    void shouldCreateCustomerValidationServiceBean() {
        // When
        CustomerValidationService validationService = customerConfiguration
            .customerValidationService(customerRepository);

        // Then
        assertThat(validationService).isNotNull();
        assertThat(validationService).isInstanceOf(CustomerValidationService.class);
    }

    @Test
    @DisplayName("Should create CustomerService bean with all dependencies")
    void shouldCreateCustomerServiceBeanWithAllDependencies() {
        // Given
        CustomerValidationService validationService = customerConfiguration
            .customerValidationService(customerRepository);

        // When
        CustomerService customerService = customerConfiguration.customerService(
            customerRepository,
            validationService,
            fraudCheckService,
            notificationService
        );

        // Then
        assertThat(customerService).isNotNull();
        assertThat(customerService).isInstanceOf(CustomerService.class);
    }

    @Test
    @DisplayName("Should create different instances for each call")
    void shouldCreateDifferentInstancesForEachCall() {
        // When
        CustomerValidationService validationService1 = customerConfiguration
            .customerValidationService(customerRepository);
        CustomerValidationService validationService2 = customerConfiguration
            .customerValidationService(customerRepository);

        // Then
        assertThat(validationService1).isNotSameAs(validationService2);
    }

    @Test
    @DisplayName("Should create CustomerService with proper configuration")
    void shouldCreateCustomerServiceWithProperConfiguration() {
        // Given
        CustomerValidationService validationService = customerConfiguration
            .customerValidationService(customerRepository);

        // When
        CustomerService customerService1 = customerConfiguration.customerService(
            customerRepository, validationService, fraudCheckService, notificationService);
        CustomerService customerService2 = customerConfiguration.customerService(
            customerRepository, validationService, fraudCheckService, notificationService);

        // Then
        assertThat(customerService1).isNotNull();
        assertThat(customerService2).isNotNull();
        assertThat(customerService1).isNotSameAs(customerService2); // Each call creates new instance
    }
} 