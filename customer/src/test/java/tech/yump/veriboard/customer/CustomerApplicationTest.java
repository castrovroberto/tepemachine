package tech.yump.veriboard.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Customer Application Tests")
class CustomerApplicationTest {

    @Test
    @DisplayName("Should load application context successfully")
    void shouldLoadApplicationContextSuccessfully() {
        // This test verifies that the Spring Boot application context loads successfully
        // If the application context fails to load, this test will fail
        // This covers the main() method and basic application configuration
    }
} 