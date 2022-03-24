package tech.yump.msapp.notification;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.yump.msapp.clients.notification.NotificationRequest;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(NotificationRequest request) {
        notificationRepository.save(
                Notification.builder()
                        .toCustomerId(request.toCustomerId())
                        .toCustomerEmail(request.toCustomerName())
                        .sender("Yump Technologies")
                        .message(request.message())
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }


}
