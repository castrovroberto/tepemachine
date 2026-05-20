package tech.yump.veriboard.customer.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email) {

    public Customer toCustomer() {
        return new Customer(firstName, lastName, email);
    }
}