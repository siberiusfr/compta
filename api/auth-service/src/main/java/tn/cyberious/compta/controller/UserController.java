package tn.cyberious.compta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.dto.AssignRoleRequest;
import tn.cyberious.compta.dto.UpdateUserRequest;
import tn.cyberious.compta.dto.UserResponse;
import tn.cyberious.compta.security.CustomUserDetails;
import tn.cyberious.compta.service.UserManagementService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @Operation(summary = "Get all users", description = "Get list of all users (ADMIN or COMPTABLE)")
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to get all users by {}", currentUser.getUsername());
        List<UserResponse> users = userManagementService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (ADMIN or COMPTABLE)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to get user: {} by {}", id, currentUser.getUsername());
        UserResponse user = userManagementService.getUserById(id, currentUser);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update user information (ADMIN only)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateUserRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to update user {} by {}", id, currentUser.getUsername());
        UserResponse user = userManagementService.updateUser(id, request, currentUser);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user (ADMIN only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to delete user {} by {}", id, currentUser.getUsername());
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Activate user account (ADMIN only)")
    public ResponseEntity<Void> activateUser(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to activate user {} by {}", id, currentUser.getUsername());
        userManagementService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate user account (ADMIN only)")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id,
                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to deactivate user {} by {}", id, currentUser.getUsername());
        userManagementService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user", description = "Unlock user account (ADMIN only)")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to unlock user {} by {}", id, currentUser.getUsername());
        userManagementService.unlockUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @Operation(summary = "Get user roles", description = "Get all roles for a user (ADMIN or COMPTABLE)")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable Long id) {
        log.info("Request to get roles for user: {}", id);
        List<String> roles = userManagementService.getUserRoles(id);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user", description = "Assign a role to user (ADMIN only)")
    public ResponseEntity<Void> assignRole(@PathVariable Long id,
                                            @Valid @RequestBody AssignRoleRequest request,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to assign role {} to user {} by {}", request.getRoleName(), id, currentUser.getUsername());
        userManagementService.assignRole(id, request.getRoleName(), currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Remove a role from user (ADMIN only)")
    public ResponseEntity<Void> removeRole(@PathVariable Long id,
                                            @PathVariable Long roleId,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request to remove role {} from user {} by {}", roleId, id, currentUser.getUsername());
        userManagementService.removeRole(id, roleId);
        return ResponseEntity.noContent().build();
    }
}
