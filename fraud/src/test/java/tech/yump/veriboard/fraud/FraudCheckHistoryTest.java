package tech.yump.veriboard.fraud;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FraudCheckHistoryTest {

    @Test
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
} 