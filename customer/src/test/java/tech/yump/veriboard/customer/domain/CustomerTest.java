package tech.yump.veriboard.customer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void constructor_WithThreeParameters_ShouldCreateCustomerWithNullId() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";

        // When
        Customer customer = new Customer(firstName, lastName, email);

        // Then
        assertNull(customer.getId());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(email, customer.getEmail());
    }

    @Test
    void constructor_WithFourParameters_ShouldCreateCustomerWithId() {
        // Given
        Integer id = 1;
        String firstName = "Jane";
        String lastName = "Smith";
        String email = "jane.smith@example.com";

        // When
        Customer customer = new Customer(id, firstName, lastName, email);

        // Then
        assertEquals(id, customer.getId());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(email, customer.getEmail());
    }

    @Test
    void getFullName_ShouldReturnConcatenatedFirstAndLastName() {
        // Given
        Customer customer = new Customer("John", "Doe", "john.doe@example.com");

        // When
        String fullName = customer.getFullName();

        // Then
        assertEquals("John Doe", fullName);
    }

    @Test
    void getFullName_WithNullFirstName_ShouldHandleGracefully() {
        // Given
        Customer customer = new Customer(null, "Doe", "john.doe@example.com");

        // When
        String fullName = customer.getFullName();

        // Then
        assertEquals("null Doe", fullName);
    }

    @Test
    void getFullName_WithNullLastName_ShouldHandleGracefully() {
        // Given
        Customer customer = new Customer("John", null, "john.doe@example.com");

        // When
        String fullName = customer.getFullName();

        // Then
        assertEquals("John null", fullName);
    }

    @Test
    void isNew_WithNullId_ShouldReturnTrue() {
        // Given
        Customer customer = new Customer("John", "Doe", "john.doe@example.com");

        // When
        boolean isNew = customer.isNew();

        // Then
        assertTrue(isNew);
    }

    @Test
    void isNew_WithNonNullId_ShouldReturnFalse() {
        // Given
        Customer customer = new Customer(1, "John", "Doe", "john.doe@example.com");

        // When
        boolean isNew = customer.isNew();

        // Then
        assertFalse(isNew);
    }

    @Test
    void toString_ShouldContainAllFieldValues() {
        // Given
        Customer customer = new Customer(1, "John", "Doe", "john.doe@example.com");

        // When
        String toString = customer.toString();

        // Then
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("john.doe@example.com"));
        assertTrue(toString.contains("Customer"));
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        Customer customer1 = new Customer(1, "John", "Doe", "john.doe@example.com");
        Customer customer2 = new Customer(1, "John", "Doe", "john.doe@example.com");

        // When & Then
        assertEquals(customer1, customer2);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        Customer customer1 = new Customer(1, "John", "Doe", "john.doe@example.com");
        Customer customer2 = new Customer(2, "Jane", "Smith", "jane.smith@example.com");

        // When & Then
        assertNotEquals(customer1, customer2);
    }

    @Test
    void equals_ShouldReturnFalseWhenComparedToNull() {
        // Given
        Customer customer = new Customer(1, "John", "Doe", "john.doe@example.com");

        // When & Then
        assertNotEquals(customer, null);
    }

    @Test
    void equals_ShouldReturnFalseWhenComparedToDifferentClass() {
        // Given
        Customer customer = new Customer(1, "John", "Doe", "john.doe@example.com");
        String notACustomer = "Not a customer";

        // When & Then
        assertNotEquals(customer, notACustomer);
    }

    @Test
    void hashCode_ShouldBeConsistentForSameValues() {
        // Given
        Customer customer1 = new Customer(1, "John", "Doe", "john.doe@example.com");
        Customer customer2 = new Customer(1, "John", "Doe", "john.doe@example.com");

        // When & Then
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }

    @Test
    void hashCode_ShouldBeDifferentForDifferentValues() {
        // Given
        Customer customer1 = new Customer(1, "John", "Doe", "john.doe@example.com");
        Customer customer2 = new Customer(2, "Jane", "Smith", "jane.smith@example.com");

        // When & Then
        assertNotEquals(customer1.hashCode(), customer2.hashCode());
    }
} 