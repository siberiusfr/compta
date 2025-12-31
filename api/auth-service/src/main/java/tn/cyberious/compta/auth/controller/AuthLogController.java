package tn.cyberious.compta.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.auth.dto.AuthLogResponse;
import tn.cyberious.compta.auth.service.AuthLogService;

@Slf4j
@RestController
@RequestMapping("/api/auth/logs")
@RequiredArgsConstructor
@Tag(name = "Auth Logs", description = "Authentication audit logs")
@SecurityRequirement(name = "bearer-jwt")
public class AuthLogController {

  private final AuthLogService authLogService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get all auth logs",
      description = "Get all authentication logs (ADMIN only)")
  public ResponseEntity<List<AuthLogResponse>> getAllLogs(
      @RequestParam(required = false, defaultValue = "100") Integer limit) {
    log.info("Request to get all auth logs with limit: {}", limit);
    List<AuthLogResponse> logs = authLogService.getAllLogs(limit);
    return ResponseEntity.ok(logs);
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Get user auth logs",
      description = "Get authentication logs for a specific user")
  public ResponseEntity<List<AuthLogResponse>> getUserLogs(
      @PathVariable Long userId,
      @RequestParam(required = false, defaultValue = "50") Integer limit) {
    log.info("Request to get auth logs for user {} with limit: {}", userId, limit);
    List<AuthLogResponse> logs = authLogService.getUserLogs(userId, limit);
    return ResponseEntity.ok(logs);
  }

  @GetMapping("/action/{action}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get logs by action",
      description = "Get authentication logs filtered by action type (ADMIN only)")
  public ResponseEntity<List<AuthLogResponse>> getLogsByAction(
      @PathVariable String action,
      @RequestParam(required = false, defaultValue = "100") Integer limit) {
    log.info("Request to get auth logs for action {} with limit: {}", action, limit);
    List<AuthLogResponse> logs = authLogService.getLogsByAction(action, limit);
    return ResponseEntity.ok(logs);
  }
}
