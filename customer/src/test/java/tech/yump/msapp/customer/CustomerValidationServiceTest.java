package tech.yump.msapp.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        validRequest = new CustomerRegistrationRequest(
            "John", 
            "Doe", 
            "john.doe@example.com"
        );
    }

    @Test
    @DisplayName("Should pass validation for valid customer data")
    void shouldPassValidationForValidCustomer() {
        // Given
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatNoException()
            .isThrownBy(() -> validationService.validateCustomerRegistration(validRequest));
        
        verify(customerRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw exception when firstName is blank")
    void shouldThrowExceptionWhenFirstNameIsBlank() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "Doe", "john.doe@example.com"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("First name is required");
    }

    @Test
    @DisplayName("Should throw exception when firstName is null")
    void shouldThrowExceptionWhenFirstNameIsNull() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            null, "Doe", "john.doe@example.com"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("First name is required");
    }

    @Test
    @DisplayName("Should throw exception when lastName is blank")
    void shouldThrowExceptionWhenLastNameIsBlank() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "", "john.doe@example.com"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Last name is required");
    }

    @Test
    @DisplayName("Should throw exception when lastName is null")
    void shouldThrowExceptionWhenLastNameIsNull() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", null, "john.doe@example.com"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Last name is required");
    }

    @Test
    @DisplayName("Should throw exception when email is blank")
    void shouldThrowExceptionWhenEmailIsBlank() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", ""
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Email is required");
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", null
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "missing@",
        "@missing-local.com",
        "missing-domain@.com",
        "missing-tld@domain",
        "spaces in@email.com",
        "double@@domain.com"
    })
    @DisplayName("Should throw exception for invalid email formats")
    void shouldThrowExceptionForInvalidEmailFormats(String invalidEmail) {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", invalidEmail
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Email format is invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user123@test123.io",
        "valid@domain.org"
    })
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats(String validEmail) {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", validEmail
        );
        when(customerRepository.findByEmail(validEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatNoException()
            .isThrownBy(() -> validationService.validateCustomerRegistration(request));
        
        verify(customerRepository).findByEmail(validEmail);
    }

    @Test
    @DisplayName("Should throw exception when email is already registered")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        Customer existingCustomer = Customer.builder()
            .id(1)
            .firstName("Jane")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
        
        when(customerRepository.findByEmail("john.doe@example.com"))
            .thenReturn(Optional.of(existingCustomer));

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(validRequest))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Email is already registered");
        
        verify(customerRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should collect multiple validation errors")
    void shouldCollectMultipleValidationErrors() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "", "invalid-email"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("First name is required")
            .hasMessageContaining("Last name is required")
            .hasMessageContaining("Email format is invalid");
    }

    @Test
    @DisplayName("Should not check email uniqueness if email format is invalid")
    void shouldNotCheckEmailUniquenessIfFormatIsInvalid() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "invalid-email"
        );

        // When & Then
        assertThatThrownBy(() -> validationService.validateCustomerRegistration(request))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessageContaining("Email format is invalid");
        
        // Repository should not be called for uniqueness check
        verify(customerRepository, never()).findByEmail(anyString());
    }
} 