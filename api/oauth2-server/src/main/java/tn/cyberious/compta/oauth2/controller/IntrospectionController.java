package tn.cyberious.compta.oauth2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.oauth2.dto.IntrospectionResponse;
import tn.cyberious.compta.oauth2.service.TokenIntrospectionService;

@Tag(name = "Token Introspection", description = "OAuth2 token introspection endpoint (RFC 6819)")
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class IntrospectionController {

  private final TokenIntrospectionService tokenIntrospectionService;

  @Operation(
      summary = "Introspect token",
      description = "Introspect an OAuth2 token to get its metadata and validity status")
  @PostMapping("/introspect")
  public ResponseEntity<IntrospectionResponse> introspect(
      @RequestBody IntrospectionRequest request) {
    IntrospectionResponse response =
        tokenIntrospectionService.introspectToken(request.token(), request.tokenTypeHint());
    return ResponseEntity.ok(response);
  }

  public record IntrospectionRequest(
      @Parameter(description = "The token value to introspect", required = true)
          @NotBlank(message = "Token is required")
          String token,
      @Parameter(description = "Hint about the type of token (access_token or refresh_token)")
          String tokenTypeHint) {}
}
