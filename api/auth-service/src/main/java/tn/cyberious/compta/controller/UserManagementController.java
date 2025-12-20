package tn.cyberious.compta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.dto.CreateEmployeeRequest;
import tn.cyberious.compta.dto.CreateSocieteRequest;
import tn.cyberious.compta.dto.CreateUserRequest;
import tn.cyberious.compta.security.CustomUserDetails;
import tn.cyberious.compta.service.SocieteService;
import tn.cyberious.compta.service.UserManagementService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User and organization management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class UserManagementController {

  private final UserManagementService userManagementService;
  private final SocieteService societeService;

  @PostMapping("/users/comptable")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create comptable", description = "Create a new comptable user (ADMIN only)")
  public ResponseEntity<Users> createComptable(
      @Valid @RequestBody CreateUserRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    log.info("Request to create comptable by user: {}", currentUser.getUsername());
    Users user = userManagementService.createComptable(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/users/societe")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(
      summary = "Create societe user",
      description = "Create a new societe user (ADMIN or COMPTABLE)")
  public ResponseEntity<Users> createSocieteUser(
      @Valid @RequestBody CreateUserRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    log.info("Request to create societe user by user: {}", currentUser.getUsername());
    Users user = userManagementService.createSocieteUser(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/users/employee")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(
      summary = "Create employee user",
      description = "Create a new employee user (ADMIN, COMPTABLE, or SOCIETE)")
  public ResponseEntity<Users> createEmployeeUser(
      @Valid @RequestBody CreateUserRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    log.info("Request to create employee user by user: {}", currentUser.getUsername());
    Users user = userManagementService.createEmployeeUser(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/societes")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
  @Operation(summary = "Create societe", description = "Create a new societe (ADMIN or COMPTABLE)")
  public ResponseEntity<Societes> createSociete(
      @Valid @RequestBody CreateSocieteRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    log.info("Request to create societe by user: {}", currentUser.getUsername());
    Societes societe = societeService.createSociete(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(societe);
  }

  @PostMapping("/employees")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'SOCIETE')")
  @Operation(
      summary = "Link employee to societe",
      description = "Create employee association (ADMIN, COMPTABLE, or SOCIETE)")
  public ResponseEntity<Void> createEmployee(
      @Valid @RequestBody CreateEmployeeRequest request,
      @AuthenticationPrincipal CustomUserDetails currentUser) {

    log.info("Request to create employee association by user: {}", currentUser.getUsername());
    userManagementService.createEmployee(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
