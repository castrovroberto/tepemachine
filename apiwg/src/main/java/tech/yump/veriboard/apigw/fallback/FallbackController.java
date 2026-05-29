package tech.yump.veriboard.apigw.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
public class FallbackController {

    @RequestMapping("/fallback/customer")
    public Mono<ResponseEntity<Map<String, Object>>> customerFallback() {
        log.warn("Customer service circuit breaker triggered — returning fallback response");
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "30")
                .body(Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "error", "Service Temporarily Unavailable",
                        "message", "Customer service is temporarily unavailable. Please try again later.",
                        "retryAfter", "30"
                )));
    }
}
