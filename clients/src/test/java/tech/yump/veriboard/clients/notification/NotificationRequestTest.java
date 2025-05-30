package tech.yump.veriboard.clients.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    @Test
    void constructor_ShouldCreateInstanceWithCorrectValues() {
        // Given
        Integer customerId = 1;
        String email = "test@example.com";
        String message = "Welcome!";

        // When
        NotificationRequest request = new NotificationRequest(customerId, email, message);

        // Then
        assertEquals(customerId, request.toCustomerId());
        assertEquals(email, request.toCustomerEmail());
        assertEquals(message, request.message());
    }

    @Test
    void constructor_WithNullValues_ShouldCreateInstanceCorrectly() {
        // Given
        Integer customerId = null;
        String email = null;
        String message = null;

        // When
        NotificationRequest request = new NotificationRequest(customerId, email, message);

        // Then
        assertNull(request.toCustomerId());
        assertNull(request.toCustomerEmail());
        assertNull(request.message());
    }

    @Test
    void constructor_WithEmptyStrings_ShouldCreateInstanceCorrectly() {
        // Given
        Integer customerId = 123;
        String email = "";
        String message = "";

        // When
        NotificationRequest request = new NotificationRequest(customerId, email, message);

        // Then
        assertEquals(customerId, request.toCustomerId());
        assertEquals("", request.toCustomerEmail());
        assertEquals("", request.message());
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        NotificationRequest request1 = new NotificationRequest(1, "test@example.com", "Welcome!");
        NotificationRequest request2 = new NotificationRequest(1, "test@example.com", "Welcome!");

        // When & Then
        assertEquals(request1, request2);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        NotificationRequest request1 = new NotificationRequest(1, "test@example.com", "Welcome!");
        NotificationRequest request2 = new NotificationRequest(2, "test@example.com", "Welcome!");

        // When & Then
        assertNotEquals(request1, request2);
    }

    @Test
    void hashCode_ShouldBeConsistentForSameValues() {
        // Given
        NotificationRequest request1 = new NotificationRequest(1, "test@example.com", "Welcome!");
        NotificationRequest request2 = new NotificationRequest(1, "test@example.com", "Welcome!");

        // When & Then
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void toString_ShouldContainAllFieldValues() {
        // Given
        NotificationRequest request = new NotificationRequest(123, "user@test.com", "Hello World");

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("user@test.com"));
        assertTrue(toString.contains("Hello World"));
        assertTrue(toString.contains("NotificationRequest"));
    }

    @Test
    void accessors_ShouldReturnCorrectValues() {
        // Given
        Integer customerId = 456;
        String email = "customer@example.com";
        String message = "Test notification message";
        NotificationRequest request = new NotificationRequest(customerId, email, message);

        // When & Then
        assertEquals(customerId, request.toCustomerId());
        assertEquals(email, request.toCustomerEmail());
        assertEquals(message, request.message());
    }
} 