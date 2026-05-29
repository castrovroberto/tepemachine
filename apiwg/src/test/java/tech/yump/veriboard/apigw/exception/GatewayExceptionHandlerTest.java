package tech.yump.veriboard.apigw.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeoutException;

class GatewayExceptionHandlerTest {

    private GatewayExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GatewayExceptionHandler(new ObjectMapper());
    }

    @Test
    void handle_callNotPermitted_returns503() {
        MockServerWebExchange exchange = exchangeFor("/api/v1/customers");
        CallNotPermittedException ex = CallNotPermittedException.createCallNotPermittedException(
                CircuitBreaker.ofDefaults("test-cb")
        );

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE;
        assert "30".equals(exchange.getResponse().getHeaders().getFirst("Retry-After"));
        assert MediaType.APPLICATION_JSON.equals(exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void handle_timeout_returns504() {
        MockServerWebExchange exchange = exchangeFor("/api/v1/customers");

        StepVerifier.create(handler.handle(exchange, new TimeoutException("upstream timeout")))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.GATEWAY_TIMEOUT;
    }

    @Test
    void handle_responseStatusException_propagatesStatus() {
        MockServerWebExchange exchange = exchangeFor("/api/v1/customers");
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    void handle_unexpectedException_returns500() {
        MockServerWebExchange exchange = exchangeFor("/api/v1/customers");

        StepVerifier.create(handler.handle(exchange, new RuntimeException("boom")))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static MockServerWebExchange exchangeFor(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }
}
