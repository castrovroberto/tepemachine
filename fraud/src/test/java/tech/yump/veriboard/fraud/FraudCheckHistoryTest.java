package tech.yump.veriboard.fraud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fraud Check History Tests")
class FraudCheckHistoryTest {

    @Test
    @DisplayName("Should create instance using builder with correct values")
    void builder_ShouldCreateInstanceWithCorrectValues() {
        // Given
        Integer id = 1;
        Integer customerId = 123;
        Boolean isFraudster = false;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(id)
                .customerId(customerId)
                .isFraudster(isFraudster)
                .createdAt(createdAt)
                .build();

        // Then
        assertEquals(id, history.getId());
        assertEquals(customerId, history.getCustomerId());
        assertEquals(isFraudster, history.getIsFraudster());
        assertEquals(createdAt, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should create instance using all-args constructor")
    void constructor_ShouldCreateInstanceWithAllFields() {
        // Given
        Integer id = 2;
        Integer customerId = 456;
        Boolean isFraudster = true;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        FraudCheckHistory history = new FraudCheckHistory(id, customerId, isFraudster, createdAt);

        // Then
        assertEquals(id, history.getId());
        assertEquals(customerId, history.getCustomerId());
        assertEquals(isFraudster, history.getIsFraudster());
        assertEquals(createdAt, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should create empty instance using default constructor")
    void defaultConstructor_ShouldCreateEmptyInstance() {
        // When
        FraudCheckHistory history = new FraudCheckHistory();

        // Then
        assertNull(history.getId());
        assertNull(history.getCustomerId());
        assertNull(history.getIsFraudster());
        assertNull(history.getCreatedAt());
    }

    @Test
    @DisplayName("Should update fields correctly using setters")
    void setters_ShouldUpdateFieldsCorrectly() {
        // Given
        FraudCheckHistory history = new FraudCheckHistory();
        Integer id = 3;
        Integer customerId = 789;
        Boolean isFraudster = false;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        history.setId(id);
        history.setCustomerId(customerId);
        history.setIsFraudster(isFraudster);
        history.setCreatedAt(createdAt);

        // Then
        assertEquals(id, history.getId());
        assertEquals(customerId, history.getCustomerId());
        assertEquals(isFraudster, history.getIsFraudster());
        assertEquals(createdAt, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should return correct values using getters")
    void getters_ShouldReturnCorrectValues() {
        // Given
        Integer id = 4;
        Integer customerId = 101112;
        Boolean isFraudster = true;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(id)
                .customerId(customerId)
                .isFraudster(isFraudster)
                .createdAt(createdAt)
                .build();

        // When & Then
        assertEquals(id, history.getId());
        assertEquals(customerId, history.getCustomerId());
        assertEquals(isFraudster, history.getIsFraudster());
        assertEquals(createdAt, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should create instance with null values using builder")
    void builder_WithNullValues_ShouldCreateInstanceCorrectly() {
        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(null)
                .customerId(null)
                .isFraudster(null)
                .createdAt(null)
                .build();

        // Then
        assertNull(history.getId());
        assertNull(history.getCustomerId());
        assertNull(history.getIsFraudster());
        assertNull(history.getCreatedAt());
    }

    @Test
    @DisplayName("Should return true for objects with same values")
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FraudCheckHistory history1 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        FraudCheckHistory history2 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        // When & Then
        assertEquals(history1, history2);
    }

    @Test
    @DisplayName("Should return false for objects with different values")
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FraudCheckHistory history1 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        FraudCheckHistory history2 = FraudCheckHistory.builder()
                .id(2)
                .customerId(456)
                .isFraudster(true)
                .createdAt(now.plusHours(1))
                .build();

        // When & Then
        assertNotEquals(history1, history2);
    }

    @Test
    @DisplayName("Should return false when compared to null")
    void equals_ShouldReturnFalseWhenComparedToNull() {
        // Given
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then
        assertNotEquals(history, null);
    }

    @Test
    @DisplayName("Should return false when compared to different class")
    void equals_ShouldReturnFalseWhenComparedToDifferentClass() {
        // Given
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        String notAHistory = "Not a FraudCheckHistory";

        // When & Then
        assertNotEquals(history, notAHistory);
    }

    @Test
    @DisplayName("Should return true when compared to same object reference")
    void equals_ShouldReturnTrueForSameObjectReference() {
        // Given
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then
        assertEquals(history, history);
    }

    @Test
    @DisplayName("Should have equal hashCodes for objects with same values")
    void hashCode_ShouldBeConsistentForSameValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FraudCheckHistory history1 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        FraudCheckHistory history2 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        // When & Then
        assertEquals(history1.hashCode(), history2.hashCode());
    }

    @Test
    @DisplayName("Should have different hashCodes for objects with different values")
    void hashCode_ShouldBeDifferentForDifferentValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FraudCheckHistory history1 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        FraudCheckHistory history2 = FraudCheckHistory.builder()
                .id(2)
                .customerId(456)
                .isFraudster(true)
                .createdAt(now.plusHours(1))
                .build();

        // When & Then
        assertNotEquals(history1.hashCode(), history2.hashCode());
    }

    @Test
    @DisplayName("Should maintain consistent hashCode across multiple calls")
    void hashCode_ShouldBeConsistentAcrossMultipleCalls() {
        // Given
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        int hashCode1 = history.hashCode();
        int hashCode2 = history.hashCode();
        int hashCode3 = history.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
        assertEquals(hashCode2, hashCode3);
    }

    @Test
    @DisplayName("Should contain all field values in toString")
    void toString_ShouldContainAllFieldValues() {
        // Given
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(5)
                .customerId(999)
                .isFraudster(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        // When
        String toString = history.toString();

        // Then
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("999"));
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("FraudCheckHistory"));
    }

    @Test
    @DisplayName("Should handle toString with null values")
    void toString_ShouldHandleNullValues() {
        // Given
        FraudCheckHistory history = new FraudCheckHistory();

        // When
        String toString = history.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("FraudCheckHistory"));
        assertTrue(toString.contains("null"));
    }

    @Test
    @DisplayName("Should handle edge case values correctly")
    void shouldHandleEdgeCaseValues() {
        // Given
        Integer maxId = Integer.MAX_VALUE;
        Integer maxCustomerId = Integer.MAX_VALUE;
        LocalDateTime futureDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(maxId)
                .customerId(maxCustomerId)
                .isFraudster(true)
                .createdAt(futureDate)
                .build();

        // Then
        assertEquals(maxId, history.getId());
        assertEquals(maxCustomerId, history.getCustomerId());
        assertTrue(history.getIsFraudster());
        assertEquals(futureDate, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle negative ID values")
    void shouldHandleNegativeIdValues() {
        // Given
        Integer negativeId = -1;
        Integer negativeCustomerId = -999;

        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(negativeId)
                .customerId(negativeCustomerId)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals(negativeId, history.getId());
        assertEquals(negativeCustomerId, history.getCustomerId());
        assertFalse(history.getIsFraudster());
    }

    @Test
    @DisplayName("Should handle zero values")
    void shouldHandleZeroValues() {
        // Given
        Integer zeroId = 0;
        Integer zeroCustomerId = 0;

        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(zeroId)
                .customerId(zeroCustomerId)
                .isFraudster(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals(zeroId, history.getId());
        assertEquals(zeroCustomerId, history.getCustomerId());
        assertFalse(history.getIsFraudster());
    }

    @Test
    @DisplayName("Should handle partial builder usage")
    void shouldHandlePartialBuilderUsage() {
        // When - Only setting some fields
        FraudCheckHistory history = FraudCheckHistory.builder()
                .customerId(123)
                .isFraudster(true)
                .build();

        // Then
        assertNull(history.getId());
        assertEquals(123, history.getCustomerId());
        assertTrue(history.getIsFraudster());
        assertNull(history.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle builder chaining correctly")
    void shouldHandleBuilderChainingCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        FraudCheckHistory history = FraudCheckHistory.builder()
                .id(1)
                .id(2) // Overwrite previous value
                .customerId(100)
                .customerId(200) // Overwrite previous value
                .isFraudster(false)
                .isFraudster(true) // Overwrite previous value
                .createdAt(now.minusHours(1))
                .createdAt(now) // Overwrite previous value
                .build();

        // Then - Should have the last set values
        assertEquals(2, history.getId());
        assertEquals(200, history.getCustomerId());
        assertTrue(history.getIsFraudster());
        assertEquals(now, history.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle equals with objects having only some fields different")
    void equals_ShouldHandlePartialDifferences() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // Same except for isFraudster
        FraudCheckHistory history1 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(false)
                .createdAt(now)
                .build();

        FraudCheckHistory history2 = FraudCheckHistory.builder()
                .id(1)
                .customerId(123)
                .isFraudster(true)
                .createdAt(now)
                .build();

        // When & Then
        assertNotEquals(history1, history2);
        assertNotEquals(history1.hashCode(), history2.hashCode());
    }
}