package tech.yump.veriboard.customer.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yump.veriboard.customer.domain.Customer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryAdapterTest {

    @Mock
    private JpaCustomerRepository jpaCustomerRepository;

    private CustomerRepositoryAdapter customerRepositoryAdapter;

    @BeforeEach
    void setUp() {
        customerRepositoryAdapter = new CustomerRepositoryAdapter(jpaCustomerRepository);
    }

    @Test
    void save_ShouldConvertToEntityAndSaveAndConvertBack() {
        // Given
        Customer customer = new Customer("John", "Doe", "john.doe@example.com");
        JpaCustomerEntity savedEntity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        when(jpaCustomerRepository.saveAndFlush(any(JpaCustomerEntity.class))).thenReturn(savedEntity);

        // When
        Customer result = customerRepositoryAdapter.save(customer);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        
        verify(jpaCustomerRepository).saveAndFlush(argThat(entity -> 
            entity.getFirstName().equals("John") &&
            entity.getLastName().equals("Doe") &&
            entity.getEmail().equals("john.doe@example.com")
        ));
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnCustomer() {
        // Given
        String email = "john.doe@example.com";
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .build();

        when(jpaCustomerRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        // When
        Optional<Customer> result = customerRepositoryAdapter.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        Customer customer = result.get();
        assertEquals(1, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals(email, customer.getEmail());
        
        verify(jpaCustomerRepository).findByEmail(email);
    }

    @Test
    void findByEmail_WhenEmailDoesNotExist_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(jpaCustomerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = customerRepositoryAdapter.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(jpaCustomerRepository).findByEmail(email);
    }

    @Test
    void findById_WhenIdExists_ShouldReturnCustomer() {
        // Given
        Integer id = 1;
        JpaCustomerEntity entity = JpaCustomerEntity.builder()
                .id(id)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        when(jpaCustomerRepository.findById(id)).thenReturn(Optional.of(entity));

        // When
        Optional<Customer> result = customerRepositoryAdapter.findById(id);

        // Then
        assertTrue(result.isPresent());
        Customer customer = result.get();
        assertEquals(id, customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("jane.smith@example.com", customer.getEmail());
        
        verify(jpaCustomerRepository).findById(id);
    }

    @Test
    void findById_WhenIdDoesNotExist_ShouldReturnEmpty() {
        // Given
        Integer id = 999;
        when(jpaCustomerRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = customerRepositoryAdapter.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(jpaCustomerRepository).findById(id);
    }

    @Test
    void findById_WithNullId_ShouldReturnEmpty() {
        // Given
        Integer id = null;
        when(jpaCustomerRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = customerRepositoryAdapter.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(jpaCustomerRepository).findById(id);
    }
} 