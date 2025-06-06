package tech.yump.veriboard.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Idempotent notification service that prevents duplicate message processing.
 * Implements idempotency using unique constraint on business keys.
 */
@Service
@Slf4j
public class IdempotentNotificationService {

    private final NotificationRepository notificationRepository;

    public IdempotentNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Processes notification request idempotently.
     * Uses MD5 hash of business keys to ensure idempotency.
     */
    @Transactional
    public void send(NotificationRequest request) {
        String idempotencyKey = generateIdempotencyKey(request);
        
        log.info("Processing notification request with idempotency key: {}", idempotencyKey);

        try {
            // Try to find existing notification
            Optional<Notification> existing = findByIdempotencyKey(idempotencyKey);
            
            if (existing.isPresent()) {
                log.info("Notification already processed for key: {}. Skipping.", idempotencyKey);
                return;
            }

            // Create and save new notification
            Notification notification = Notification.builder()
                    .toCustomerId(request.toCustomerId())
                    .toCustomerEmail(request.toCustomerEmail())
                    .sender("VeriBoard")
                    .message(request.message())
                    .sentAt(LocalDateTime.now())
                    .idempotencyKey(idempotencyKey)
                    .build();

            notificationRepository.save(notification);
            
            // Here you would actually send the email/SMS
            sendActualNotification(notification);
            
            log.info("Notification processed successfully for customer: {}", request.toCustomerId());

        } catch (DataIntegrityViolationException e) {
            // Handle race condition where duplicate key constraint is violated
            log.warn("Duplicate notification detected during processing for key: {}. " +
                    "Another thread processed this request.", idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to process notification for customer: {}. Error: {}", 
                     request.toCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generates idempotency key based on business significance.
     * Uses customer ID, email, and message content to ensure uniqueness.
     */
    private String generateIdempotencyKey(NotificationRequest request) {
        String businessKey = String.format("%s:%s:%s", 
                request.toCustomerId(), 
                request.toCustomerEmail(), 
                hashMessage(request.message()));
        
        return hashString(businessKey);
    }

    /**
     * Creates a hash of the message content to include in idempotency key.
     * This ensures same customer doesn't get identical messages multiple times.
     */
    private String hashMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "empty";
        }
        
        // Take first 50 characters for message fingerprint
        String messageFingerprint = message.length() > 50 ? 
            message.substring(0, 50) : message;
            
        return hashString(messageFingerprint);
    }

    /**
     * Generates MD5 hash of input string.
     */
    private String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available", e);
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * Finds notification by idempotency key.
     * This would require adding a findByIdempotencyKey method to the repository.
     */
    private Optional<Notification> findByIdempotencyKey(String idempotencyKey) {
        // This would need to be implemented in the repository
        // return notificationRepository.findByIdempotencyKey(idempotencyKey);
        
        // For now, return empty to demonstrate the pattern
        return Optional.empty();
    }

    /**
     * Placeholder for actual notification sending logic.
     * This could integrate with email services, SMS providers, etc.
     */
    private void sendActualNotification(Notification notification) {
        log.info("Sending notification to: {} - Message: {}", 
                notification.getToCustomerEmail(), 
                notification.getMessage());
        
        // Implement actual notification sending here
        // - Email service integration
        // - SMS service integration
        // - Push notification service
    }
} 