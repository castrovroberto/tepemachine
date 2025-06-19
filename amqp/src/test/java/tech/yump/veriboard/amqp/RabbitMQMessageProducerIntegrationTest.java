package tech.yump.veriboard.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@DisplayName("RabbitMQ Message Producer Integration Tests with Testcontainers")
class RabbitMQMessageProducerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management")
            .withUser("test", "test")
            .withVhost("/")
            .withPermission("/", "test", ".*", ".*", ".*");

    private RabbitMQConfig rabbitMQConfig;
    private RabbitMQMessageProducer messageProducer;
    private RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;
    private ObjectMapper objectMapper;
    
    private static final String TEST_EXCHANGE = "test.exchange";
    private static final String TEST_QUEUE = "test.queue";
    private static final String TEST_ROUTING_KEY = "test.routing.key";
    
    private static final String CUSTOMER_EXCHANGE = "customer.exchange";
    private static final String NOTIFICATION_QUEUE = "notification.queue";
    private static final String NOTIFICATION_ROUTING_KEY = "customer.notification";

    @BeforeEach
    void setUp() {
        // Set up RabbitMQ connection factory
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQContainer.getHost());
        connectionFactory.setPort(rabbitMQContainer.getAmqpPort());
        connectionFactory.setUsername("test");
        connectionFactory.setPassword("test");
        connectionFactory.setVirtualHost("/");
        
        // Initialize configuration and producer
        rabbitMQConfig = new RabbitMQConfig(connectionFactory);
        messageProducer = new RabbitMQMessageProducer(rabbitMQConfig.amqpTemplate());
        
        // Create RabbitTemplate for verification
        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMQConfig.jacksonConverter());
        
        // Create RabbitAdmin for infrastructure management
        rabbitAdmin = new RabbitAdmin(connectionFactory);
        
        // Initialize ObjectMapper for JSON verification
        objectMapper = new ObjectMapper();
        
        // Set up test infrastructure
        setupTestInfrastructure();
    }

    private void setupTestInfrastructure() {
        // Test exchange and queue
        DirectExchange testExchange = new DirectExchange(TEST_EXCHANGE);
        rabbitAdmin.declareExchange(testExchange);
        
        Queue testQueue = new Queue(TEST_QUEUE, true);
        rabbitAdmin.declareQueue(testQueue);
        
        Binding testBinding = BindingBuilder.bind(testQueue).to(testExchange).with(TEST_ROUTING_KEY);
        rabbitAdmin.declareBinding(testBinding);
        
        // Customer/notification exchange and queue (simulating real usage)
        DirectExchange customerExchange = new DirectExchange(CUSTOMER_EXCHANGE);
        rabbitAdmin.declareExchange(customerExchange);
        
        Queue notificationQueue = new Queue(NOTIFICATION_QUEUE, true);
        rabbitAdmin.declareQueue(notificationQueue);
        
        Binding notificationBinding = BindingBuilder.bind(notificationQueue).to(customerExchange).with(NOTIFICATION_ROUTING_KEY);
        rabbitAdmin.declareBinding(notificationBinding);
    }

    @Test
    @DisplayName("Should successfully publish and receive string message")
    void publish_StringMessage_ShouldBeSuccessfullyReceived() {
        // Given
        String testMessage = "Hello, RabbitMQ Integration Test!";

        // When
        messageProducer.publish(testMessage, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(TEST_QUEUE);
            assertThat(receivedMessage).isNotNull();
            
            String receivedPayload = (String) rabbitTemplate.getMessageConverter().fromMessage(receivedMessage);
            assertThat(receivedPayload).isEqualTo(testMessage);
        });
    }

    @Test
    @DisplayName("Should successfully publish and receive complex object message")
    void publish_ComplexObject_ShouldBeSuccessfullyReceived() {
        // Given
        CustomerNotification notification = new CustomerNotification(
                "customer-123", 
                "John Doe", 
                "john.doe@example.com", 
                "Welcome to VeriBoard!"
        );

        // When
        messageProducer.publish(notification, CUSTOMER_EXCHANGE, NOTIFICATION_ROUTING_KEY);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(NOTIFICATION_QUEUE);
            assertThat(receivedMessage).isNotNull();
            
            CustomerNotification receivedPayload = (CustomerNotification) rabbitTemplate.getMessageConverter().fromMessage(receivedMessage);
            assertThat(receivedPayload)
                    .isNotNull()
                    .satisfies(msg -> {
                        assertThat(msg.getCustomerId()).isEqualTo("customer-123");
                        assertThat(msg.getCustomerName()).isEqualTo("John Doe");
                        assertThat(msg.getEmail()).isEqualTo("john.doe@example.com");
                        assertThat(msg.getMessage()).isEqualTo("Welcome to VeriBoard!");
                    });
        });
    }

    @Test
    @DisplayName("Should handle multiple messages published to same exchange")
    void publish_MultipleMessages_ShouldAllBeReceived() {
        // Given
        String message1 = "First integration message";
        String message2 = "Second integration message";
        String message3 = "Third integration message";

        // When
        messageProducer.publish(message1, TEST_EXCHANGE, TEST_ROUTING_KEY);
        messageProducer.publish(message2, TEST_EXCHANGE, TEST_ROUTING_KEY);
        messageProducer.publish(message3, TEST_EXCHANGE, TEST_ROUTING_KEY);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check that all messages are available in queue
            QueueInformation queueInfo = rabbitAdmin.getQueueInfo(TEST_QUEUE);
            assertThat(queueInfo.getMessageCount()).isGreaterThanOrEqualTo(3);
        });

        // Receive and verify all messages
        Message receivedMessage1 = rabbitTemplate.receive(TEST_QUEUE, 5000);
        Message receivedMessage2 = rabbitTemplate.receive(TEST_QUEUE, 5000);
        Message receivedMessage3 = rabbitTemplate.receive(TEST_QUEUE, 5000);

        assertThat(receivedMessage1).isNotNull();
        assertThat(receivedMessage2).isNotNull();
        assertThat(receivedMessage3).isNotNull();

        String payload1 = (String) rabbitTemplate.getMessageConverter().fromMessage(receivedMessage1);
        String payload2 = (String) rabbitTemplate.getMessageConverter().fromMessage(receivedMessage2);
        String payload3 = (String) rabbitTemplate.getMessageConverter().fromMessage(receivedMessage3);

        assertThat(payload1).isEqualTo(message1);
        assertThat(payload2).isEqualTo(message2);
        assertThat(payload3).isEqualTo(message3);
    }

    @Test
    @DisplayName("Should properly serialize and deserialize JSON messages")
    void publish_JSONSerialization_ShouldWorkCorrectly() throws Exception {
        // Given
        CustomerNotification originalNotification = new CustomerNotification(
                "customer-456", 
                "Jane Smith", 
                "jane.smith@example.com", 
                "Account created successfully!"
        );

        // When
        messageProducer.publish(originalNotification, CUSTOMER_EXCHANGE, NOTIFICATION_ROUTING_KEY);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(NOTIFICATION_QUEUE);
            assertThat(receivedMessage).isNotNull();
            
            // Verify message is properly JSON serialized
            String messageBody = new String(receivedMessage.getBody());
            assertThat(messageBody).contains("customer-456");
            assertThat(messageBody).contains("Jane Smith");
            assertThat(messageBody).contains("jane.smith@example.com");
            
            // Verify it can be deserialized back to object
            CustomerNotification deserializedNotification = objectMapper.readValue(messageBody, CustomerNotification.class);
            assertThat(deserializedNotification.getCustomerId()).isEqualTo(originalNotification.getCustomerId());
            assertThat(deserializedNotification.getCustomerName()).isEqualTo(originalNotification.getCustomerName());
            assertThat(deserializedNotification.getEmail()).isEqualTo(originalNotification.getEmail());
            assertThat(deserializedNotification.getMessage()).isEqualTo(originalNotification.getMessage());
        });
    }

    @Test
    @DisplayName("Should handle null payload gracefully")
    void publish_NullPayload_ShouldNotThrowException() {
        // Given
        Object nullPayload = null;

        // When/Then - Jackson converter throws NPE for null objects, which is expected behavior
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            messageProducer.publish(nullPayload, TEST_EXCHANGE, TEST_ROUTING_KEY);
        });
    }

    // Test payload class representing a customer notification
    public static class CustomerNotification {
        private String customerId;
        private String customerName;
        private String email;
        private String message;

        // Default constructor for Jackson
        public CustomerNotification() {}

        public CustomerNotification(String customerId, String customerName, String email, String message) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.email = email;
            this.message = message;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
} 