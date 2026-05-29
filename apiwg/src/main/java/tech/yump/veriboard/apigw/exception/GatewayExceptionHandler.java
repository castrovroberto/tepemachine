package tech.yump.veriboard.apigw.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeoutException;

// Order(-1) ensures this runs before Spring Boot's DefaultErrorWebExceptionHandler
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String error;
        String message;

        if (ex instanceof CallNotPermittedException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            error = "Service Temporarily Unavailable";
            message = "The service is temporarily unavailable. Please try again later.";
            log.warn("Circuit breaker open for request {}: {}", exchange.getRequest().getPath(), ex.getMessage());
            exchange.getResponse().getHeaders().set("Retry-After", "30");

        } else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            error = "Gateway Timeout";
            message = "The upstream service did not respond in time. Please try again.";
            log.warn("Timeout for request {}: {}", exchange.getRequest().getPath(), ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            error = status.getReasonPhrase();
            message = rse.getReason() != null ? rse.getReason() : error;
            log.warn("Response status exception for request {}: {}", exchange.getRequest().getPath(), ex.getMessage());

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Internal Server Error";
            message = "An unexpected error occurred. Please try again later.";
            log.error("Unhandled gateway exception for request {}", exchange.getRequest().getPath(), ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", error,
                "message", message,
                "path", exchange.getRequest().getPath().value()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            bytes = "{\"error\":\"Internal Server Error\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
