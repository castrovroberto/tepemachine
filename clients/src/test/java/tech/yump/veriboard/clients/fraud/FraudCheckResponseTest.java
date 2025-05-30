package tech.yump.veriboard.clients.fraud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraudCheckResponseTest {

    @Test
    void constructor_ShouldCreateInstanceWithCorrectValue() {
        // Given
        Boolean isFraudster = true;

        // When
        FraudCheckResponse response = new FraudCheckResponse(isFraudster);

        // Then
        assertEquals(isFraudster, response.isFraudster());
    }

    @Test
    void constructor_WithFalse_ShouldCreateInstanceCorrectly() {
        // Given
        Boolean isFraudster = false;

        // When
        FraudCheckResponse response = new FraudCheckResponse(isFraudster);

        // Then
        assertEquals(isFraudster, response.isFraudster());
        assertFalse(response.isFraudster());
    }

    @Test
    void constructor_WithNull_ShouldCreateInstanceCorrectly() {
        // Given
        Boolean isFraudster = null;

        // When
        FraudCheckResponse response = new FraudCheckResponse(isFraudster);

        // Then
        assertNull(response.isFraudster());
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        FraudCheckResponse response1 = new FraudCheckResponse(true);
        FraudCheckResponse response2 = new FraudCheckResponse(true);

        // When & Then
        assertEquals(response1, response2);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        FraudCheckResponse response1 = new FraudCheckResponse(true);
        FraudCheckResponse response2 = new FraudCheckResponse(false);

        // When & Then
        assertNotEquals(response1, response2);
    }

    @Test
    void hashCode_ShouldBeConsistentForSameValues() {
        // Given
        FraudCheckResponse response1 = new FraudCheckResponse(true);
        FraudCheckResponse response2 = new FraudCheckResponse(true);

        // When & Then
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValue() {
        // Given
        FraudCheckResponse response = new FraudCheckResponse(true);

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("FraudCheckResponse"));
    }
} 