package tn.cyberious.compta.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for password reset confirmation. Used when a user confirms their password reset with a token
 * and new password.
 */
public record PasswordResetConfirmRequest(
    @NotBlank(message = "Reset token is required")
        @Size(min = 36, max = 36, message = "Reset token must be 36 characters")
        String token,
    @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password) {}
