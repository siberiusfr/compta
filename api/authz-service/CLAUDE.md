# CLAUDE.md - authz-service

Ce fichier fournit des instructions à Claude Code pour travailler avec le code de ce service.

## Apercu du Service

Le **authz-service** est le microservice de gestion des autorisations de l'application COMPTA. Il gère :
- Les sociétés comptables (cabinets)
- Les sociétés clientes
- Les affectations d'utilisateurs aux cabinets et sociétés
- Les accès des comptables aux sociétés clientes
- Les permissions granulaires par rôle

**Stack Technique:**
- Spring Boot 3.5.8 (Java 21)
- PostgreSQL avec schéma `authz`
- jOOQ pour les requêtes SQL type-safe
- Flyway pour les migrations
- Swagger/OpenAPI pour la documentation

**Port:** 8082
**Context Path:** `/authz`

## Commandes

### Build et Exécution
```bash
# Build le service (depuis le répertoire authz-service)
mvn clean install

# Exécuter le service
mvn spring-boot:run

# Build tout le projet (depuis le parent api)
cd .. && mvn clean install
```

### Base de données
```bash
# Régénérer les classes jOOQ après modification du schéma
mvn clean generate-sources
```

### Tests
```bash
# Exécuter tous les tests
mvn test

# Exécuter un test spécifique
mvn test -Dtest=AuthzServiceApplicationTests
```

## Architecture

### Schéma de base de données: `authz`

Le service possède 7 tables principales:

| Table | Description |
|-------|-------------|
| `societes_comptables` | Cabinets comptables |
| `societes` | Sociétés clientes |
| `user_societe_comptable` | Utilisateurs des cabinets (MANAGER, COMPTABLE, ASSISTANT) |
| `comptable_societes` | Accès des comptables aux sociétés clientes (lecture/écriture/validation) |
| `user_societes` | Utilisateurs des sociétés clientes (MANAGER, FINANCE, VIEWER) |
| `permissions` | Permissions granulaires (resource + action) |
| `role_permissions` | Association rôle → permissions |

### Hiérarchie des entités

```
Cabinet Comptable (societes_comptables)
├── Utilisateurs du cabinet (user_societe_comptable)
│   ├── MANAGER (unique par cabinet)
│   ├── COMPTABLE
│   └── ASSISTANT
└── Sociétés clientes gérées (societes)
    ├── Accès comptables (comptable_societes)
    │   └── can_read, can_write, can_validate
    └── Utilisateurs de la société (user_societes)
        ├── MANAGER (unique par société)
        ├── FINANCE
        └── VIEWER
```

### Contraintes métier importantes

1. **Un seul MANAGER actif par cabinet/société** - Contrainte GIST dans PostgreSQL
2. **Un utilisateur appartient à UN SEUL cabinet** - Colonne `user_id` UNIQUE dans `user_societe_comptable`
3. **Un utilisateur appartient à UNE SEULE société cliente** - Colonne `user_id` UNIQUE dans `user_societes`
4. **Accès avec plage de dates** - `date_debut` et `date_fin` pour gérer les accès temporaires

## Structure du Code

```
src/main/java/tn/cyberious/compta/authz/
├── AuthzServiceApplication.java      # Point d'entrée
├── config/
│   └── CacheConfig.java              # Configuration Caffeine
├── controller/                       # Contrôleurs REST
│   ├── AccessController.java         # Accès unifié (avec cache)
│   ├── SocieteComptableController.java
│   ├── SocieteController.java
│   ├── UserSocieteComptableController.java
│   ├── ComptableSocietesController.java
│   ├── UserSocietesController.java
│   └── PermissionController.java
├── service/                          # Logique métier
│   ├── AccessService.java            # Accès unifié avec cache Caffeine
│   ├── SocieteComptableService.java
│   ├── SocieteService.java
│   ├── UserSocieteComptableService.java
│   ├── ComptableSocietesService.java
│   ├── UserSocietesService.java
│   └── PermissionService.java
├── repository/                       # Accès données (jOOQ)
│   ├── SocieteComptableRepository.java
│   ├── SocieteRepository.java
│   ├── UserSocieteComptableRepository.java
│   ├── ComptableSocietesRepository.java
│   ├── UserSocietesRepository.java
│   ├── PermissionRepository.java
│   └── RolePermissionRepository.java
├── dto/                              # Objets de transfert
│   ├── UserAccessDto.java            # Résultat d'accès unifié
│   ├── SocieteComptableDto.java
│   ├── SocieteDto.java
│   ├── UserSocieteComptableDto.java
│   ├── ComptableSocietesDto.java
│   ├── UserSocietesDto.java
│   ├── PermissionDto.java
│   ├── RolePermissionDto.java
│   └── request/                      # DTOs de requête
│       ├── CreateSocieteComptableRequest.java
│       ├── UpdateSocieteComptableRequest.java
│       ├── CreateSocieteRequest.java
│       ├── UpdateSocieteRequest.java
│       ├── AssignUserToSocieteComptableRequest.java
│       ├── AssignComptableToSocieteRequest.java
│       ├── UpdateComptableSocieteAccessRequest.java
│       ├── AssignUserToSocieteRequest.java
│       ├── CreatePermissionRequest.java
│       └── AssignPermissionToRoleRequest.java
└── enums/
    ├── CabinetRole.java              # MANAGER, COMPTABLE, ASSISTANT
    └── SocieteRole.java              # MANAGER, FINANCE, VIEWER
```

## Endpoints API

### Sociétés Comptables (`/api/societes-comptables`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Créer un cabinet comptable |
| PUT | `/{id}` | Modifier un cabinet |
| DELETE | `/{id}` | Supprimer un cabinet |
| GET | `/{id}` | Récupérer par ID |
| GET | `/matricule/{matriculeFiscale}` | Récupérer par matricule |
| GET | `/` | Lister tous |
| GET | `/active` | Lister les actifs |
| GET | `/search?q=` | Rechercher par raison sociale |

### Sociétés Clientes (`/api/societes`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Créer une société cliente |
| PUT | `/{id}` | Modifier une société |
| DELETE | `/{id}` | Supprimer une société |
| GET | `/{id}` | Récupérer par ID |
| GET | `/matricule/{matriculeFiscale}` | Récupérer par matricule |
| GET | `/` | Lister toutes |
| GET | `/active` | Lister les actives |
| GET | `/cabinet/{societeComptableId}` | Lister par cabinet |
| GET | `/search?q=` | Rechercher par raison sociale |
| GET | `/secteur/{secteur}` | Lister par secteur |

### Utilisateurs Cabinet (`/api/user-societe-comptable`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Assigner un utilisateur à un cabinet |
| PUT | `/{id}/role?role=` | Modifier le rôle |
| PUT | `/{id}/deactivate` | Désactiver |
| DELETE | `/{id}` | Supprimer |
| GET | `/{id}` | Récupérer par ID |
| GET | `/user/{userId}` | Récupérer par utilisateur |
| GET | `/cabinet/{societeComptableId}` | Lister par cabinet |
| GET | `/cabinet/{societeComptableId}/manager` | Récupérer le manager |

### Accès Comptables (`/api/comptable-societes`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Donner accès à un comptable |
| PUT | `/{id}` | Modifier les droits |
| DELETE | `/user/{userId}/societe/{societeId}` | Révoquer l'accès |
| GET | `/user/{userId}` | Lister les accès d'un comptable |
| GET | `/user/{userId}/societes` | Lister les sociétés accessibles |
| GET | `/check/access?userId=&societeId=` | Vérifier l'accès |
| GET | `/check/write?userId=&societeId=` | Vérifier droit écriture |
| GET | `/check/validate?userId=&societeId=` | Vérifier droit validation |

### Utilisateurs Société (`/api/user-societes`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Assigner un utilisateur à une société |
| PUT | `/{id}/role?role=` | Modifier le rôle |
| PUT | `/{id}/deactivate` | Désactiver |
| DELETE | `/{id}` | Supprimer |
| GET | `/user/{userId}` | Récupérer par utilisateur |
| GET | `/societe/{societeId}` | Lister par société |
| GET | `/societe/{societeId}/manager` | Récupérer le manager |
| GET | `/user/{userId}/societe` | Récupérer la société de l'utilisateur |

### Permissions (`/api/permissions`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/` | Créer une permission |
| DELETE | `/{id}` | Supprimer une permission |
| GET | `/{id}` | Récupérer par ID |
| GET | `/code/{code}` | Récupérer par code |
| GET | `/` | Lister toutes |
| GET | `/resource/{resource}` | Lister par ressource |
| GET | `/resources` | Lister les ressources distinctes |
| GET | `/actions` | Lister les actions distinctes |
| POST | `/role-assignment` | Assigner permission à un rôle |
| DELETE | `/role/{role}/permission/{permissionId}` | Révoquer |
| GET | `/role/{role}` | Lister les permissions d'un rôle |
| GET | `/roles` | Lister les rôles |
| GET | `/check?role=&permissionCode=` | Vérifier permission |
| GET | `/check/resource?role=&resource=&action=` | Vérifier accès ressource |

### Accès Unifié (`/api/access`) - AVEC CACHE CAFFEINE

**Endpoint principal pour vérifier les accès utilisateur.** Combine les deux sources d'accès:
- **COMPTABLE**: Accès via `comptable_societes` (comptable avec accès à la société cliente)
- **MEMBRE**: Accès via `user_societes` (employé/manager de la société)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/user/{userId}/societe/{societeId}` | Détails complets d'accès (type, rôle, droits) |
| GET | `/check?userId=&societeId=` | Vérification rapide d'accès (comptable OU membre) |
| GET | `/check/write?userId=&societeId=` | Vérifier droit d'écriture |
| GET | `/check/validate?userId=&societeId=` | Vérifier droit de validation |
| GET | `/check/permission?userId=&societeId=&permissionCode=` | Vérifier une permission spécifique |
| GET | `/user/{userId}/societe/{societeId}/permissions` | Lister toutes les permissions |
| GET | `/user/{userId}/societes` | **Lister TOUTES les sociétés accessibles** (comptable + membre) |
| GET | `/user/{userId}/societes/write` | Lister les sociétés avec droit d'écriture |
| DELETE | `/cache` | Invalider tout le cache d'accès |
| DELETE | `/cache/user/{userId}/societe/{societeId}` | Invalider le cache pour un accès spécifique |

## Cache Caffeine

Le service utilise **Caffeine** pour mettre en cache les vérifications d'accès fréquentes.

### Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
    public static final String USER_ACCESS_CACHE = "userAccessCache";
    public static final String USER_PERMISSIONS_CACHE = "userPermissionsCache";

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .recordStats();
    }
}
```

### Caches disponibles

| Cache | TTL | Description |
|-------|-----|-------------|
| `userAccessCache` | 5 min | Résultats des vérifications d'accès |
| `userPermissionsCache` | 5 min | Liste des permissions par utilisateur/société |

### Invalidation du cache

Le cache est automatiquement invalidé après 5 minutes. Pour une invalidation manuelle:

```bash
# Invalider tout le cache
DELETE /api/access/cache

# Invalider pour un utilisateur/société spécifique
DELETE /api/access/cache/user/{userId}/societe/{societeId}
```

**Important**: Après modification des accès (via `/api/comptable-societes` ou `/api/user-societes`),
il est recommandé d'invalider le cache correspondant.

## Patterns de Code

### Repository (jOOQ)

```java
@Repository
@RequiredArgsConstructor
public class SocieteComptableRepository {
    private final DSLContext dsl;

    public Optional<SocietesComptables> findById(Long id) {
        return dsl.selectFrom(SOCIETES_COMPTABLES)
                .where(SOCIETES_COMPTABLES.ID.eq(id))
                .fetchOptional()
                .map(r -> r.into(SocietesComptables.class));
    }

    public boolean hasActiveAccess(Long userId, Long societeId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(COMPTABLE_SOCIETES)
                        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
                        .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
                        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
                        .and(COMPTABLE_SOCIETES.DATE_FIN.isNull()
                                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now()))));
    }
}
```

### Service

```java
@Service
@RequiredArgsConstructor
public class ComptableSocietesService {
    private final ComptableSocietesRepository comptableSocietesRepository;

    @Transactional(readOnly = true)
    public boolean hasAccess(Long userId, Long societeId) {
        return comptableSocietesRepository.hasActiveAccess(userId, societeId);
    }
}
```

### Controller (avec Swagger)

```java
@RestController
@RequestMapping("/api/comptable-societes")
@RequiredArgsConstructor
@Tag(name = "Accès Comptables", description = "Gestion des accès")
public class ComptableSocietesController {

    @GetMapping("/check/access")
    @Operation(summary = "Vérifier l'accès")
    public ResponseEntity<Boolean> hasAccess(
            @RequestParam Long userId,
            @RequestParam Long societeId) {
        return ResponseEntity.ok(service.hasAccess(userId, societeId));
    }
}
```

## Configuration

### application.yml

```yaml
spring:
  application:
    name: authz-service
  datasource:
    url: jdbc:postgresql://localhost:5432/compta
  flyway:
    enabled: true
    schemas: authz
    default-schema: authz

server:
  port: 8082
  servlet:
    context-path: /authz

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

## Documentation Swagger

Accessible à: `http://localhost:8082/authz/swagger-ui.html`

## Cas d'utilisation typiques

### Créer un nouveau cabinet avec son manager

1. `POST /api/societes-comptables` - Créer le cabinet
2. `POST /api/user-societe-comptable` - Assigner l'utilisateur comme MANAGER

### Donner accès à un comptable sur plusieurs sociétés

1. Vérifier que l'utilisateur est bien dans `user_societe_comptable`
2. Pour chaque société: `POST /api/comptable-societes` avec les droits appropriés

### Vérifier les autorisations avant une action

1. `GET /api/comptable-societes/check/write?userId=X&societeId=Y`
2. Si `false`, refuser l'action

### Lister les sociétés accessibles à un comptable

1. `GET /api/comptable-societes/user/{userId}/societes`
