package tech.yump.veriboard.notification.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.clients.notification.NotificationRequest;
import tech.yump.veriboard.notification.NotificationService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        notificationConsumer = new NotificationConsumer(notificationService);
    }

    @Test
    void consumer_ShouldCallNotificationService() {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                "test@example.com",
                "Test notification"
        );

        // When
        notificationConsumer.consumer(request);

        // Then
        verify(notificationService).send(request);
    }

    @Test
    void consumer_WithNullFields_ShouldStillCallNotificationService() {
        // Given
        NotificationRequest request = new NotificationRequest(
                null,
                null,
                null
        );

        // When
        notificationConsumer.consumer(request);

        // Then
        verify(notificationService).send(request);
    }

    @Test
    void consumer_WithValidRequest_ShouldCallServiceOnce() {
        // Given
        NotificationRequest request = new NotificationRequest(
                123,
                "customer@example.com",
                "Welcome to our platform!"
        );

        // When
        notificationConsumer.consumer(request);

        // Then
        verify(notificationService, times(1)).send(request);
        verifyNoMoreInteractions(notificationService);
    }
} 