package tech.yump.veriboard.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "rabbitmq.exchanges.internal=test.internal.exchange",
    "rabbitmq.queue.notification=test.notification.queue",
    "rabbitmq.routing-keys.internal-notification=test.internal.notification.routing.key"
})
@DisplayName("Notification Configuration Tests")
class NotificationConfigTest {

    @Autowired
    private NotificationConfig notificationConfig;

    @Autowired
    private TopicExchange internalTopicExchange;

    @Autowired
    private Queue notificationQueue;

    @Autowired
    private Binding internalToNotificationBinding;

    @Test
    @DisplayName("Should inject configuration properties correctly")
    void shouldInjectConfigurationPropertiesCorrectly() {
        // When & Then
        assertThat(notificationConfig.getInternalExchange()).isEqualTo("test.internal.exchange");
        assertThat(notificationConfig.getNotificationQueue()).isEqualTo("test.notification.queue");
        assertThat(notificationConfig.getInternalNotificationRoutingKey()).isEqualTo("test.internal.notification.routing.key");
    }

    @Test
    @DisplayName("Should create internal topic exchange bean correctly")
    void shouldCreateInternalTopicExchangeBeanCorrectly() {
        // When & Then
        assertThat(internalTopicExchange).isNotNull();
        assertThat(internalTopicExchange.getName()).isEqualTo("test.internal.exchange");
        assertThat(internalTopicExchange.getType()).isEqualTo("topic");
        assertThat(internalTopicExchange.isDurable()).isTrue();
        assertThat(internalTopicExchange.isAutoDelete()).isFalse();
    }

    @Test
    @DisplayName("Should create notification queue bean correctly")
    void shouldCreateNotificationQueueBeanCorrectly() {
        // When & Then
        assertThat(notificationQueue).isNotNull();
        assertThat(notificationQueue.getName()).isEqualTo("test.notification.queue");
        assertThat(notificationQueue.isDurable()).isTrue();
        assertThat(notificationQueue.isAutoDelete()).isFalse();
        assertThat(notificationQueue.isExclusive()).isFalse();
    }

    @Test
    @DisplayName("Should create binding between queue and exchange correctly")
    void shouldCreateBindingBetweenQueueAndExchangeCorrectly() {
        // When & Then
        assertThat(internalToNotificationBinding).isNotNull();
        assertThat(internalToNotificationBinding.getDestination()).isEqualTo("test.notification.queue");
        assertThat(internalToNotificationBinding.getExchange()).isEqualTo("test.internal.exchange");
        assertThat(internalToNotificationBinding.getRoutingKey()).isEqualTo("test.internal.notification.routing.key");
    }

    @Test
    @DisplayName("Should return correct property values from getter methods")
    void shouldReturnCorrectPropertyValuesFromGetterMethods() {
        // When
        String internalExchange = notificationConfig.getInternalExchange();
        String notificationQueueName = notificationConfig.getNotificationQueue();
        String routingKey = notificationConfig.getInternalNotificationRoutingKey();

        // Then
        assertThat(internalExchange).isEqualTo("test.internal.exchange");
        assertThat(notificationQueueName).isEqualTo("test.notification.queue");
        assertThat(routingKey).isEqualTo("test.internal.notification.routing.key");
    }

    @Test
    @DisplayName("Should have configuration annotated correctly")
    void shouldHaveConfigurationAnnotatedCorrectly() {
        // When & Then
        assertThat(NotificationConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should create beans with correct bean names")
    void shouldCreateBeansWithCorrectBeanNames() {
        // When & Then
        assertThat(internalTopicExchange).isNotNull();
        assertThat(notificationQueue).isNotNull();
        assertThat(internalToNotificationBinding).isNotNull();
    }

    @Test
    @DisplayName("Should handle property injection for all required properties")
    void shouldHandlePropertyInjectionForAllRequiredProperties() {
        // When & Then
        assertThat(notificationConfig.getInternalExchange()).isNotNull();
        assertThat(notificationConfig.getInternalExchange()).isNotEmpty();
        
        assertThat(notificationConfig.getNotificationQueue()).isNotNull();
        assertThat(notificationConfig.getNotificationQueue()).isNotEmpty();
        
        assertThat(notificationConfig.getInternalNotificationRoutingKey()).isNotNull();
        assertThat(notificationConfig.getInternalNotificationRoutingKey()).isNotEmpty();
    }

    @Test
    @DisplayName("Should maintain consistency between configuration and beans")
    void shouldMaintainConsistencyBetweenConfigurationAndBeans() {
        // When & Then
        assertThat(internalTopicExchange.getName()).isEqualTo(notificationConfig.getInternalExchange());
        assertThat(notificationQueue.getName()).isEqualTo(notificationConfig.getNotificationQueue());
        assertThat(internalToNotificationBinding.getRoutingKey()).isEqualTo(notificationConfig.getInternalNotificationRoutingKey());
    }

    @Test
    @DisplayName("Should create topic exchange with correct properties")
    void shouldCreateTopicExchangeWithCorrectProperties() {
        // When
        TopicExchange exchange = notificationConfig.internalTopicExchange();

        // Then
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("test.internal.exchange");
        assertThat(exchange.getType()).isEqualTo("topic");
        assertThat(exchange.isDurable()).isTrue();
    }

    @Test
    @DisplayName("Should create queue with correct properties")
    void shouldCreateQueueWithCorrectProperties() {
        // When
        Queue queue = notificationConfig.notificationQueue();

        // Then
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("test.notification.queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(queue.isExclusive()).isFalse();
    }

    @Test
    @DisplayName("Should create binding with correct properties")
    void shouldCreateBindingWithCorrectProperties() {
        // When
        Binding binding = notificationConfig.internalToNotificationBinding();

        // Then
        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo("test.notification.queue");
        assertThat(binding.getExchange()).isEqualTo("test.internal.exchange");
        assertThat(binding.getRoutingKey()).isEqualTo("test.internal.notification.routing.key");
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
    }
} 