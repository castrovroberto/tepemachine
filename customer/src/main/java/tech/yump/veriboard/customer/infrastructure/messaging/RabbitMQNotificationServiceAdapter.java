package tech.yump.veriboard.customer.infrastructure.messaging;

import org.springframework.stereotype.Component;
import tech.yump.veriboard.amqp.RabbitMQMessageProducer;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.ports.NotificationService;
import tech.yump.veriboard.clients.notification.NotificationRequest;

/**
 * Adapter that implements the domain NotificationService port using RabbitMQ.
 * This bridges the domain layer with the messaging infrastructure.
 */
@Component
public class RabbitMQNotificationServiceAdapter implements NotificationService {
    
    private final RabbitMQMessageProducer messageProducer;
    
    public RabbitMQNotificationServiceAdapter(RabbitMQMessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }
    
    @Override
    public void sendWelcomeNotification(Customer customer) {
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to VeriBoard! We're excited to have you on board.", 
                            customer.getFirstName())
        );
        
        messageProducer.publish(
            notificationRequest, 
            "internal.exchange", 
            "internal.notification.routing-key"
        );
    }
} 