package tech.yump.veriboard.clients.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Integer toCustomerId,
        @NotBlank @Email String toCustomerEmail,
        @NotBlank String message
) {
}
