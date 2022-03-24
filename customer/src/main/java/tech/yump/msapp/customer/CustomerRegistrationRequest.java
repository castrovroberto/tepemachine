package tech.yump.msapp.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}
