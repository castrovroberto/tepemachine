package tech.yump.veriboard.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification Entity Tests")
class NotificationTest {

    @Test
    @DisplayName("Should create notification using no-args constructor")
    void shouldCreateNotificationUsingNoArgsConstructor() {
        // When
        Notification notification = new Notification();

        // Then
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isNull();
        assertThat(notification.getToCustomerId()).isNull();
        assertThat(notification.getToCustomerEmail()).isNull();
        assertThat(notification.getSender()).isNull();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    @DisplayName("Should create notification using all-args constructor")
    void shouldCreateNotificationUsingAllArgsConstructor() {
        // Given
        Integer id = 1;
        Integer toCustomerId = 123;
        String toCustomerEmail = "customer@example.com";
        String sender = "VeriBoard";
        String message = "Welcome to our platform!";
        LocalDateTime sentAt = LocalDateTime.now();

        // When
        Notification notification = Notification.builder()
                .id(id)
                .toCustomerId(toCustomerId)
                .toCustomerEmail(toCustomerEmail)
                .sender(sender)
                .message(message)
                .sentAt(sentAt)
                .build();

        // Then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getToCustomerId()).isEqualTo(toCustomerId);
        assertThat(notification.getToCustomerEmail()).isEqualTo(toCustomerEmail);
        assertThat(notification.getSender()).isEqualTo(sender);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("Should create notification using builder pattern")
    void shouldCreateNotificationUsingBuilder() {
        // Given
        Integer id = 2;
        Integer toCustomerId = 456;
        String toCustomerEmail = "user@example.com";
        String sender = "System";
        String message = "Your account has been created";
        LocalDateTime sentAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        // When
        Notification notification = Notification.builder()
                .id(id)
                .toCustomerId(toCustomerId)
                .toCustomerEmail(toCustomerEmail)
                .sender(sender)
                .message(message)
                .sentAt(sentAt)
                .build();

        // Then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getToCustomerId()).isEqualTo(toCustomerId);
        assertThat(notification.getToCustomerEmail()).isEqualTo(toCustomerEmail);
        assertThat(notification.getSender()).isEqualTo(sender);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("Should create notification with null values using builder")
    void shouldCreateNotificationWithNullValuesUsingBuilder() {
        // When
        Notification notification = Notification.builder()
                .id(null)
                .toCustomerId(null)
                .toCustomerEmail(null)
                .sender(null)
                .message(null)
                .sentAt(null)
                .build();

        // Then
        assertThat(notification.getId()).isNull();
        assertThat(notification.getToCustomerId()).isNull();
        assertThat(notification.getToCustomerEmail()).isNull();
        assertThat(notification.getSender()).isNull();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    @DisplayName("Should set and get all fields correctly using setters and getters")
    void shouldSetAndGetAllFieldsCorrectly() {
        // Given
        Notification notification = new Notification();
        Integer id = 99;
        Integer toCustomerId = 777;
        String toCustomerEmail = "updated@example.com";
        String sender = "Updated Sender";
        String message = "Updated message";
        LocalDateTime sentAt = LocalDateTime.now();

        // When
        notification.setId(id);
        notification.setToCustomerId(toCustomerId);
        notification.setToCustomerEmail(toCustomerEmail);
        notification.setSender(sender);
        notification.setMessage(message);
        notification.setSentAt(sentAt);

        // Then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getToCustomerId()).isEqualTo(toCustomerId);
        assertThat(notification.getToCustomerEmail()).isEqualTo(toCustomerEmail);
        assertThat(notification.getSender()).isEqualTo(sender);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("Should implement equals correctly for same values")
    void shouldImplementEqualsCorrectlyForSameValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals correctly for different values")
    void shouldImplementEqualsCorrectlyForDifferentValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test1@example.com")
                .sender("System")
                .message("Message 1")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(2)
                .toCustomerId(456)
                .toCustomerEmail("test2@example.com")
                .sender("Admin")
                .message("Message 2")
                .sentAt(now.plusHours(1))
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification1.hashCode()).isNotEqualTo(notification2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals correctly when compared to null")
    void shouldImplementEqualsCorrectlyWhenComparedToNull() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(notification).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should implement equals correctly when compared to different class")
    void shouldImplementEqualsCorrectlyWhenComparedToDifferentClass() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        String notANotification = "Not a notification";

        // When & Then
        assertThat(notification).isNotEqualTo(notANotification);
    }

    @Test
    @DisplayName("Should implement equals correctly for same object reference")
    void shouldImplementEqualsCorrectlyForSameObjectReference() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(notification).isEqualTo(notification);
    }

    @Test
    @DisplayName("Should maintain consistent hashCode across multiple calls")
    void shouldMaintainConsistentHashCodeAcrossMultipleCalls() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // When
        int hashCode1 = notification.hashCode();
        int hashCode2 = notification.hashCode();
        int hashCode3 = notification.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
        assertThat(hashCode2).isEqualTo(hashCode3);
    }

    @Test
    @DisplayName("Should generate toString containing all field values")
    void shouldGenerateToStringContainingAllFieldValues() {
        // Given
        Notification notification = Notification.builder()
                .id(5)
                .toCustomerId(999)
                .toCustomerEmail("test@example.com")
                .sender("TestSender")
                .message("Test message")
                .sentAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        // When
        String toString = notification.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("Notification");
        assertThat(toString).contains("id=5");
        assertThat(toString).contains("toCustomerId=999");
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("TestSender");
        assertThat(toString).contains("Test message");
    }

    @Test
    @DisplayName("Should handle toString with null values")
    void shouldHandleToStringWithNullValues() {
        // Given
        Notification notification = new Notification();

        // When
        String toString = notification.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("Notification");
        assertThat(toString).contains("null");
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("")
                .sender("")
                .message("")
                .sentAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(notification.getToCustomerEmail()).isEqualTo("");
        assertThat(notification.getSender()).isEqualTo("");
        assertThat(notification.getMessage()).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle special characters in string fields")
    void shouldHandleSpecialCharactersInStringFields() {
        // Given
        String specialEmail = "test+123@sub-domain.example.cóm";
        String specialSender = "Sender with émojis 🎉";
        String specialMessage = "Message with special chars: àáâãäå & symbols: @#$%^&*()";

        // When
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail(specialEmail)
                .sender(specialSender)
                .message(specialMessage)
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(notification.getToCustomerEmail()).isEqualTo(specialEmail);
        assertThat(notification.getSender()).isEqualTo(specialSender);
        assertThat(notification.getMessage()).isEqualTo(specialMessage);
    }

    @Test
    @DisplayName("Should handle very long string values")
    void shouldHandleVeryLongStringValues() {
        // Given
        String longEmail = "very.long.email.address." + "a".repeat(100) + "@example.com";
        String longSender = "S".repeat(1000);
        String longMessage = "M".repeat(10000);

        // When
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail(longEmail)
                .sender(longSender)
                .message(longMessage)
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(notification.getToCustomerEmail()).isEqualTo(longEmail);
        assertThat(notification.getSender()).isEqualTo(longSender);
        assertThat(notification.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Should handle edge case ID values")
    void shouldHandleEdgeCaseIdValues() {
        // Given
        Integer maxId = Integer.MAX_VALUE;
        Integer maxCustomerId = Integer.MAX_VALUE;

        // When
        Notification notification = Notification.builder()
                .id(maxId)
                .toCustomerId(maxCustomerId)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(notification.getId()).isEqualTo(maxId);
        assertThat(notification.getToCustomerId()).isEqualTo(maxCustomerId);
    }

    @Test
    @DisplayName("Should handle negative ID values")
    void shouldHandleNegativeIdValues() {
        // Given
        Integer negativeId = -1;
        Integer negativeCustomerId = -999;

        // When
        Notification notification = Notification.builder()
                .id(negativeId)
                .toCustomerId(negativeCustomerId)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(notification.getId()).isEqualTo(negativeId);
        assertThat(notification.getToCustomerId()).isEqualTo(negativeCustomerId);
    }

    @Test
    @DisplayName("Should handle zero ID values")
    void shouldHandleZeroIdValues() {
        // Given
        Integer zeroId = 0;
        Integer zeroCustomerId = 0;

        // When
        Notification notification = Notification.builder()
                .id(zeroId)
                .toCustomerId(zeroCustomerId)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(notification.getId()).isEqualTo(zeroId);
        assertThat(notification.getToCustomerId()).isEqualTo(zeroCustomerId);
    }

    @Test
    @DisplayName("Should handle builder chaining correctly")
    void shouldHandleBuilderChainingCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        Notification notification = Notification.builder()
                .id(1)
                .id(2) // Overwrite previous value
                .toCustomerId(100)
                .toCustomerId(200) // Overwrite previous value
                .toCustomerEmail("first@example.com")
                .toCustomerEmail("second@example.com") // Overwrite previous value
                .sender("FirstSender")
                .sender("SecondSender") // Overwrite previous value
                .message("First message")
                .message("Second message") // Overwrite previous value
                .sentAt(now.minusHours(1))
                .sentAt(now) // Overwrite previous value
                .build();

        // Then - Should have the last set values
        assertThat(notification.getId()).isEqualTo(2);
        assertThat(notification.getToCustomerId()).isEqualTo(200);
        assertThat(notification.getToCustomerEmail()).isEqualTo("second@example.com");
        assertThat(notification.getSender()).isEqualTo("SecondSender");
        assertThat(notification.getMessage()).isEqualTo("Second message");
        assertThat(notification.getSentAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should handle partial builder usage")
    void shouldHandlePartialBuilderUsage() {
        // When - Only setting some fields
        Notification notification = Notification.builder()
                .toCustomerId(123)
                .message("Partial notification")
                .build();

        // Then
        assertThat(notification.getId()).isNull();
        assertThat(notification.getToCustomerId()).isEqualTo(123);
        assertThat(notification.getToCustomerEmail()).isNull();
        assertThat(notification.getSender()).isNull();
        assertThat(notification.getMessage()).isEqualTo("Partial notification");
        assertThat(notification.getSentAt()).isNull();
    }

    // Additional branch coverage tests for equals() and hashCode()
    
    @Test
    @DisplayName("Should handle equals with mixed null fields - id null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(null)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with mixed null fields - toCustomerId null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsToCustomerId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(null)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with mixed null fields - toCustomerEmail null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsToCustomerEmail() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail(null)
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with mixed null fields - sender null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsSender() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender(null)
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with mixed null fields - message null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsMessage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message(null)
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with mixed null fields - sentAt null vs non-null")
    void shouldHandleEqualsWithMixedNullFieldsSentAt() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(null)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification2).isNotEqualTo(notification1);
    }

    @Test
    @DisplayName("Should handle equals with both objects having null fields in same positions")
    void shouldHandleEqualsWithBothObjectsHavingNullFields() {
        // Given
        Notification notification1 = Notification.builder()
                .id(null)
                .toCustomerId(null)
                .toCustomerEmail(null)
                .sender(null)
                .message(null)
                .sentAt(null)
                .build();

        Notification notification2 = Notification.builder()
                .id(null)
                .toCustomerId(null)
                .toCustomerEmail(null)
                .sender(null)
                .message(null)
                .sentAt(null)
                .build();

        // When & Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with partial null fields matching")
    void shouldHandleEqualsWithPartialNullFieldsMatching() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(null)
                .toCustomerEmail("test@example.com")
                .sender(null)
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(null)
                .toCustomerEmail("test@example.com")
                .sender(null)
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    @DisplayName("Should handle hashCode consistency with null id")
    void shouldHandleHashCodeConsistencyWithNullId() {
        // Given
        Notification notification = Notification.builder()
                .id(null)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // When
        int hashCode1 = notification.hashCode();
        int hashCode2 = notification.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should handle hashCode consistency with null toCustomerId")
    void shouldHandleHashCodeConsistencyWithNullToCustomerId() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(null)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        // When
        int hashCode1 = notification.hashCode();
        int hashCode2 = notification.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should handle hashCode consistency with null string fields")
    void shouldHandleHashCodeConsistencyWithNullStringFields() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail(null)
                .sender(null)
                .message(null)
                .sentAt(LocalDateTime.now())
                .build();

        // When
        int hashCode1 = notification.hashCode();
        int hashCode2 = notification.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should handle hashCode consistency with null sentAt")
    void shouldHandleHashCodeConsistencyWithNullSentAt() {
        // Given
        Notification notification = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(null)
                .build();

        // When
        int hashCode1 = notification.hashCode();
        int hashCode2 = notification.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should handle equals with different id values only")
    void shouldHandleEqualsWithDifferentIdValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(2)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle equals with different toCustomerId values only")
    void shouldHandleEqualsWithDifferentToCustomerIdValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(456)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle equals with different toCustomerEmail values only")
    void shouldHandleEqualsWithDifferentToCustomerEmailValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test1@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test2@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle equals with different sender values only")
    void shouldHandleEqualsWithDifferentSenderValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("Admin")
                .message("Test message")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle equals with different message values only")
    void shouldHandleEqualsWithDifferentMessageValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Message 1")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Message 2")
                .sentAt(now)
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle equals with different sentAt values only")
    void shouldHandleEqualsWithDifferentSentAtValuesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now)
                .build();

        Notification notification2 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(now.plusHours(1))
                .build();

        // When & Then
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    @DisplayName("Should handle canEqual method coverage")
    void shouldHandleCanEqualMethodCoverage() {
        // Given
        Notification notification1 = Notification.builder()
                .id(1)
                .toCustomerId(123)
                .toCustomerEmail("test@example.com")
                .sender("System")
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        Notification notification2 = new Notification();

        // When & Then - This tests the canEqual method indirectly
        assertThat(notification1.equals(notification2)).isFalse();
        assertThat(notification2.equals(notification1)).isFalse();
    }
} 