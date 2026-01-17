package tn.cyberious.compta.oauth2.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.dto.CreateRoleRequest;
import tn.cyberious.compta.oauth2.dto.RoleResponse;
import tn.cyberious.compta.oauth2.dto.RoleWithUserCountDto;
import tn.cyberious.compta.oauth2.dto.UpdateRoleRequest;
import tn.cyberious.compta.oauth2.generated.tables.records.RolesRecord;
import tn.cyberious.compta.oauth2.repository.RoleRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleManagementService {

  private final RoleRepository roleRepository;

  @Transactional
  public RoleResponse createRole(CreateRoleRequest request) {
    log.info("Creating new role with name: {}", request.getName());

    if (roleRepository.existsByName(request.getName())) {
      throw new IllegalArgumentException("Role already exists with name: " + request.getName());
    }

    var roleRecord = roleRepository.insert(request.getName(), request.getDescription());

    log.info("Successfully created role with name: {}", request.getName());
    return toRoleResponse(roleRecord);
  }

  @Transactional(readOnly = true)
  public List<RoleWithUserCountDto> getAllRoles() {
    log.debug("Retrieving all roles with user count");
    var roles = roleRepository.findAll();

    return roles.stream()
        .map(
            role -> {
              int userCount = roleRepository.countUsersByRole(role.getId());

              return RoleWithUserCountDto.builder()
                  .id(role.getId().toString())
                  .name(role.getName())
                  .description(role.getDescription())
                  .createdAt(role.getCreatedAt())
                  .userCount(userCount)
                  .build();
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public RoleResponse getRoleById(UUID roleId) {
    log.debug("Retrieving role with id: {}", roleId);
    var roleRecord =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));

    return toRoleResponse(roleRecord);
  }

  @Transactional(readOnly = true)
  public RoleResponse getRoleByName(String name) {
    log.debug("Retrieving role with name: {}", name);
    var roleRecord =
        roleRepository
            .findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));

    return toRoleResponse(roleRecord);
  }

  @Transactional
  public RoleResponse updateRole(UUID roleId, UpdateRoleRequest request) {
    log.info("Updating role with id: {}", roleId);

    var existingRole =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));

    int updated = roleRepository.update(roleId, request.getDescription());

    if (updated == 0) {
      throw new IllegalArgumentException("Failed to update role with id: " + roleId);
    }

    var updatedRole =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found after update"));

    log.info("Successfully updated role with id: {}", roleId);
    return toRoleResponse(updatedRole);
  }

  @Transactional
  public void deleteRole(UUID roleId) {
    log.info("Deleting role with id: {}", roleId);

    var existingRole =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));

    int deletedUsers = roleRepository.deleteUsersByRole(roleId);
    log.info("Removed role from {} users", deletedUsers);

    int deleted = roleRepository.delete(roleId);

    if (deleted == 0) {
      throw new IllegalArgumentException("Failed to delete role with id: " + roleId);
    }

    log.info("Successfully deleted role with id: {}", roleId);
  }

  private RoleResponse toRoleResponse(RolesRecord roleRecord) {
    return RoleResponse.builder()
        .id(roleRecord.getId().toString())
        .name(roleRecord.getName())
        .description(roleRecord.getDescription())
        .createdAt(roleRecord.getCreatedAt())
        .build();
  }
}
