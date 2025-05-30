package tech.yump.veriboard.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void send(NotificationRequest request) {
        log.info("Sending notification to customer {}: {}", request.toCustomerId(), request.message());
        
        notificationRepository.save(
                Notification.builder()
                        .toCustomerId(request.toCustomerId())
                        .toCustomerEmail(request.toCustomerEmail())
                        .sender("VeriBoard")
                        .message(request.message())
                        .sentAt(LocalDateTime.now())
                        .build()
        );
        
        log.info("Notification saved successfully for customer {}", request.toCustomerId());
    }
}
