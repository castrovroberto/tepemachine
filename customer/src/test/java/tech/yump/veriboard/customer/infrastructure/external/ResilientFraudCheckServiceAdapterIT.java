package tech.yump.veriboard.customer.infrastructure.external;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import tech.yump.veriboard.clients.fraud.FraudCheckResponse;
import tech.yump.veriboard.clients.fraud.FraudClient;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ResilientFraudCheckServiceAdapter.
 *
 * Two complementary test strategies:
 *
 * 1. Spring proxy tests — verify that the @CircuitBreaker fallback method fires when
 *    FraudClient throws, exercising the full AOP chain through the Spring context.
 *
 * 2. CircuitBreakerRegistry unit tests — verify state machine transitions (CLOSED →
 *    OPEN → HALF_OPEN → CLOSED) against a programmatically configured breaker, fast
 *    and without full context overhead.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Disable retry so fallback tests don't incur backoff delay
        "resilience4j.retry.instances.fraud-service.max-attempts=1",
        // Fast-tripping circuit breaker for proxy-based fallback tests
        "resilience4j.circuitbreaker.instances.fraud-service.sliding-window-size=3",
        "resilience4j.circuitbreaker.instances.fraud-service.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.fraud-service.failure-rate-threshold=100",
        "resilience4j.circuitbreaker.instances.fraud-service.wait-duration-in-open-state=1s",
        "resilience4j.circuitbreaker.instances.fraud-service.record-exceptions=java.lang.RuntimeException"
})
@DisplayName("ResilientFraudCheckServiceAdapter - Resilience Integration Tests")
class ResilientFraudCheckServiceAdapterIT {

    // --- Spring proxy tests (fallback behaviour) ---

    @Autowired
    private ResilientFraudCheckServiceAdapter adapter;

    @MockBean
    private FraudClient fraudClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("fraud-service").reset();
    }

    @Test
    @DisplayName("Returns actual fraud-check result when service is healthy")
    void isFraudulent_healthyService_returnsActualResult() {
        when(fraudClient.isFraudster(1)).thenReturn(new FraudCheckResponse(false));
        when(fraudClient.isFraudster(2)).thenReturn(new FraudCheckResponse(true));

        assertThat(adapter.isFraudulent(1)).isFalse();
        assertThat(adapter.isFraudulent(2)).isTrue();
    }

    @Test
    @DisplayName("Fallback returns false for low-risk IDs when service throws")
    void isFraudulent_serviceThrows_fallbackReturnsFalseForLowRisk() {
        when(fraudClient.isFraudster(anyInt()))
                .thenThrow(new RuntimeException("fraud service down"));

        assertThat(adapter.isFraudulent(42)).isFalse();
    }

    @Test
    @DisplayName("Fallback returns true for high-risk IDs (>999999) when service throws")
    void isFraudulent_serviceThrows_fallbackReturnsTrueForHighRisk() {
        when(fraudClient.isFraudster(anyInt()))
                .thenThrow(new RuntimeException("fraud service down"));

        assertThat(adapter.isFraudulent(1_000_000)).isTrue();
    }

    @Test
    @DisplayName("Negative customer IDs are treated as high-risk in fallback")
    void isFraudulent_serviceThrows_negativeIdIsHighRisk() {
        when(fraudClient.isFraudster(anyInt()))
                .thenThrow(new RuntimeException("fraud service down"));

        assertThat(adapter.isFraudulent(-1)).isTrue();
    }

    // --- CircuitBreakerRegistry tests (state machine) ---

    @Test
    @DisplayName("Circuit breaker opens after minimum calls exceed failure threshold")
    void circuitBreaker_repeatedFailures_transitionsToOpen() {
        CircuitBreaker cb = buildFastTripBreaker("cb-open-test");

        Supplier<Boolean> failing = CircuitBreaker.decorateSupplier(cb,
                () -> { throw new RuntimeException("down"); });

        for (int i = 0; i < 3; i++) {
            try { failing.get(); } catch (Exception ignored) {}
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("Open circuit breaker rejects calls with CallNotPermittedException")
    void circuitBreaker_openState_rejectsCallsImmediately() {
        CircuitBreaker cb = buildFastTripBreaker("cb-reject-test");
        cb.transitionToOpenState();

        Supplier<Boolean> decorated = CircuitBreaker.decorateSupplier(cb, () -> true);

        assertThatThrownBy(decorated::get)
                .isInstanceOf(CallNotPermittedException.class);
        verify(fraudClient, never()).isFraudster(anyInt());
    }

    @Test
    @DisplayName("Circuit breaker transitions to HALF_OPEN after wait duration")
    void circuitBreaker_afterWaitDuration_transitionsToHalfOpen() throws InterruptedException {
        CircuitBreaker cb = buildFastTripBreaker("cb-halfopen-test");
        cb.transitionToOpenState();

        Thread.sleep(1100); // wait-duration-in-open-state = 1s

        // Any call attempt triggers the OPEN → HALF_OPEN transition
        Supplier<Boolean> decorated = CircuitBreaker.decorateSupplier(cb, () -> true);
        decorated.get();

        assertThat(cb.getState()).isNotEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("Circuit breaker closes after successful calls in HALF_OPEN")
    void circuitBreaker_successInHalfOpen_transitionsToClosed() {
        CircuitBreaker cb = buildFastTripBreaker("cb-close-test");
        cb.transitionToOpenState();
        cb.transitionToHalfOpenState();

        Supplier<Boolean> decorated = CircuitBreaker.decorateSupplier(cb, () -> true);

        // permitted-number-of-calls-in-half-open-state = 2 in our config
        decorated.get();
        decorated.get();

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    // Builds a programmatic circuit breaker that mirrors the fast-trip settings used in
    // the @TestPropertySource above, for direct state machine tests without the Spring proxy.
    private CircuitBreaker buildFastTripBreaker(String name) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(3)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(100)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordException(e -> true)
                .build();
        return CircuitBreakerRegistry.of(config).circuitBreaker(name);
    }
}
