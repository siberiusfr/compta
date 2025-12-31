package tn.cyberious.compta.oauth2.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.oauth2.dto.ChangePasswordRequest;
import tn.cyberious.compta.oauth2.dto.CreateUserRequest;
import tn.cyberious.compta.oauth2.dto.UpdateUserRequest;
import tn.cyberious.compta.oauth2.dto.UserResponse;
import tn.cyberious.compta.oauth2.service.UserManagementService;

@Tag(name = "User Management", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

  private final UserManagementService userManagementService;

  @Operation(summary = "Create a new user", description = "Create a new user with roles")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserResponse response = userManagementService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get all users", description = "Retrieve all users")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    return ResponseEntity.ok(userManagementService.getAllUsers());
  }

  @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(
      @Parameter(description = "User ID") @PathVariable UUID id) {
    UserResponse response = userManagementService.getUserById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update user", description = "Update an existing user")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @Parameter(description = "User ID") @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request) {
    UserResponse response = userManagementService.updateUser(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete user", description = "Delete a user")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User ID") @PathVariable UUID id) {
    userManagementService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Disable user", description = "Disable a user account")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/disable")
  public ResponseEntity<Void> disableUser(
      @Parameter(description = "User ID") @PathVariable UUID id) {
    userManagementService.disableUser(id);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Enable user", description = "Enable a user account")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/enable")
  public ResponseEntity<Void> enableUser(
      @Parameter(description = "User ID") @PathVariable UUID id) {
    userManagementService.enableUser(id);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get user roles", description = "Retrieve all roles for a user")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}/roles")
  public ResponseEntity<List<String>> getUserRoles(
      @Parameter(description = "User ID") @PathVariable UUID id) {
    return ResponseEntity.ok(userManagementService.getUserRoles(id));
  }

  @Operation(summary = "Assign roles to user", description = "Assign roles to a user")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/roles")
  public ResponseEntity<Void> assignRoles(
      @Parameter(description = "User ID") @PathVariable UUID id,
      @RequestBody Map<String, List<String>> request) {
    userManagementService.assignRolesToUser(id, request.get("roles"));
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Remove role from user", description = "Remove a specific role from a user")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}/roles/{roleId}")
  public ResponseEntity<Void> removeRole(
      @Parameter(description = "User ID") @PathVariable UUID id,
      @Parameter(description = "Role ID") @PathVariable UUID roleId) {
    userManagementService.removeRoleFromUser(id, roleId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Change user password", description = "Change password for a user")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/password")
  public ResponseEntity<Void> changePassword(
      @Parameter(description = "User ID") @PathVariable UUID id,
      @Valid @RequestBody ChangePasswordRequest request) {
    userManagementService.changePassword(id, request);
    return ResponseEntity.ok().build();
  }
}
