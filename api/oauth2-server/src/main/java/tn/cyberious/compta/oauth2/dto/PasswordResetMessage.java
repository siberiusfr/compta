package tn.cyberious.compta.oauth2.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le message de demande de reset de mot de passe envoye vers BullMQ.
 *
 * <p>Ce message est publie dans la queue 'password-reset' et consomme par notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetMessage {

  /** Identifiant unique de l'utilisateur. */
  @JsonProperty("userId")
  private String userId;

  /** Adresse email de l'utilisateur. */
  @JsonProperty("email")
  private String email;

  /** Nom d'utilisateur pour personnaliser l'email. */
  @JsonProperty("username")
  private String username;

  /** Token de reset unique. */
  @JsonProperty("token")
  private String token;

  /** Lien complet de reset a inclure dans l'email. */
  @JsonProperty("resetLink")
  private String resetLink;

  /** Date et heure d'expiration du token. */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @JsonProperty("expiresAt")
  private LocalDateTime expiresAt;
}
