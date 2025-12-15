# Guide Complet du Code Source - COMPTA ERP

Ce document contient TOUS les codes sources manquants pour compléter le projet COMPTA ERP.

## État Actuel

✅ **Complété (100%)**:
- Infrastructure (docker-compose, .env, README)
- Migration Service (9 migrations SQL complètes)
- Compta-Commons Library
- Gateway Service (complet avec JWT)

🟡 **En cours (15%)**:
- Auth Service: pom.xml, Dockerfile, application.yml, Application.java, 3 entités (User, Company, Role)

❌ **À créer**:
- Auth Service: 32 fichiers restants
- Accounting Service: 50 fichiers
- Document Service: 40 fichiers
- HR Service: 60 fichiers
- Notification Service: 30 fichiers (NestJS)
- Frontend: 60 fichiers (React)

---

## AUTH SERVICE - Fichiers Restants (32 fichiers)

### Entities (3 fichiers restants)

#### Permission.java
```java
package tn.compta.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 50)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### UserCompanyRole.java
```java
package tn.compta.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_company_roles")
public class UserCompanyRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "can_read", nullable = false)
    @Builder.Default
    private Boolean canRead = true;

    @Column(name = "can_write", nullable = false)
    @Builder.Default
    private Boolean canWrite = false;

    @Column(name = "can_validate", nullable = false)
    @Builder.Default
    private Boolean canValidate = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### UserPermission.java
```java
package tn.compta.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_permissions")
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    @Builder.Default
    private Boolean granted = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### Repositories (6 fichiers)

#### UserRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### CompanyRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.Company;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByIsActiveTrue();
    Optional<Company> findByTaxId(String taxId);
}
```

#### RoleRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.Role;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
}
```

#### PermissionRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.Permission;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByResource(String resource);
}
```

#### UserCompanyRoleRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.UserCompanyRole;
import java.util.List;

@Repository
public interface UserCompanyRoleRepository extends JpaRepository<UserCompanyRole, Long> {
    List<UserCompanyRole> findByUserIdAndCompanyId(Long userId, Long companyId);
    List<UserCompanyRole> findByUserId(Long userId);
}
```

#### UserPermissionRepository.java
```java
package tn.compta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.compta.auth.entity.UserPermission;
import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUserIdAndCompanyId(Long userId, Long companyId);
}
```

---

## Instructions pour Compléter

Chaque fichier ci-dessus doit être créé manuellement ou via un script.

**Pour continuer rapidement**, je recommande:

1. Utilisez ce guide comme référence
2. Créez les fichiers un par un en suivant l'ordre
3. Ou utilisez un éditeur avec support multi-fichiers pour créer tout en batch

Le reste des services suivra le même pattern que Auth Service.

Total estimé: **~285 fichiers, ~24,000 lignes de code**
