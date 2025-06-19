package tech.yump.veriboard.customer.infrastructure.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;
import tech.yump.veriboard.clients.fraud.FraudClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fraud Check Service Adapter Tests")
class FraudCheckServiceAdapterTest {

    @Mock
    private FraudClient fraudClient;

    @InjectMocks
    private FraudCheckServiceAdapter fraudCheckServiceAdapter;

    @Test
    @DisplayName("Should return true when customer is fraudulent")
    void shouldReturnTrueWhenCustomerIsFraudulent() {
        // Given
        Integer customerId = 1;
        FraudCheckResponse fraudResponse = new FraudCheckResponse(true);
        when(fraudClient.isFraudster(customerId)).thenReturn(fraudResponse);

        // When
        boolean result = fraudCheckServiceAdapter.isFraudulent(customerId);

        // Then
        assertThat(result).isTrue();
        verify(fraudClient).isFraudster(customerId);
    }

    @Test
    @DisplayName("Should return false when customer is not fraudulent")
    void shouldReturnFalseWhenCustomerIsNotFraudulent() {
        // Given
        Integer customerId = 2;
        FraudCheckResponse fraudResponse = new FraudCheckResponse(false);
        when(fraudClient.isFraudster(customerId)).thenReturn(fraudResponse);

        // When
        boolean result = fraudCheckServiceAdapter.isFraudulent(customerId);

        // Then
        assertThat(result).isFalse();
        verify(fraudClient).isFraudster(customerId);
    }

    @Test
    @DisplayName("Should handle fraud client exceptions")
    void shouldHandleFraudClientExceptions() {
        // Given
        Integer customerId = 3;
        when(fraudClient.isFraudster(customerId))
            .thenThrow(new RuntimeException("Fraud service unavailable"));

        // When & Then
        assertThatThrownBy(() -> fraudCheckServiceAdapter.isFraudulent(customerId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Fraud service unavailable");

        verify(fraudClient).isFraudster(customerId);
    }

    @Test
    @DisplayName("Should handle null customer id")
    void shouldHandleNullCustomerId() {
        // Given
        Integer customerId = null;
        when(fraudClient.isFraudster(customerId))
            .thenThrow(new IllegalArgumentException("Customer ID cannot be null"));

        // When & Then
        assertThatThrownBy(() -> fraudCheckServiceAdapter.isFraudulent(customerId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID cannot be null");

        verify(fraudClient).isFraudster(customerId);
    }

    @Test
    @DisplayName("Should handle various customer IDs correctly")
    void shouldHandleVariousCustomerIdsCorrectly() {
        // Given
        Integer customerId1 = 100;
        Integer customerId2 = 999;
        
        FraudCheckResponse response1 = new FraudCheckResponse(true);
        FraudCheckResponse response2 = new FraudCheckResponse(false);
        
        when(fraudClient.isFraudster(customerId1)).thenReturn(response1);
        when(fraudClient.isFraudster(customerId2)).thenReturn(response2);

        // When
        boolean result1 = fraudCheckServiceAdapter.isFraudulent(customerId1);
        boolean result2 = fraudCheckServiceAdapter.isFraudulent(customerId2);

        // Then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
        
        verify(fraudClient).isFraudster(customerId1);
        verify(fraudClient).isFraudster(customerId2);
    }
} 