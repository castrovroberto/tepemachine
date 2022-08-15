package tech.yump.msapp.customer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.yump.msaap.amqp.RabbitMQMessageProducer;
import tech.yump.msapp.clients.fraud.FraudCheckResponse;
import tech.yump.msapp.clients.fraud.FraudClient;
import tech.yump.msapp.clients.notification.NotificationClient;
import tech.yump.msapp.clients.notification.NotificationRequest;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        // todo: check if email valid
        // todo: check if email not taken
        customerRepository.saveAndFlush(customer);
        // todo: check if fraudster
        FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        // todo: send notification in an async manner
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to Yump Technologies...", customer.getFirstName())
        );

        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }
}
