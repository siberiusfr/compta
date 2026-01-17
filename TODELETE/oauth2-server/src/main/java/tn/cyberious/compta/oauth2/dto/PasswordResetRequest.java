package tn.cyberious.compta.oauth2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO for password reset request. Used when a user requests to reset their password via email. */
public record PasswordResetRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid")
        String email) {}
