package tech.yump.veriboard.customer.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.amqp.RabbitMQMessageProducer;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQ Notification Service Adapter Tests")
class RabbitMQNotificationServiceAdapterTest {

    @Mock
    private RabbitMQMessageProducer messageProducer;

    @InjectMocks
    private RabbitMQNotificationServiceAdapter notificationServiceAdapter;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(1, "John", "Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should send welcome notification successfully")
    void shouldSendWelcomeNotificationSuccessfully() {
        // Given
        doNothing().when(messageProducer).publish(any(), anyString(), anyString());

        // When
        notificationServiceAdapter.sendWelcomeNotification(customer);

        // Then
        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(messageProducer).publish(requestCaptor.capture(), exchangeCaptor.capture(), routingKeyCaptor.capture());

        NotificationRequest capturedRequest = requestCaptor.getValue();
        // Using reflection to access fields since we don't know if it's a record or class
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.toString()).contains("1"); // Customer ID
        assertThat(capturedRequest.toString()).contains("john.doe@example.com"); // Email
        assertThat(capturedRequest.toString()).contains("Hi John, welcome to VeriBoard! We're excited to have you on board."); // Message

        assertThat(exchangeCaptor.getValue()).isEqualTo("internal.exchange");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("internal.notification.routing-key");
    }

    @Test
    @DisplayName("Should handle customer with different name correctly")
    void shouldHandleCustomerWithDifferentNameCorrectly() {
        // Given
        Customer customerWithDifferentName = new Customer(2, "Jane", "Smith", "jane.smith@example.com");
        doNothing().when(messageProducer).publish(any(), anyString(), anyString());

        // When
        notificationServiceAdapter.sendWelcomeNotification(customerWithDifferentName);

        // Then
        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(messageProducer).publish(requestCaptor.capture(), anyString(), anyString());

        NotificationRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.toString()).contains("2"); // Customer ID
        assertThat(capturedRequest.toString()).contains("jane.smith@example.com"); // Email
        assertThat(capturedRequest.toString()).contains("Hi Jane, welcome to VeriBoard! We're excited to have you on board."); // Message
    }

    @Test
    @DisplayName("Should handle messaging exceptions gracefully")
    void shouldHandleMessagingExceptionsGracefully() {
        // Given
        doThrow(new RuntimeException("RabbitMQ connection failed"))
            .when(messageProducer).publish(any(), anyString(), anyString());

        // When & Then
        assertThatThrownBy(() -> notificationServiceAdapter.sendWelcomeNotification(customer))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("RabbitMQ connection failed");

        verify(messageProducer).publish(any(NotificationRequest.class), eq("internal.exchange"), eq("internal.notification.routing-key"));
    }

    @Test
    @DisplayName("Should create correct notification message format")
    void shouldCreateCorrectNotificationMessageFormat() {
        // Given
        Customer customerWithSpecialName = new Customer(3, "María José", "González", "maria.gonzalez@example.com");
        doNothing().when(messageProducer).publish(any(), anyString(), anyString());

        // When
        notificationServiceAdapter.sendWelcomeNotification(customerWithSpecialName);

        // Then
        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(messageProducer).publish(requestCaptor.capture(), anyString(), anyString());

        NotificationRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.toString()).contains("Hi María José, welcome to VeriBoard! We're excited to have you on board.");
    }
} 