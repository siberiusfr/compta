package tn.cyberious.compta.authz.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.authz.dto.PermissionDto;
import tn.cyberious.compta.authz.dto.RolePermissionDto;
import tn.cyberious.compta.authz.dto.request.AssignPermissionToRoleRequest;
import tn.cyberious.compta.authz.dto.request.CreatePermissionRequest;
import tn.cyberious.compta.authz.generated.tables.pojos.Permissions;
import tn.cyberious.compta.authz.generated.tables.pojos.RolePermissions;
import tn.cyberious.compta.authz.repository.PermissionRepository;
import tn.cyberious.compta.authz.repository.RolePermissionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionRepository permissionRepository;
  private final RolePermissionRepository rolePermissionRepository;

  // ===== Permission CRUD =====

  @Transactional
  public PermissionDto createPermission(CreatePermissionRequest request) {
    if (permissionRepository.existsByCode(request.getCode())) {
      throw new IllegalArgumentException(
          "Une permission avec ce code existe deja: " + request.getCode());
    }

    Permissions permission = new Permissions();
    permission.setCode(request.getCode());
    permission.setResource(request.getResource());
    permission.setAction(request.getAction());
    permission.setDescription(request.getDescription());

    Permissions created = permissionRepository.insert(permission);
    log.info("Permission creee: {} (ID: {})", created.getCode(), created.getId());
    return toDto(created);
  }

  @Transactional
  public PermissionDto updateDescription(Long id, String description) {
    Permissions existing =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permission non trouvee: " + id));

    existing.setDescription(description);
    Permissions updated = permissionRepository.update(existing);
    log.info("Permission mise a jour: {}", updated.getCode());
    return toDto(updated);
  }

  @Transactional
  public void deletePermission(Long id) {
    if (!permissionRepository.existsById(id)) {
      throw new IllegalArgumentException("Permission non trouvee: " + id);
    }
    // Supprimer d'abord les associations role_permissions
    rolePermissionRepository.deleteByPermissionId(id);
    permissionRepository.delete(id);
    log.info("Permission supprimee: ID {}", id);
  }

  @Transactional(readOnly = true)
  public PermissionDto findById(Long id) {
    return permissionRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Permission non trouvee: " + id));
  }

  @Transactional(readOnly = true)
  public PermissionDto findByCode(String code) {
    return permissionRepository
        .findByCode(code)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Permission non trouvee: " + code));
  }

  @Transactional(readOnly = true)
  public List<PermissionDto> findAll() {
    return permissionRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionDto> findByResource(String resource) {
    return permissionRepository.findByResource(resource).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionDto> findByAction(String action) {
    return permissionRepository.findByAction(action).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<String> findDistinctResources() {
    return permissionRepository.findDistinctResources();
  }

  @Transactional(readOnly = true)
  public List<String> findDistinctActions() {
    return permissionRepository.findDistinctActions();
  }

  // ===== Role-Permission Management =====

  @Transactional
  public RolePermissionDto assignPermissionToRole(AssignPermissionToRoleRequest request) {
    if (!permissionRepository.existsById(request.getPermissionId())) {
      throw new IllegalArgumentException("Permission non trouvee: " + request.getPermissionId());
    }

    if (rolePermissionRepository.existsByRoleAndPermissionId(
        request.getRole(), request.getPermissionId())) {
      throw new IllegalArgumentException("Cette permission est deja assignee a ce rôle");
    }

    RolePermissions rolePermission = new RolePermissions();
    rolePermission.setRole(request.getRole());
    rolePermission.setPermissionId(request.getPermissionId());

    RolePermissions created = rolePermissionRepository.insert(rolePermission);
    log.info("Permission {} assignee au rôle {}", request.getPermissionId(), request.getRole());
    return toRolePermissionDto(created);
  }

  @Transactional
  public void revokePermissionFromRole(String role, Long permissionId) {
    if (!rolePermissionRepository.existsByRoleAndPermissionId(role, permissionId)) {
      throw new IllegalArgumentException("Cette permission n'est pas assignee a ce rôle");
    }
    rolePermissionRepository.deleteByRoleAndPermissionId(role, permissionId);
    log.info("Permission {} revoquee du rôle {}", permissionId, role);
  }

  @Transactional(readOnly = true)
  public List<PermissionDto> findPermissionsByRole(String role) {
    return permissionRepository.findByRole(role).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<RolePermissionDto> findRolePermissionsByRole(String role) {
    return rolePermissionRepository.findByRole(role).stream()
        .map(this::toRolePermissionDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<String> findDistinctRoles() {
    return rolePermissionRepository.findDistinctRoles();
  }

  @Transactional(readOnly = true)
  public boolean hasPermission(String role, String permissionCode) {
    return rolePermissionRepository.hasPermission(role, permissionCode);
  }

  @Transactional(readOnly = true)
  public boolean hasPermissionOnResource(String role, String resource, String action) {
    return rolePermissionRepository.hasPermissionOnResource(role, resource, action);
  }

  private PermissionDto toDto(Permissions entity) {
    return PermissionDto.builder()
        .id(entity.getId())
        .code(entity.getCode())
        .resource(entity.getResource())
        .action(entity.getAction())
        .description(entity.getDescription())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  private RolePermissionDto toRolePermissionDto(RolePermissions entity) {
    return RolePermissionDto.builder()
        .id(entity.getId())
        .role(entity.getRole())
        .permissionId(entity.getPermissionId())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
