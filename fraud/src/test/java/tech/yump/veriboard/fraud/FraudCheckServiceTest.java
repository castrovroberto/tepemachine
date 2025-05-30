package tech.yump.veriboard.fraud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fraud Check Service Tests")
class FraudCheckServiceTest {

    @Mock
    private FraudCheckHistoryRepository fraudCheckHistoryRepository;
    
    @InjectMocks
    private FraudCheckService fraudCheckService;

    @Test
    @DisplayName("Should return false for fraud check and save history")
    void shouldReturnFalseAndSaveHistory() {
        // Given
        Integer customerId = 123;
        FraudCheckHistory savedHistory = FraudCheckHistory.builder()
            .id(1)
            .customerId(customerId)
            .isFraudster(false)
            .createdAt(LocalDateTime.now())
            .build();
        
        when(fraudCheckHistoryRepository.save(any(FraudCheckHistory.class)))
            .thenReturn(savedHistory);

        // When
        boolean result = fraudCheckService.isFraudulentCustomer(customerId);

        // Then
        assertThat(result).isFalse();
        
        verify(fraudCheckHistoryRepository).save(argThat(history -> 
            history.getCustomerId().equals(customerId) &&
            !history.getIsFraudster() &&
            history.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("Should handle different customer IDs correctly")
    void shouldHandleDifferentCustomerIds() {
        // Given
        Integer customerId1 = 456;
        Integer customerId2 = 789;
        
        when(fraudCheckHistoryRepository.save(any(FraudCheckHistory.class)))
            .thenReturn(FraudCheckHistory.builder().build());

        // When
        boolean result1 = fraudCheckService.isFraudulentCustomer(customerId1);
        boolean result2 = fraudCheckService.isFraudulentCustomer(customerId2);

        // Then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
        
        verify(fraudCheckHistoryRepository, times(2)).save(any(FraudCheckHistory.class));
    }
} 