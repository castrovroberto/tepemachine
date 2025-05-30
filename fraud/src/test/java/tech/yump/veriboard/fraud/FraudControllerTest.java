package tech.yump.veriboard.fraud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FraudController.class)
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudCheckService fraudCheckService;

    @Test
    void isFraudster_ShouldReturnFraudCheckResponse() throws Exception {
        // Given
        Integer customerId = 1;
        when(fraudCheckService.isFraudulentCustomer(customerId)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-check/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFraudster").value(false));
    }

    @Test
    void isFraudster_WithFraudulentCustomer_ShouldReturnTrue() throws Exception {
        // Given
        Integer customerId = 2;
        when(fraudCheckService.isFraudulentCustomer(customerId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-check/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFraudster").value(true));
    }

    @Test
    void isFraudster_WithDifferentCustomerId_ShouldCallService() throws Exception {
        // Given
        Integer customerId = 123;
        when(fraudCheckService.isFraudulentCustomer(customerId)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-check/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.isFraudster").exists());
    }
} 