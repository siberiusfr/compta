package tn.cyberious.compta.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le message de demande de verification d'email envoye vers BullMQ.
 *
 * <p>Ce message est publie dans la queue 'email-verification' et consomme par notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationMessage {

  /** Identifiant unique de l'utilisateur. */
  @JsonProperty("userId")
  private String userId;

  /** Adresse email a verifier. */
  @JsonProperty("email")
  private String email;

  /** Nom d'utilisateur pour personnaliser l'email. */
  @JsonProperty("username")
  private String username;

  /** Token de verification unique. */
  @JsonProperty("token")
  private String token;

  /** Lien complet de verification a inclure dans l'email. */
  @JsonProperty("verificationLink")
  private String verificationLink;

  /** Date et heure d'expiration du token. */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @JsonProperty("expiresAt")
  private LocalDateTime expiresAt;
}
