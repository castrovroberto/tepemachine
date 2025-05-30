package tech.yump.msapp.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private NotificationPublisher notificationPublisher;
    
    @InjectMocks
    private CustomerService customerService;
    
    private CustomerRegistrationRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRegistrationRequest(
            "John", 
            "Doe", 
            "john.doe@example.com"
        );
        
        savedCustomer = Customer.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
    }

    @Test
    @DisplayName("Should successfully register customer when all validations pass")
    void shouldRegisterCustomerSuccessfully() {
        // Given
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        doNothing().when(notificationPublisher).publishWelcomeNotification(savedCustomer);

        // When
        Customer result = customerService.registerCustomer(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository).saveAndFlush(any(Customer.class));
        verify(fraudCheckService).isFraudulent(savedCustomer.getId());
        verify(notificationPublisher).publishWelcomeNotification(savedCustomer);
    }

    @Test
    @DisplayName("Should throw CustomerValidationException when validation fails")
    void shouldThrowExceptionWhenValidationFails() {
        // Given
        doThrow(new CustomerValidationException("Email is required"))
            .when(validationService).validateCustomerRegistration(validRequest);

        // When & Then
        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
            .isInstanceOf(CustomerValidationException.class)
            .hasMessage("Email is required");

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository, never()).saveAndFlush(any());
        verify(fraudCheckService, never()).isFraudulent(any());
        verify(notificationPublisher, never()).publishWelcomeNotification(any());
    }

    @Test
    @DisplayName("Should throw CustomerFraudException when fraud is detected")
    void shouldThrowExceptionWhenFraudDetected() {
        // Given
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(true);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);

        // When & Then
        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
            .isInstanceOf(CustomerFraudException.class)
            .hasMessage("Customer registration blocked due to fraud detection");

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository).saveAndFlush(any(Customer.class));
        verify(fraudCheckService).isFraudulent(savedCustomer.getId());
        verify(notificationPublisher, never()).publishWelcomeNotification(any());
    }

    @Test
    @DisplayName("Should complete registration even if notification fails")
    void shouldCompleteRegistrationEvenIfNotificationFails() {
        // Given
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        doThrow(new RuntimeException("Notification service unavailable"))
            .when(notificationPublisher).publishWelcomeNotification(savedCustomer);

        // When
        Customer result = customerService.registerCustomer(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository).saveAndFlush(any(Customer.class));
        verify(fraudCheckService).isFraudulent(savedCustomer.getId());
        verify(notificationPublisher).publishWelcomeNotification(savedCustomer);
    }

    @Test
    @DisplayName("Should handle repository failure gracefully")
    void shouldHandleRepositoryFailure() {
        // Given
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        when(customerRepository.saveAndFlush(any(Customer.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> customerService.registerCustomer(validRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database connection failed");

        verify(validationService).validateCustomerRegistration(validRequest);
        verify(customerRepository).saveAndFlush(any(Customer.class));
        verify(fraudCheckService, never()).isFraudulent(any());
        verify(notificationPublisher, never()).publishWelcomeNotification(any());
    }

    @Test
    @DisplayName("Should build customer object correctly from request")
    void shouldBuildCustomerObjectCorrectly() {
        // Given
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudCheckService.isFraudulent(savedCustomer.getId())).thenReturn(false);
        doNothing().when(validationService).validateCustomerRegistration(validRequest);
        doNothing().when(notificationPublisher).publishWelcomeNotification(savedCustomer);

        // When
        customerService.registerCustomer(validRequest);

        // Then
        verify(customerRepository).saveAndFlush(argThat(customer -> 
            customer.getFirstName().equals("John") &&
            customer.getLastName().equals("Doe") &&
            customer.getEmail().equals("john.doe@example.com") &&
            customer.getId() == null // ID should be null before saving
        ));
    }
} 