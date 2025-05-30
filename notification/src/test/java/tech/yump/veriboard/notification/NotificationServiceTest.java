package tech.yump.veriboard.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void send_ShouldCreateAndSaveNotification() {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                "test@example.com",
                "Welcome to VeriBoard!"
        );

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void send_ShouldCreateNotificationWithCorrectData() {
        // Given
        Integer customerId = 2;
        String customerEmail = "user@test.com";
        String message = "Test message";
        NotificationRequest request = new NotificationRequest(customerId, customerEmail, message);

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(argThat(notification -> 
            notification.getToCustomerId().equals(customerId) &&
            notification.getToCustomerEmail().equals(customerEmail) &&
            notification.getMessage().equals(message) &&
            notification.getSender().equals("VeriBoard") &&
            notification.getSentAt() != null
        ));
    }

    @Test
    void send_WithNullCustomerId_ShouldStillSaveNotification() {
        // Given
        NotificationRequest request = new NotificationRequest(
                null,
                "test@example.com",
                "Test message"
        );

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void send_WithEmptyMessage_ShouldStillSaveNotification() {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                "test@example.com",
                ""
        );

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void send_WithNullEmail_ShouldStillSaveNotification() {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                null,
                "Test message"
        );

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void send_ShouldSetCorrectSenderAndTimestamp() {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                "test@example.com",
                "Welcome!"
        );

        // When
        notificationService.send(request);

        // Then
        verify(notificationRepository).save(argThat(notification -> 
            "VeriBoard".equals(notification.getSender()) &&
            notification.getSentAt() != null
        ));
    }
} 