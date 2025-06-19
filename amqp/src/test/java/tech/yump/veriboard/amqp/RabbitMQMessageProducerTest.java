package tech.yump.veriboard.amqp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQ Message Producer Unit Tests")
class RabbitMQMessageProducerTest {

    @Mock
    private AmqpTemplate mockAmqpTemplate;
    
    private RabbitMQMessageProducer messageProducer;
    
    private static final String TEST_EXCHANGE = "test.exchange";
    private static final String TEST_ROUTING_KEY = "test.routing.key";

    @BeforeEach
    void setUp() {
        messageProducer = new RabbitMQMessageProducer(mockAmqpTemplate);
    }

    @Test
    @DisplayName("Should publish string message successfully")
    void publish_StringMessage_ShouldCallAmqpTemplate() {
        // Given
        String payload = "Test message";

        // When
        messageProducer.publish(payload, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, payload);
    }

    @Test
    @DisplayName("Should publish object message successfully")
    void publish_ObjectMessage_ShouldCallAmqpTemplate() {
        // Given
        TestMessage payload = new TestMessage("test-id", "Test content");

        // When
        messageProducer.publish(payload, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, payload);
    }

    @Test
    @DisplayName("Should handle null payload without throwing exception")
    void publish_NullPayload_ShouldNotThrowException() {
        // Given
        Object nullPayload = null;

        // When/Then
        assertThatNoException()
                .isThrownBy(() -> messageProducer.publish(nullPayload, TEST_EXCHANGE, TEST_ROUTING_KEY));
        
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, nullPayload);
    }

    @Test
    @DisplayName("Should handle empty string payload")
    void publish_EmptyStringPayload_ShouldCallAmqpTemplate() {
        // Given
        String emptyPayload = "";

        // When
        messageProducer.publish(emptyPayload, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, emptyPayload);
    }

    @Test
    @DisplayName("Should handle different exchange and routing key combinations")
    void publish_DifferentExchangeAndRoutingKey_ShouldCallAmqpTemplate() {
        // Given
        String payload = "Test message";
        String customExchange = "custom.exchange";
        String customRoutingKey = "custom.routing.key";

        // When
        messageProducer.publish(payload, customExchange, customRoutingKey);

        // Then
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(customExchange, customRoutingKey, payload);
    }

    @Test
    @DisplayName("Should handle multiple messages published sequentially")
    void publish_MultipleMessages_ShouldCallAmqpTemplateMultipleTimes() {
        // Given
        String message1 = "First message";
        String message2 = "Second message";
        String message3 = "Third message";

        // When
        messageProducer.publish(message1, TEST_EXCHANGE, TEST_ROUTING_KEY);
        messageProducer.publish(message2, TEST_EXCHANGE, TEST_ROUTING_KEY);
        messageProducer.publish(message3, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        verify(mockAmqpTemplate, times(3))
                .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(Object.class));
        
        verify(mockAmqpTemplate).convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, (Object) message1);
        verify(mockAmqpTemplate).convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, (Object) message2);
        verify(mockAmqpTemplate).convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, (Object) message3);
    }

    @Test
    @DisplayName("Constructor should initialize with provided AmqpTemplate")
    void constructor_ShouldInitializeCorrectly() {
        // Given/When
        RabbitMQMessageProducer producer = new RabbitMQMessageProducer(mockAmqpTemplate);

        // Then
        assertThat(producer).isNotNull();
    }

    @Test
    @DisplayName("Should propagate AmqpTemplate exceptions")
    void publish_AmqpTemplateThrowsException_ShouldPropagateException() {
        // Given
        String payload = "Test message";
        RuntimeException expectedException = new RuntimeException("AMQP connection failed");
        
        doThrow(expectedException)
                .when(mockAmqpTemplate)
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, payload);

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            messageProducer.publish(payload, TEST_EXCHANGE, TEST_ROUTING_KEY);
        });
        
        verify(mockAmqpTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, payload);
    }

    // Test message class for object serialization tests
    public static class TestMessage {
        private String id;
        private String content;

        public TestMessage() {}

        public TestMessage(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
} 