package tech.yump.veriboard.amqp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQMessageProducerTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    private RabbitMQMessageProducer messageProducer;

    @BeforeEach
    void setUp() {
        messageProducer = new RabbitMQMessageProducer(amqpTemplate);
    }

    @Test
    void publish_ShouldCallAmqpTemplateConvertAndSend() {
        // Given
        Object payload = "test message";
        String exchange = "test.exchange";
        String routingKey = "test.routing.key";

        // When
        messageProducer.publish(payload, exchange, routingKey);

        // Then
        verify(amqpTemplate).convertAndSend(exchange, routingKey, payload);
    }

    @Test
    void publish_WithComplexPayload_ShouldCallAmqpTemplate() {
        // Given
        TestPayload payload = new TestPayload("test", 123);
        String exchange = "internal.exchange";
        String routingKey = "internal.notification.routing-key";

        // When
        messageProducer.publish(payload, exchange, routingKey);

        // Then
        verify(amqpTemplate).convertAndSend(exchange, routingKey, payload);
    }

    @Test
    void publish_WithNullPayload_ShouldStillCallAmqpTemplate() {
        // Given
        Object payload = null;
        String exchange = "test.exchange";
        String routingKey = "test.routing.key";

        // When
        messageProducer.publish(payload, exchange, routingKey);

        // Then
        verify(amqpTemplate).convertAndSend(exchange, routingKey, payload);
    }

    @Test
    void publish_ShouldCallAmqpTemplateOnce() {
        // Given
        String payload = "single message";
        String exchange = "test.exchange";
        String routingKey = "test.routing.key";

        // When
        messageProducer.publish(payload, exchange, routingKey);

        // Then
        verify(amqpTemplate, times(1)).convertAndSend(exchange, routingKey, payload);
        verifyNoMoreInteractions(amqpTemplate);
    }

    // Test payload class for testing complex objects
    private static class TestPayload {
        private final String message;
        private final Integer id;

        public TestPayload(String message, Integer id) {
            this.message = message;
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public Integer getId() {
            return id;
        }
    }
} 