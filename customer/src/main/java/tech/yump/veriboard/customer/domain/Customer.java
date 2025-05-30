package tech.yump.veriboard.customer.domain;

/**
 * Domain entity representing a Customer in the VeriBoard platform.
 * This is a pure domain object with no external dependencies.
 */
public class Customer {
    
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    
    // Constructor for creating new customers (no ID)
    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Constructor for existing customers (with ID)
    public Customer(Integer id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Domain behavior: Get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Domain behavior: Check if customer is new (no ID)
    public boolean isNew() {
        return id == null;
    }
    
    // Getters
    public Integer getId() {
        return id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    // For testing and debugging
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Customer customer = (Customer) o;
        
        return id != null ? id.equals(customer.id) : customer.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 