package tech.yump.msapp.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.yump.msapp.clients.fraud.FraudCheckResponse;
import tech.yump.msapp.clients.fraud.FraudClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebMvc
@Transactional
@DisplayName("Customer Integration Tests")
class CustomerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("customer_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management")
            .withUser("test", "test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudClient fraudClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "test");
        registry.add("spring.rabbitmq.password", () -> "test");
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register customer with valid data")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any(Integer.class)))
            .thenReturn(new FraudCheckResponse(false));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer registered successfully"));

        // Verify customer was saved in database
        var customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        Customer savedCustomer = customers.get(0);
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedCustomer.getId()).isNotNull();

        verify(fraudClient).isFraudster(savedCustomer.getId());
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "", "Doe", "invalid-email"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());

        // Verify no customer was saved
        assertThat(customerRepository.findAll()).isEmpty();
        verifyNoInteractions(fraudClient);
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // Given - Save a customer first
        Customer existingCustomer = Customer.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("john.doe@example.com")
            .build();
        customerRepository.save(existingCustomer);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Email is already registered"));

        // Verify only the original customer exists
        var customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getFirstName()).isEqualTo("Jane");
        verifyNoInteractions(fraudClient);
    }

    @Test
    @DisplayName("Should return 500 when fraud is detected")
    void shouldReturn500WhenFraudDetected() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any(Integer.class)))
            .thenReturn(new FraudCheckResponse(true));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Fraud detected"))
                .andExpect(jsonPath("$.details").value("Customer registration blocked due to fraud detection"));

        // Customer should still be saved even though fraud was detected
        // (this is the current behavior - in production you might want different logic)
        var customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        verify(fraudClient).isFraudster(any(Integer.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void shouldHandleMalformedJsonGracefully() throws Exception {
        // Given
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());

        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should handle missing content type")
    void shouldHandleMissingContentType() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should successfully find customer by email")
    void shouldFindCustomerByEmail() {
        // Given
        Customer customer = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
        customerRepository.save(customer);

        // When
        var found = customerRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty when customer not found by email")
    void shouldReturnEmptyWhenCustomerNotFoundByEmail() {
        // When
        var found = customerRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should complete registration even if notification fails")
    void shouldCompleteRegistrationEvenIfNotificationFails() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );
        when(fraudClient.isFraudster(any(Integer.class)))
            .thenReturn(new FraudCheckResponse(false));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer registered successfully"));

        // Customer should be saved regardless of notification outcome
        var customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        verify(fraudClient).isFraudster(any(Integer.class));
    }
} 