package tn.cyberious.compta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.auth.generated.tables.pojos.Employees;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.dto.ComptableSocieteRequest;
import tn.cyberious.compta.dto.UpdateSocieteRequest;
import tn.cyberious.compta.dto.UserResponse;
import tn.cyberious.compta.dto.UserSocieteRequest;
import tn.cyberious.compta.security.CustomUserDetails;
import tn.cyberious.compta.service.SocieteService;

@Slf4j
@RestController
@RequestMapping("/api/societes")
@RequiredArgsConstructor
@Tag(name = "Societe Management", description = "Company management operations")
@SecurityRequirement(name = "bearer-jwt")
public class SocieteController {

  private final SocieteService societeService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Get all societes",
      description = "Get list of all companies (ADMIN or COMPTABLE)")
  public ResponseEntity<List<Societes>> getAllSocietes(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to get all societes by {}", currentUser.getUsername());
    List<Societes> societes = societeService.getAllSocietes(currentUser);
    return ResponseEntity.ok(societes);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(summary = "Get societe by ID", description = "Get company details by ID")
  public ResponseEntity<Societes> getSocieteById(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to get societe: {} by {}", id, currentUser.getUsername());
    Societes societe = societeService.getSocieteById(id, currentUser);
    return ResponseEntity.ok(societe);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Update societe",
      description = "Update company information (ADMIN or COMPTABLE)")
  public ResponseEntity<Societes> updateSociete(
      @PathVariable Long id,
      @Valid @RequestBody UpdateSocieteRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to update societe {} by {}", id, currentUser.getUsername());
    Societes societe = societeService.updateSociete(id, request, currentUser);
    return ResponseEntity.ok(societe);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Delete societe",
      description = "Delete company (ADMIN or COMPTABLE if assigned)")
  public ResponseEntity<Void> deleteSociete(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to delete societe {} by {}", id, currentUser.getUsername());
    societeService.deleteSociete(id, currentUser);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/users")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(summary = "Get societe users", description = "Get all users associated with a company")
  public ResponseEntity<List<UserResponse>> getSocieteUsers(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to get users for societe: {} by {}", id, currentUser.getUsername());
    List<UserResponse> users = societeService.getSocieteUsers(id, currentUser);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}/employees")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(summary = "Get societe employees", description = "Get all employees of a company")
  public ResponseEntity<List<Employees>> getSocieteEmployees(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to get employees for societe: {} by {}", id, currentUser.getUsername());
    List<Employees> employees = societeService.getSocieteEmployees(id, currentUser);
    return ResponseEntity.ok(employees);
  }

  @PostMapping("/comptable-assignment")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Assign comptable to societe",
      description = "Create comptable-societe association (ADMIN only)")
  public ResponseEntity<Void> assignComptable(
      @Valid @RequestBody ComptableSocieteRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info(
        "Request to assign comptable {} to societe {} by {}",
        request.getUserId(),
        request.getSocieteId(),
        currentUser.getUsername());
    societeService.assignComptableToSociete(request, currentUser);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/comptable-assignment/{userId}/{societeId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Remove comptable from societe",
      description = "Remove comptable-societe association (ADMIN only)")
  public ResponseEntity<Void> removeComptable(
      @PathVariable Long userId,
      @PathVariable Long societeId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info(
        "Request to remove comptable {} from societe {} by {}",
        userId,
        societeId,
        currentUser.getUsername());
    societeService.removeComptableFromSociete(userId, societeId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/user-assignment")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Assign user to societe",
      description = "Create user-societe association (ADMIN or COMPTABLE)")
  public ResponseEntity<Void> assignUserToSociete(
      @Valid @RequestBody UserSocieteRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info(
        "Request to assign user {} to societe {} by {}",
        request.getUserId(),
        request.getSocieteId(),
        currentUser.getUsername());
    societeService.assignUserToSociete(request, currentUser);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/user-assignment/{userId}/{societeId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(summary = "Remove user from societe", description = "Remove user-societe association")
  public ResponseEntity<Void> removeUserFromSociete(
      @PathVariable Long userId,
      @PathVariable Long societeId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info(
        "Request to remove user {} from societe {} by {}",
        userId,
        societeId,
        currentUser.getUsername());
    societeService.removeUserFromSociete(userId, societeId, currentUser);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(summary = "Get user societes", description = "Get all companies for a user")
  public ResponseEntity<List<Societes>> getUserSocietes(
      @PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails currentUser) {
    log.info("Request to get societes for user: {} by {}", userId, currentUser.getUsername());
    List<Societes> societes = societeService.getUserSocietes(userId, currentUser);
    return ResponseEntity.ok(societes);
  }
}
