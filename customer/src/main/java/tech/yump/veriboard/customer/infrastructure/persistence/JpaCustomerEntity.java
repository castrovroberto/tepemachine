package tech.yump.veriboard.customer.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.yump.veriboard.customer.domain.Customer;

/**
 * JPA entity for customer persistence.
 * This is an infrastructure concern, separate from the domain entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Customer")
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(name = "customer_email_unique", columnNames = "email")
})
public class JpaCustomerEntity {

    @Id
    @SequenceGenerator(
            name = "customer_id_sequence",
            sequenceName = "customer_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "customer_id_sequence"
    )
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Converts this JPA entity to a domain Customer.
     * @return domain Customer instance
     */
    public Customer toDomain() {
        return new Customer(id, firstName, lastName, email);
    }

    /**
     * Creates a JPA entity from a domain Customer.
     * @param customer domain customer
     * @return JPA entity
     */
    public static JpaCustomerEntity fromDomain(Customer customer) {
        return JpaCustomerEntity.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .build();
    }
} 