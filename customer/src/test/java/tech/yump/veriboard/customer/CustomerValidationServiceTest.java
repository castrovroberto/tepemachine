package tech.yump.veriboard.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;
import tech.yump.veriboard.customer.domain.services.CustomerValidationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Validation Service Tests")
class CustomerValidationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerValidationService validationService;

    private CustomerRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRegistrationRequest("John", "Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should pass validation when email is not taken")
    void shouldPassValidationWhenEmailIsAvailable() {
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        assertThatNoException()
                .isThrownBy(() -> validationService.validateCustomerRegistration(validRequest));

        verify(customerRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw exception when email is already registered")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        Customer existing = new Customer(1, "Jane", "Doe", "john.doe@example.com");
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validationService.validateCustomerRegistration(validRequest))
                .isInstanceOf(CustomerValidationException.class)
                .hasMessageContaining("Email is already registered");

        verify(customerRepository).findByEmail("john.doe@example.com");
    }
}
