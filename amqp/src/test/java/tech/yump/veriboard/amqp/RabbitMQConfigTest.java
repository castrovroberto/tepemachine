package tech.yump.veriboard.amqp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("RabbitMQ Configuration Tests")
class RabbitMQConfigTest {

    private final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

    @Test
    @DisplayName("Should create AmqpTemplate bean")
    void amqpTemplate_ShouldBeCreated() {
        // Given
        RabbitMQConfig config = new RabbitMQConfig(connectionFactory);

        // When
        AmqpTemplate amqpTemplate = config.amqpTemplate();

        // Then
        assertThat(amqpTemplate)
                .isNotNull()
                .isInstanceOf(RabbitTemplate.class);
    }

    @Test
    @DisplayName("Should create Jackson message converter bean")
    void jacksonConverter_ShouldBeCreated() {
        // Given
        RabbitMQConfig config = new RabbitMQConfig(connectionFactory);

        // When
        MessageConverter converter = config.jacksonConverter();

        // Then
        assertThat(converter)
                .isNotNull()
                .isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    @DisplayName("Should create SimpleRabbitListenerContainerFactory bean")
    void simpleRabbitListenerContainerFactory_ShouldBeCreated() {
        // Given
        RabbitMQConfig config = new RabbitMQConfig(connectionFactory);

        // When
        SimpleRabbitListenerContainerFactory factory = config.simpleRabbitListenerContainerFactory();

        // Then
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("AmqpTemplate should use correct ConnectionFactory and MessageConverter")
    void amqpTemplate_ShouldBeConfiguredCorrectly() {
        // Given
        RabbitMQConfig config = new RabbitMQConfig(connectionFactory);
        MessageConverter jacksonConverter = config.jacksonConverter();

        // When
        AmqpTemplate amqpTemplate = config.amqpTemplate();

        // Then
        assertThat(amqpTemplate).isInstanceOf(RabbitTemplate.class);
        
        RabbitTemplate rabbitTemplate = (RabbitTemplate) amqpTemplate;
        assertThat(rabbitTemplate.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    @DisplayName("Configuration should be properly initialized with ConnectionFactory")
    void configuration_ShouldBeInitializedWithConnectionFactory() {
        // Given/When
        RabbitMQConfig config = new RabbitMQConfig(connectionFactory);

        // Then
        assertThat(config).isNotNull();
        
        // Verify all beans can be created
        assertThat(config.amqpTemplate()).isNotNull();
        assertThat(config.jacksonConverter()).isNotNull();
        assertThat(config.simpleRabbitListenerContainerFactory()).isNotNull();
    }
}