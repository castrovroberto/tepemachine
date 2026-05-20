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
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;
import tech.yump.veriboard.customer.infrastructure.outbox.OutboxEventPublisher;

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
    private OutboxEventPublisher outboxEventPublisher;

    @BeforeEach
    void setUp() {
        customerConfiguration = new CustomerConfiguration();
    }

    @Test
    @DisplayName("Should create CustomerValidationService bean")
    void shouldCreateCustomerValidationServiceBean() {
        CustomerValidationService validationService = customerConfiguration
                .customerValidationService(customerRepository);

        assertThat(validationService).isNotNull().isInstanceOf(CustomerValidationService.class);
    }

    @Test
    @DisplayName("Should create CustomerService bean with all dependencies")
    void shouldCreateCustomerServiceBeanWithAllDependencies() {
        CustomerValidationService validationService = customerConfiguration
                .customerValidationService(customerRepository);

        CustomerService customerService = customerConfiguration.customerService(
                customerRepository, validationService, fraudCheckService, outboxEventPublisher);

        assertThat(customerService).isNotNull().isInstanceOf(CustomerService.class);
    }

    @Test
    @DisplayName("Should create different instances for each call")
    void shouldCreateDifferentInstancesForEachCall() {
        CustomerValidationService vs1 = customerConfiguration.customerValidationService(customerRepository);
        CustomerValidationService vs2 = customerConfiguration.customerValidationService(customerRepository);

        assertThat(vs1).isNotSameAs(vs2);
    }

    @Test
    @DisplayName("Should create distinct CustomerService instances per call")
    void shouldCreateDistinctCustomerServiceInstances() {
        CustomerValidationService validationService = customerConfiguration
                .customerValidationService(customerRepository);

        CustomerService cs1 = customerConfiguration.customerService(
                customerRepository, validationService, fraudCheckService, outboxEventPublisher);
        CustomerService cs2 = customerConfiguration.customerService(
                customerRepository, validationService, fraudCheckService, outboxEventPublisher);

        assertThat(cs1).isNotNull();
        assertThat(cs2).isNotNull();
        assertThat(cs1).isNotSameAs(cs2);
    }
}
