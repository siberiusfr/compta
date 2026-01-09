package tn.cyberious.compta.oauth2.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import tn.cyberious.compta.oauth2.dto.CreateRoleRequest;
import tn.cyberious.compta.oauth2.dto.RoleResponse;
import tn.cyberious.compta.oauth2.dto.UpdateRoleRequest;
import tn.cyberious.compta.oauth2.service.RoleManagementService;

@Tag(name = "Role Management", description = "Role management endpoints")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoleManagementController {

  private final RoleManagementService roleManagementService;

  @Operation(summary = "Create a new role", description = "Create a new role")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
    RoleResponse response = roleManagementService.createRole(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get all roles", description = "Retrieve all roles")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<RoleResponse>> getAllRoles() {
    return ResponseEntity.ok(roleManagementService.getAllRoles());
  }

  @Operation(summary = "Get role by ID", description = "Retrieve a specific role by ID")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<RoleResponse> getRoleById(
      @Parameter(description = "Role ID") @PathVariable UUID id) {
    RoleResponse response = roleManagementService.getRoleById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get role by name", description = "Retrieve a specific role by name")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/name/{name}")
  public ResponseEntity<RoleResponse> getRoleByName(
      @Parameter(description = "Role name") @PathVariable String name) {
    RoleResponse response = roleManagementService.getRoleByName(name);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update role", description = "Update an existing role")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<RoleResponse> updateRole(
      @Parameter(description = "Role ID") @PathVariable UUID id,
      @Valid @RequestBody UpdateRoleRequest request) {
    RoleResponse response = roleManagementService.updateRole(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete role", description = "Delete a role")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRole(
      @Parameter(description = "Role ID") @PathVariable UUID id) {
    roleManagementService.deleteRole(id);
    return ResponseEntity.noContent().build();
  }
}
