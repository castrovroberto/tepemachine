package tech.yump.veriboard.customer.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yump.veriboard.customer.application.CustomerService;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerFraudException;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRegistrationRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRegistrationRequest(
            "John", 
            "Doe", 
            "john.doe@example.com"
        );
        
        savedCustomer = new Customer(1, "John", "Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should register customer successfully")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        // Given
        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
            .thenReturn(savedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Customer registered successfully"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService).registerCustomer(any(CustomerRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should handle validation exception")
    void shouldHandleValidationException() throws Exception {
        // Given
        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
            .thenThrow(new CustomerValidationException("Email is required"));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email is required"));

        verify(customerService).registerCustomer(any(CustomerRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should handle fraud exception")
    void shouldHandleFraudException() throws Exception {
        // Given
        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
            .thenThrow(new CustomerFraudException("Customer registration blocked due to fraud detection"));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration Blocked"))
                .andExpect(jsonPath("$.message").value("Customer registration blocked due to fraud detection"));

        verify(customerService).registerCustomer(any(CustomerRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verify(customerService, never()).registerCustomer(any());
    }

    @Test
    @DisplayName("Should handle internal server error")
    void shouldHandleInternalServerError() throws Exception {
        // Given
        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

        verify(customerService).registerCustomer(any(CustomerRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should validate request body structure")
    void shouldValidateRequestBodyStructure() throws Exception {
        // Given
        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
            .thenReturn(savedCustomer);

        CustomerRegistrationRequest requestWithDifferentData = new CustomerRegistrationRequest(
            "Jane", 
            "Smith", 
            "jane.smith@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithDifferentData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com")); // Returns saved customer data

        verify(customerService).registerCustomer(any(CustomerRegistrationRequest.class));
    }
} 