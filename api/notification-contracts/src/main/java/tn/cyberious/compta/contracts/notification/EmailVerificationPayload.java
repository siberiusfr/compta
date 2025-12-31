package tn.cyberious.compta.contracts.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload du message de demande de verification d'email.
 *
 * <p>Utilise pour la communication asynchrone entre oauth2-server et notification-service via
 * Redis/BullMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationPayload {

  /** Identifiant unique de l'utilisateur. */
  @NotBlank(message = "userId is required")
  @JsonProperty("userId")
  private String userId;

  /** Adresse email a verifier. */
  @NotBlank(message = "email is required")
  @Email(message = "email must be a valid email address")
  @JsonProperty("email")
  private String email;

  /** Nom d'utilisateur pour personnaliser l'email. */
  @NotBlank(message = "username is required")
  @Size(min = 1, max = 255, message = "username must be between 1 and 255 characters")
  @JsonProperty("username")
  private String username;

  /** Token de verification unique. */
  @NotBlank(message = "token is required")
  @Size(min = 32, max = 64, message = "token must be between 32 and 64 characters")
  @JsonProperty("token")
  private String token;

  /** Lien complet de verification a inclure dans l'email. */
  @NotBlank(message = "verificationLink is required")
  @JsonProperty("verificationLink")
  private String verificationLink;

  /** Date et heure d'expiration du token. */
  @NotNull(message = "expiresAt is required")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @JsonProperty("expiresAt")
  private LocalDateTime expiresAt;
}
