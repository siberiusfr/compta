package tn.cyberious.compta.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import tn.cyberious.compta.oauth2.dto.EmailVerificationRequest;
import tn.cyberious.compta.oauth2.service.EmailVerificationService;

/**
 * REST controller for email verification operations.
 *
 * <p>Provides endpoints for verifying email addresses during user registration.
 */
@RestController
@RequestMapping("/api/users/email")
public class EmailVerificationController {

  private final EmailVerificationService emailVerificationService;

  public EmailVerificationController(EmailVerificationService emailVerificationService) {
    this.emailVerificationService = emailVerificationService;
  }

  /**
   * Initiate email verification for a user.
   *
   * @param request The email verification request
   * @param httpRequest The HTTP request
   * @return Response indicating if verification was initiated
   */
  @PostMapping("/verify")
  @Operation(
      summary = "Initiate email verification",
      description = "Sends a verification email to the user's email address")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Verification email sent (even if email doesn't exist for security)",
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
  public ResponseEntity<String> initiateEmailVerification(
      @Valid @RequestBody EmailVerificationRequest request, HttpServletRequest httpRequest) {

    String ipAddress = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    boolean initiated =
        emailVerificationService.initiateEmailVerification(request.email(), ipAddress, userAgent);

    if (initiated) {
      return ResponseEntity.ok("If the email exists, a verification link has been sent.");
    } else {
      return ResponseEntity.ok("If the email exists, a verification link has been sent.");
      // Note: We don't reveal whether the email exists or not for security
    }
  }

  /**
   * Confirm email verification with token.
   *
   * @param token The verification token
   * @param httpRequest The HTTP request
   * @return Response indicating if email was verified
   */
  @PostMapping("/verify/confirm")
  @Operation(
      summary = "Confirm email verification",
      description = "Verifies the user's email address using a valid verification token")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Email verified successfully",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid or expired token",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Token not found",
        content = @Content(schema = @Schema(implementation = String.class)))
  })
  public ResponseEntity<String> confirmEmailVerification(
      @Parameter(description = "The verification token from the email") String token,
      HttpServletRequest httpRequest) {

    String ipAddress = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    boolean verified = emailVerificationService.verifyEmail(token, ipAddress, userAgent);

    if (verified) {
      return ResponseEntity.ok("Email has been verified successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Invalid or expired verification token.");
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
