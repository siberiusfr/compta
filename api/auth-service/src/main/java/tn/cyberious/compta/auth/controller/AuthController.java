package tn.cyberious.compta.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.auth.dto.AuthResponse;
import tn.cyberious.compta.auth.dto.ChangePasswordRequest;
import tn.cyberious.compta.auth.dto.LoginRequest;
import tn.cyberious.compta.auth.dto.UpdateUserRequest;
import tn.cyberious.compta.auth.dto.UserResponse;
import tn.cyberious.compta.auth.security.CustomUserDetails;
import tn.cyberious.compta.auth.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and return JWT tokens")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
    String ipAddress = getClientIp(request);
    String userAgent = request.getHeader("User-Agent");
    AuthResponse response = authService.login(loginRequest, ipAddress, userAgent);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
  public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
    AuthResponse response = authService.refreshToken(refreshToken);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @SecurityRequirement(name = "bearer-jwt")
  @Operation(summary = "Logout", description = "Logout user and invalidate refresh token")
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestParam(required = false) String refreshToken) {
    authService.logout(currentUser.getId(), refreshToken);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  @SecurityRequirement(name = "bearer-jwt")
  @Operation(security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<UserResponse> getCurrentUser(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    UserResponse user = authService.getCurrentUser(currentUser.getId());
    return ResponseEntity.ok(user);
  }

  @PutMapping("/me")
  @SecurityRequirement(name = "bearer-jwt")
  @Operation(summary = "Update current user", description = "Update current user profile")
  public ResponseEntity<UserResponse> updateCurrentUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @Valid @RequestBody UpdateUserRequest request) {
    UserResponse user =
        authService.updateCurrentUser(
            currentUser.getId(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPhone());
    return ResponseEntity.ok(user);
  }

  @PutMapping("/password")
  @SecurityRequirement(name = "bearer-jwt")
  @Operation(summary = "Change password", description = "Change current user password")
  public ResponseEntity<Void> changePassword(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @Valid @RequestBody ChangePasswordRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new RuntimeException("Passwords do not match");
    }

    authService.changePassword(
        currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.noContent().build();
  }

  private String getClientIp(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
  }
}
