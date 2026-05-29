package tech.yump.veriboard.apigw.fallback;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void customerFallback_returnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/customer")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader().exists("Retry-After")
                .expectHeader().valueEquals("Retry-After", "30")
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Temporarily Unavailable")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.retryAfter").isEqualTo("30")
                .jsonPath("$.timestamp").isNotEmpty();
    }

    @Test
    void customerFallback_respondsToPostRequests() {
        webTestClient.post()
                .uri("/fallback/customer")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503);
    }
}
