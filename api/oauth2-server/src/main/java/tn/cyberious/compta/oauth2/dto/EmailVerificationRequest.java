package tn.cyberious.compta.oauth2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO for email verification request. Used when a user requests to verify their email address. */
public record EmailVerificationRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid")
        String email) {}
