package tech.yump.veriboard.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for customer persistence.
 * Works with JPA entities, not domain objects directly.
 */
@Repository
public interface JpaCustomerRepository extends JpaRepository<JpaCustomerEntity, Integer> {
    
    /**
     * Finds a customer entity by email address.
     * @param email the email to search for
     * @return Optional containing the JPA entity if found
     */
    Optional<JpaCustomerEntity> findByEmail(String email);
} 