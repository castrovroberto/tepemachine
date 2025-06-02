package tech.yump.veriboard.customer.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.yump.veriboard.customer.domain.Customer;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JPA Customer Entity Tests")
class JpaCustomerEntityTest {

    @Test
    @DisplayName("Should create entity using no-args constructor")
    void shouldCreateEntityUsingNoArgsConstructor() {
        // When
        JpaCustomerEntity entity = new JpaCustomerEntity();

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFirstName()).isNull();
        assertThat(entity.getLastName()).isNull();
        assertThat(entity.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create entity using all-args constructor")
    void shouldCreateEntityUsingAllArgsConstructor() {
        // When
        JpaCustomerEntity entity = new JpaCustomerEntity(1, "John", "Doe", "john.doe@example.com");

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should create entity using builder pattern")
    void shouldCreateEntityUsingBuilder() {
        // When
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(2)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2);
        assertThat(entity.getFirstName()).isEqualTo("Jane");
        assertThat(entity.getLastName()).isEqualTo("Smith");
        assertThat(entity.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should create entity using builder with null values")
    void shouldCreateEntityUsingBuilderWithNullValues() {
        // When
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(null)
                .firstName(null)
                .lastName(null)
                .email(null)
                .build();

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFirstName()).isNull();
        assertThat(entity.getLastName()).isNull();
        assertThat(entity.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should convert entity to domain object correctly")
    void shouldConvertToDomainCorrectly() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When
        Customer customer = entity.toDomain();

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isEqualTo(1);
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should convert entity to domain with null ID")
    void shouldConvertToDomainWithNullId() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(null)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When
        Customer customer = entity.toDomain();

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isNull();
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should convert domain object to entity correctly")
    void shouldConvertFromDomainCorrectly() {
        // Given
        Customer customer = new Customer(1, "Jane", "Smith", "jane.smith@example.com");

        // When
        JpaCustomerEntity entity = JpaCustomerEntity.fromDomain(customer);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getFirstName()).isEqualTo("Jane");
        assertThat(entity.getLastName()).isEqualTo("Smith");
        assertThat(entity.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should convert domain object with null ID to entity")
    void shouldConvertFromDomainWithNullId() {
        // Given
        Customer customer = new Customer("New", "Customer", "new.customer@example.com");

        // When
        JpaCustomerEntity entity = JpaCustomerEntity.fromDomain(customer);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFirstName()).isEqualTo("New");
        assertThat(entity.getLastName()).isEqualTo("Customer");
        assertThat(entity.getEmail()).isEqualTo("new.customer@example.com");
    }

    @Test
    @DisplayName("Should handle round-trip conversion correctly")
    void shouldHandleRoundTripConversion() {
        // Given
        Customer originalCustomer = new Customer(5, "Test", "User", "test.user@example.com");

        // When
        JpaCustomerEntity entity = JpaCustomerEntity.fromDomain(originalCustomer);
        Customer convertedCustomer = entity.toDomain();

        // Then
        assertThat(convertedCustomer.getId()).isEqualTo(originalCustomer.getId());
        assertThat(convertedCustomer.getFirstName()).isEqualTo(originalCustomer.getFirstName());
        assertThat(convertedCustomer.getLastName()).isEqualTo(originalCustomer.getLastName());
        assertThat(convertedCustomer.getEmail()).isEqualTo(originalCustomer.getEmail());
    }

    @Test
    @DisplayName("Should implement equals correctly for same values")
    void shouldImplementEqualsCorrectlyForSameValues() {
        // Given
        JpaCustomerEntity entity1 = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        JpaCustomerEntity entity2 = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When & Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals correctly for different values")
    void shouldImplementEqualsCorrectlyForDifferentValues() {
        // Given
        JpaCustomerEntity entity1 = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        JpaCustomerEntity entity2 = JpaCustomerEntity.builder()
                .id(2)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        // When & Then
        assertThat(entity1).isNotEqualTo(entity2);
        assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals correctly when compared to null")
    void shouldImplementEqualsCorrectlyWhenComparedToNull() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When & Then
        assertThat(entity).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should implement equals correctly when compared to different class")
    void shouldImplementEqualsCorrectlyWhenComparedToDifferentClass() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        String notAnEntity = "Not an entity";

        // When & Then
        assertThat(entity).isNotEqualTo(notAnEntity);
    }

    @Test
    @DisplayName("Should implement equals correctly for same object reference")
    void shouldImplementEqualsCorrectlyForSameObjectReference() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When & Then
        assertThat(entity).isEqualTo(entity);
    }

    @Test
    @DisplayName("Should generate toString correctly")
    void shouldGenerateToStringCorrectly() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When
        String toString = entity.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("JpaCustomerEntity");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("firstName=John");
        assertThat(toString).contains("lastName=Doe");
        assertThat(toString).contains("email=john.doe@example.com");
    }

    @Test
    @DisplayName("Should generate toString correctly with null values")
    void shouldGenerateToStringCorrectlyWithNullValues() {
        // Given
        JpaCustomerEntity entity = new JpaCustomerEntity();

        // When
        String toString = entity.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("JpaCustomerEntity");
        assertThat(toString).contains("id=null");
        assertThat(toString).contains("firstName=null");
        assertThat(toString).contains("lastName=null");
        assertThat(toString).contains("email=null");
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void shouldSetAndGetAllFieldsCorrectly() {
        // Given
        JpaCustomerEntity entity = new JpaCustomerEntity();

        // When
        entity.setId(99);
        entity.setFirstName("Updated");
        entity.setLastName("Name");
        entity.setEmail("updated@example.com");

        // Then
        assertThat(entity.getId()).isEqualTo(99);
        assertThat(entity.getFirstName()).isEqualTo("Updated");
        assertThat(entity.getLastName()).isEqualTo("Name");
        assertThat(entity.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("")
                .lastName("")
                .email("")
                .build();

        // When
        Customer customer = entity.toDomain();

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isEqualTo(1);
        assertThat(customer.getFirstName()).isEqualTo("");
        assertThat(customer.getLastName()).isEqualTo("");
        assertThat(customer.getEmail()).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void shouldHandleSpecialCharactersInFields() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("José")
                .lastName("Müller-García")
                .email("josé.müller@example.cóm")
                .build();

        // When
        Customer customer = entity.toDomain();

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getFirstName()).isEqualTo("José");
        assertThat(customer.getLastName()).isEqualTo("Müller-García");
        assertThat(customer.getEmail()).isEqualTo("josé.müller@example.cóm");
    }

    @Test
    @DisplayName("Should handle very long field values")
    void shouldHandleVeryLongFieldValues() {
        // Given
        String longName = "A".repeat(1000);
        String longEmail = "very.long.email.address." + "a".repeat(100) + "@example.com";

        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName(longName)
                .lastName(longName)
                .email(longEmail)
                .build();

        // When
        Customer customer = entity.toDomain();

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getFirstName()).isEqualTo(longName);
        assertThat(customer.getLastName()).isEqualTo(longName);
        assertThat(customer.getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("Should maintain consistent hashCode across multiple calls")
    void shouldMaintainConsistentHashCodeAcrossMultipleCalls() {
        // Given
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When
        int hashCode1 = entity.hashCode();
        int hashCode2 = entity.hashCode();
        int hashCode3 = entity.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
        assertThat(hashCode2).isEqualTo(hashCode3);
    }

    @Test
    @DisplayName("Should have different hashCodes for entities with different IDs")
    void shouldHaveDifferentHashCodesForEntitiesWithDifferentIds() {
        // Given
        JpaCustomerEntity entity1 = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        JpaCustomerEntity entity2 = JpaCustomerEntity.builder()
                .id(2)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // When & Then
        assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }
} 