package tech.yump.veriboard.customer.infrastructure.persistence;

import org.springframework.stereotype.Component;
import tech.yump.veriboard.customer.domain.Customer;
import tech.yump.veriboard.customer.domain.ports.CustomerRepository;

import java.util.Optional;

/**
 * Adapter that implements the domain CustomerRepository port using Spring Data JPA.
 * This bridges the domain layer with the infrastructure persistence layer.
 */
@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository jpaRepository;

    public CustomerRepositoryAdapter(JpaCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        JpaCustomerEntity entity = JpaCustomerEntity.fromDomain(customer);
        JpaCustomerEntity savedEntity = jpaRepository.saveAndFlush(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(JpaCustomerEntity::toDomain);
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(JpaCustomerEntity::toDomain);
    }
} 