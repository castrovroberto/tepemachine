package tech.yump.veriboard.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Notification Application Tests")
class NotificationApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load Spring application context successfully")
    void contextLoads() {
        // When & Then
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should have correct base package scanning configuration")
    void shouldHaveCorrectPackageScanning() {
        // When & Then
        assertThat(applicationContext.containsBean("notificationConfig")).isTrue();
        assertThat(applicationContext.containsBean("rabbitMQMessageProducer")).isTrue();
    }

    @Test
    @DisplayName("Should scan base packages correctly")
    void shouldScanBasePackagesCorrectly() {
        // When & Then - Check that beans from different packages are loaded
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertThat(beanNames).isNotEmpty();

        // Check for beans from specific packages
        boolean hasAmqpBeans = false;
        boolean hasNotificationBeans = false;
        for (String beanName : beanNames) {
            if (beanName.contains("rabbitmq") || beanName.contains("amqp") || beanName.contains("Rabbit")) {
                hasAmqpBeans = true;
            }
            if (beanName.contains("notification") || beanName.contains("Notification")) {
                hasNotificationBeans = true;
            }
        }

        assertThat(hasAmqpBeans).isTrue();
        assertThat(hasNotificationBeans).isTrue();
    }

    @Test
    @DisplayName("Should be annotated as SpringBootApplication")
    void shouldBeAnnotatedAsSpringBootApplication() {
        // When & Then
        assertThat(NotificationApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class)).isTrue();
    }

    @Test
    @DisplayName("Should have notification specific beans loaded")
    void shouldHaveNotificationSpecificBeansLoaded() {
        // When & Then
        assertThat(applicationContext.containsBean("notificationQueue")).isTrue();
        assertThat(applicationContext.containsBean("internalTopicExchange")).isTrue();
        assertThat(applicationContext.containsBean("internalToNotificationBinding")).isTrue();
    }

    @Test
    @DisplayName("Should have correct Spring Boot application configuration")
    void shouldHaveCorrectSpringBootApplicationConfiguration() {
        // When
        org.springframework.boot.autoconfigure.SpringBootApplication annotation =
                NotificationApplication.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);

        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.scanBasePackages()).contains(
                "tech.yump.veriboard",
                "tech.yump.veriboard.amqp",
                "tech.yump.veriboard.notification"
        );
    }

    @Test
    @DisplayName("Should load all required infrastructure beans")
    void shouldLoadAllRequiredInfrastructureBeans() {
        // When & Then
        assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(10);

        // Verify core infrastructure beans exist
        assertThat(applicationContext.containsBean("rabbitMQConfig")).isTrue();
        assertThat(applicationContext.containsBean("notificationService")).isTrue();
        assertThat(applicationContext.containsBean("notificationController")).isTrue();
    }

    @Test
    @DisplayName("Should have main method that can be invoked")
    void shouldHaveMainMethodThatCanBeInvoked() {
        // When & Then - Just verify the main method exists and is properly configured
        assertThatNoException().isThrownBy(() -> {
            // Verify the main method exists
            java.lang.reflect.Method mainMethod = NotificationApplication.class.getMethod("main", String[].class);
            assertThat(mainMethod).isNotNull();
            assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers())).isTrue();
        });
    }
}