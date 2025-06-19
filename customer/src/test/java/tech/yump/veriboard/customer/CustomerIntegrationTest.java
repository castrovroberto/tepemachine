package tech.yump.veriboard.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.yump.veriboard.customer.domain.CustomerRegistrationRequest;
import tech.yump.veriboard.customer.infrastructure.persistence.JpaCustomerRepository;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;
import tech.yump.veriboard.clients.fraud.FraudClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Customer Integration Tests - TestContainers")
class CustomerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("customer_integration_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(false);

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management-alpine")
            .withUser("test_user", "test_password")
            .withReuse(false);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaCustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudClient fraudClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Override test configuration with TestContainers
        // This will override the H2 config from application-test.yml
        
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA configuration for PostgreSQL
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        
        // RabbitMQ configuration
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "test_user");
        registry.add("spring.rabbitmq.password", () -> "test_password");
        
        // Ensure cloud components remain disabled
        registry.add("management.tracing.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a new customer")
    void shouldRegisterNewCustomer() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any())).thenReturn(new FraudCheckResponse(false));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer registered successfully"))
                .andExpect(jsonPath("$.customerId").isNumber())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        // Verify customer was saved to database
        assertThat(customerRepository.count()).isEqualTo(1);
        var savedCustomer = customerRepository.findAll().get(0);
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return 400 when first name is missing")
    void shouldReturn400WhenFirstNameMissing() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "Doe", "john.doe@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("First name is required"));

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 400 when last name is missing")
    void shouldReturn400WhenLastNameMissing() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "", "john.doe@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Last name is required"));

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 400 when email is missing")
    void shouldReturn400WhenEmailMissing() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", ""
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email is required"));

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void shouldReturn400WhenEmailFormatInvalid() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "invalid-email"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email format is invalid"));

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 400 when email is already registered")
    void shouldReturn400WhenEmailAlreadyRegistered() throws Exception {
        // Given - Register first customer
        CustomerRegistrationRequest firstRequest = new CustomerRegistrationRequest(
            "Jane", "Smith", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any())).thenReturn(new FraudCheckResponse(false));
        
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Try to register second customer with same email
        CustomerRegistrationRequest secondRequest = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );

        // Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email is already registered"));

        // Verify only one customer was saved
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 409 when fraud is detected")
    void shouldReturn409WhenFraudDetected() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any())).thenReturn(new FraudCheckResponse(true));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration Blocked"))
                .andExpect(jsonPath("$.message").value("Customer registration blocked due to fraud detection"));

        // Verify customer was saved but registration was blocked
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void shouldReturn400ForMalformedJson() throws Exception {
        // Given
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle multiple validation errors")
    void shouldHandleMultipleValidationErrors() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "", "invalid-email"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(
                    org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("First name is required"),
                        org.hamcrest.Matchers.containsString("Last name is required"),
                        org.hamcrest.Matchers.containsString("Email format is invalid")
                    )
                ));

        // Verify no customer was saved
        assertThat(customerRepository.count()).isEqualTo(0);
    }
} 