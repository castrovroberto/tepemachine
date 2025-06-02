package tech.yump.veriboard.customer.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;
import tech.yump.veriboard.customer.domain.exceptions.CustomerFraudException;
import tech.yump.veriboard.customer.domain.exceptions.CustomerValidationException;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    
    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/customers");
    }

    @Test
    @DisplayName("Should handle CustomerValidationException correctly")
    void shouldHandleCustomerValidationException() {
        // Given
        CustomerValidationException exception = new CustomerValidationException("Email is required");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Validation Failed");
        assertThat(body.get("message")).isEqualTo("Email is required");
        assertThat(body.get("path")).isEqualTo("/api/v1/customers");
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle CustomerFraudException correctly")
    void shouldHandleCustomerFraudException() {
        // Given
        CustomerFraudException exception = new CustomerFraudException("Customer registration blocked due to fraud detection");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleFraudException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(409);
        assertThat(body.get("error")).isEqualTo("Registration Blocked");
        assertThat(body.get("message")).isEqualTo("Customer registration blocked due to fraud detection");
        assertThat(body.get("path")).isEqualTo("/api/v1/customers");
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException correctly")
    void shouldHandleHttpMessageNotReadableException() {
        // Given
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleMalformedJsonException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Malformed JSON request");
        assertThat(body.get("path")).isEqualTo("/api/v1/customers");
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle general Exception correctly")
    void shouldHandleGeneralException() {
        // Given
        Exception exception = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(500);
        assertThat(body.get("error")).isEqualTo("Internal Server Error");
        assertThat(body.get("message")).isEqualTo("An unexpected error occurred");
        assertThat(body.get("path")).isEqualTo("/api/v1/customers");
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle path extraction from WebRequest correctly")
    void shouldHandlePathExtractionCorrectly() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/customers/register");
        CustomerValidationException exception = new CustomerValidationException("Invalid data");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("path")).isEqualTo("/api/v1/customers/register");
    }

    @Test
    @DisplayName("Should handle null exception message gracefully")
    void shouldHandleNullExceptionMessage() {
        // Given
        CustomerValidationException exception = new CustomerValidationException(null);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isNull();
    }

    @Test
    @DisplayName("Should handle empty path from WebRequest")
    void shouldHandleEmptyPathFromWebRequest() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=");
        CustomerValidationException exception = new CustomerValidationException("Test message");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("path")).isEqualTo("");
    }
} 