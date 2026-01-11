package tn.cyberious.compta.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.cyberious.compta.oauth2.dto.PasswordResetConfirmRequest;
import tn.cyberious.compta.oauth2.dto.PasswordResetRequest;
import tn.cyberious.compta.oauth2.service.PasswordResetService;

/**
 * REST controller for password reset operations.
 *
 * <p>Provides endpoints for initiating and confirming password resets via email.
 */
@RestController
@RequestMapping("/api/users/password")
public class PasswordResetController {

  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService) {
    this.passwordResetService = passwordResetService;
  }

  /**
   * Initiate password reset for a user.
   *
   * @param request The password reset request containing email
   * @param httpRequest The HTTP request
   * @return Response indicating if reset was initiated
   */
  @PostMapping("/reset")
  @Operation(
      summary = "Initiate password reset",
      description = "Sends a password reset email to the user's email address")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent (even if email doesn't exist for security)",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email format",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests (rate limit exceeded)",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public ResponseEntity<String> initiatePasswordReset(
      @Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {

    String ipAddress = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    boolean initiated =
        passwordResetService.initiatePasswordReset(request.email(), ipAddress, userAgent);

    if (initiated) {
      return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
    } else {
      return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
      // Note: We don't reveal whether the email exists or not for security
    }
  }

  /**
   * Confirm password reset with token and new password.
   *
   * @param request The password reset confirmation request
   * @param httpRequest The HTTP request
   * @return Response indicating if password was reset
   */
  @PostMapping("/reset/confirm")
  @Operation(
      summary = "Confirm password reset",
      description = "Resets the user's password using a valid reset token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successfully",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid token or password",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Token not found or expired",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public ResponseEntity<String> confirmPasswordReset(
      @Valid @RequestBody PasswordResetConfirmRequest request, HttpServletRequest httpRequest) {

    String ipAddress = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    boolean success = passwordResetService.confirmPasswordReset(request, ipAddress, userAgent);

    if (success) {
      return ResponseEntity.ok("Password has been reset successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset token.");
    }
  }

  /** Get client IP address from request. Handles proxies and load balancers. */
  private String getClientIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // Handle multiple IPs in X-Forwarded-For
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    return ip;
  }
}
