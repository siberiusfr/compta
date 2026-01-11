package tn.cyberious.compta.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.cyberious.compta.oauth2.service.TokenRevocationService;

@Tag(name = "Token Revocation", description = "OAuth2 token revocation endpoint (RFC 7009)")
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class RevocationController {

  private final TokenRevocationService tokenRevocationService;

  @Operation(
      summary = "Revoke token",
      description = "Revoke an OAuth2 token. The token will be invalidated immediately.")
  @PostMapping("/revoke")
  public ResponseEntity<Void> revoke(@RequestBody RevocationRequest request) {
    tokenRevocationService.revokeToken(request.token(), request.tokenTypeHint());
    return ResponseEntity.ok().build();
  }

  public record RevocationRequest(
      @Parameter(description = "The token value to revoke", required = true)
          @NotBlank(message = "Token is required")
          String token,
      @Parameter(description = "Hint about the type of token (access_token or refresh_token)")
          String tokenTypeHint) {}
}
