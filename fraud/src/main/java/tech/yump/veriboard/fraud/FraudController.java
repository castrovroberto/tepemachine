package tech.yump.veriboard.fraud;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;

@RestController
@RequestMapping("api/v1/fraud-check")
@AllArgsConstructor
@Slf4j
public class FraudController {

    private final FraudCheckService fraudCheckService;

    @GetMapping(path = "{customerId}")
    @CircuitBreaker(name = "fraud-check", fallbackMethod = "fallbackFraudCheck")
    @Retry(name = "fraud-check")
    public FraudCheckResponse isFraudster(@PathVariable("customerId") Integer customerId) {
        log.info("Fraud check request for customer {}", customerId);
        
        boolean isFraudulentCustomer = fraudCheckService.isFraudulentCustomer(customerId);
        log.debug("Fraud check result for customer {}: {}", customerId, isFraudulentCustomer);
        return new FraudCheckResponse(isFraudulentCustomer);
    }
    
    /**
     * Fallback method for fraud check when service is degraded.
     * Returns a conservative response assuming potential fraud.
     */
    public FraudCheckResponse fallbackFraudCheck(Integer customerId, Exception ex) {
        log.warn("Fraud check service degraded for customer {}. Using fallback. Error: {}", 
                customerId, ex.getMessage());
        
        // Conservative approach: assume potential fraud during service degradation
        return new FraudCheckResponse(true);
    }
}
