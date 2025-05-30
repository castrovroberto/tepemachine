package tech.yump.msapp.customer;

import org.springframework.stereotype.Service;
import tech.yump.msaap.amqp.RabbitMQMessageProducer;
import tech.yump.msapp.clients.notification.NotificationRequest;

@Service
public class RabbitMQNotificationPublisher implements NotificationPublisher {
    
    private final RabbitMQMessageProducer rabbitMQMessageProducer;
    
    public RabbitMQNotificationPublisher(RabbitMQMessageProducer rabbitMQMessageProducer) {
        this.rabbitMQMessageProducer = rabbitMQMessageProducer;
    }
    
    @Override
    public void publishWelcomeNotification(Customer customer) {
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to VeriBoard! We're excited to have you on board.", 
                            customer.getFirstName())
        );
        
        rabbitMQMessageProducer.publish(
            notificationRequest, 
            "internal.exchange", 
            "internal.notification.routing-key"
        );
    }
} 