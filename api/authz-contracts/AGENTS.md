# AGENTS.md

Instructions pour les agents IA travaillant avec la librairie authz-contracts.

## Description

`authz-contracts` est une librairie contenant les DTOs et les clients Feign pour communiquer avec le service d'autorisation (authz-service).

## Dépendance

Pour utiliser cette librairie, ajoutez la dépendance suivante dans le pom.xml de votre service :

```xml
<dependency>
    <groupId>tn.cyberious.compta</groupId>
    <artifactId>authz-contracts</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

### 1. Activer Feign

Ajoutez l'annotation `@EnableFeignClients` dans votre classe principale d'application :

```java
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "tn.cyberious.compta.authz.client")
@SpringBootApplication
public class YourApplication {

  public static void main(String[] args) {
    SpringApplication.run(YourApplication.class, args);
  }
}
```

### 2. Configurer l'URL du service authz

Dans `application.yml` :

```yaml
authz:
  service:
    url: ${AUTHZ_SERVICE_URL:http://localhost:8085}
```

### 3. Activation de compta-security-commons

La librairie nécessite `compta-security-commons` pour propager les headers d'authentification. Ajoutez la dépendance :

```xml
<dependency>
    <groupId>tn.cyberious.compta</groupId>
    <artifactId>compta-security-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Clients Feign

### AuthzPermissionClient

Utilisé pour gérer les permissions et les rôles.

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzPermissionClient permissionClient;

  public void checkPermission() {
    List<PermissionDto> permissions = permissionClient.findAll();
    PermissionDto permission = permissionClient.findByCode("JOURNAL_ENTRY_CREATE");
    Boolean hasAccess = permissionClient.hasPermission("COMPTABLE", "JOURNAL_ENTRY_CREATE");
    Boolean hasResourceAccess = permissionClient.hasPermissionOnResource(
      "COMPTABLE",
      "JOURNAL_ENTRY",
      "CREATE"
    );
  }
}
```

### AuthzUserSocietesClient

Utilisé pour gérer les associations utilisateur-société.

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzUserSocietesClient userSocietesClient;

  public void getUserSociete(Long userId) {
    UserSocietesDto userSociete = userSocietesClient.findActiveByUserId(userId);
    SocieteDto societe = userSocietesClient.findSocieteByUserId(userId);
  }
}
```

### AuthzComptableSocietesClient

Utilisé pour gérer les accès des comptables aux sociétés clientes.

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzComptableSocietesClient comptableSocietesClient;

  public void checkAccess(Long userId, Long societeId) {
    Boolean hasAccess = comptableSocietesClient.hasAccess(userId, societeId);
    Boolean hasWriteAccess = comptableSocietesClient.hasWriteAccess(userId, societeId);
  }

  public void getComptableAccess(Long userId, Long societeId) {
    ComptableSocietesDto access = comptableSocietesClient.findByUserIdAndSocieteId(
      userId,
      societeId
    );
  }
}
```

### AuthzSocieteComptableClient

Utilisé pour gérer les utilisateurs du cabinet.

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzSocieteComptableClient societeComptableClient;

  public void assignUser(Long userId, Long societeComptableId, String role) {
    AssignUserToSocieteComptableRequest request = new AssignUserToSocieteComptableRequest(
      userId,
      societeComptableId,
      role
    );
    UserSocieteComptableDto assigned = societeComptableClient.assignUser(request);
  }

  public void updateRole(Long id, String role) {
    societeComptableClient.updateRole(id, role);
  }
}
```

### AuthzSocieteComptableClient

Utilisé pour gérer les sociétés comptables (cabinets).

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzSocieteComptableClient societeComptableClient;

  public void getSocieteInfo(Long societeComptableId) {
    SocieteComptableDto societe = societeComptableClient.findById(societeComptableId);
  }

  public void createCabinet(CreateSocieteComptableRequest request) {
    SocieteComptableDto created = societeComptableClient.create(request);
  }

  public void searchCabinets(String query) {
    List<SocieteComptableDto> results = societeComptableClient.search(query);
  }
}
```

### AuthzAccessClient

Utilisé pour vérifier les accès unifiés (comptable ou membre de société).

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzAccessClient accessClient;

  public void checkAccess(Long userId, Long societeId) {
    Boolean hasAccess = accessClient.hasAccess(userId, societeId);
    Boolean hasWriteAccess = accessClient.hasWriteAccess(userId, societeId);
  }

  public List<SocieteAccessDto> getAccessibleSocietes(Long userId) {
    return accessClient.getAccessibleSocietes(userId);
  }

  public List<String> getUserPermissions(Long userId, Long societeId) {
    return accessClient.getUserPermissions(userId, societeId);
  }
}
```

## Propagation des Headers

La librairie propage automatiquement les headers d'authentification depuis `SecurityContextHolder` vers les appels Feign :

- `X-User-Id` - ID de l'utilisateur authentifié
- `X-User-Username` - Nom d'utilisateur
- `X-User-Email` - Email de l'utilisateur
- `X-User-Roles` - Rôles de l'utilisateur
- `X-User-Societe-Ids` - IDs des sociétés de l'utilisateur
- `X-User-Primary-Societe-Id` - ID de la société principale
- `X-User-Permissions` - Permissions de l'utilisateur
- `X-Request-Id` - ID de la requête (si disponible)

Ces headers sont ajoutés uniquement si l'utilisateur est authentifié dans le SecurityContext.

## DTOs

### PermissionDto

Représente une permission granulaire.

```java
public record PermissionDto(
  Long id,
  String code, // ex: "JOURNAL_ENTRY_CREATE"
  String resource, // ex: "JOURNAL_ENTRY"
  String action, // ex: "CREATE"
  String description,
  LocalDateTime createdAt
) {}
```

### RolePermissionDto

Association entre un rôle et une permission.

```java
public record RolePermissionDto(
  Long id,
  String role, // ex: "COMPTABLE"
  Long permissionId,
  LocalDateTime createdAt
) {}
```

### SocieteDto

Représente une société cliente.

```java
public record SocieteDto(
  Long id,
  String raisonSociale,
  String matriculeFiscale,
  String codeTva,
  String codeDouane,
  String registreCommerce,
  String formeJuridique,
  BigDecimal capitalSocial,
  LocalDate dateCreation,
  String adresse,
  String ville,
  String codePostal,
  String telephone,
  String fax,
  String email,
  String siteWeb,
  String activite,
  String secteur,
  Long societeComptableId,
  Boolean isActive,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
```

### UserSocietesDto

Association entre un utilisateur et une société cliente.

```java
public record UserSocietesDto(
  Long id,
  Long userId,
  Long societeId,
  String role, // ex: "FINANCE", "VIEWER"
  LocalDate dateDebut,
  LocalDate dateFin,
  Boolean isActive,
  LocalDateTime createdAt
) {}
```

### ComptableSocietesDto

Accès d'un comptable à une société cliente.

```java
public record ComptableSocietesDto(
  Long id,
  Long userId,
  Long societeId,
  Boolean canRead,
  Boolean canWrite,
  Boolean canValidate,
  LocalDate dateDebut,
  LocalDate dateFin,
  Boolean isActive,
  LocalDateTime createdAt
) {}
```

### UserSocieteComptableDto

Association entre un utilisateur et une société comptable (cabinet).

```java
public record UserSocieteComptableDto(
  Long id,
  Long userId,
  Long societeComptableId,
  String role, // ex: "MANAGER", "COMPTABLE", "ASSISTANT"
  LocalDate dateDebut,
  LocalDate dateFin,
  Boolean isActive,
  LocalDateTime createdAt
) {}
```

### SocieteComptableDto

Représente une société comptable (cabinet).

```java
public record SocieteComptableDto(
  Long id,
  String raisonSociale,
  String matriculeFiscale,
  String codeTva,
  String adresse,
  String ville,
  String codePostal,
  String telephone,
  String email,
  String siteWeb,
  Boolean isActive,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
```

### UserAccessDto

Information d'accès unifié d'un utilisateur à une société.

```java
public record UserAccessDto(
  Long userId,
  Long societeId,
  boolean hasAccess,
  AccessType accessType, // COMPTABLE, MEMBRE, or NONE
  String role,
  Boolean canRead,
  Boolean canWrite,
  Boolean canValidate
) {
  public enum AccessType {
    COMPTABLE, // Accès via comptable_societes
    MEMBRE, // Accès via user_societes (employé de la société)
    NONE // Pas d'accès
  }

  public static UserAccessDto noAccess(Long userId, Long societeId) {
    return UserAccessDto.builder()
      .userId(userId)
      .societeId(societeId)
      .hasAccess(false)
      .accessType(AccessType.NONE)
      .build();
  }
}
```

### SocieteAccessDto

Sociète accessible par un utilisateur avec détails d'accès.

```java
public record SocieteAccessDto(
  Long societeId,
  String raisonSociale,
  String matriculeFiscale,
  AccessType accessType, // COMPTABLE, MEMBRE, or NONE
  String role,
  Boolean canRead,
  Boolean canWrite,
  Boolean canValidate
) {}
```

## DTOs de Requête

### AssignUserToSocieteComptableRequest

Requête pour assigner un utilisateur à une société comptable.

```java
public record AssignUserToSocieteComptableRequest(
  Long userId,
  Long societeComptableId,
  String role, // MANAGER, COMPTABLE, ASSISTANT
  LocalDate dateDebut,
  LocalDate dateFin
) {}
```

### AssignComptableToSocieteRequest

Requête pour assigner un comptable à une société cliente.

```java
public record AssignComptableToSocieteRequest(
    Long userId,
    Long societeId,
    Boolean canRead = true,
    Boolean canWrite = true,
    Boolean canValidate = false,
    LocalDate dateDebut,
    LocalDate dateFin
) {}
```

### UpdateComptableSocieteAccessRequest

Requête pour mettre à jour les droits d'un comptable sur une société.

```java
public record UpdateComptableSocieteAccessRequest(
  Boolean canRead,
  Boolean canWrite,
  Boolean canValidate,
  LocalDate dateFin,
  Boolean isActive
) {}
```

### CreateSocieteComptableRequest

Requête pour créer une nouvelle société comptable.

```java
public record CreateSocieteComptableRequest(
  String raisonSociale,
  String matriculeFiscale,
  String codeTva,
  String adresse,
  String ville,
  String codePostal,
  String telephone,
  String email,
  String siteWeb
) {}
```

### UpdateSocieteComptableRequest

Requête pour mettre à jour une société comptable.

```java
public record UpdateSocieteComptableRequest(
  String raisonSociale,
  String codeTva,
  String adresse,
  String ville,
  String codePostal,
  String telephone,
  String email,
  String siteWeb,
  Boolean isActive
) {}
```

## Rôles standards

Les rôles suivants sont utilisés dans le système :

- **ADMIN** - Administrateur système
- **COMPTABLE** - Comptable
- **SOCIETE** - Utilisateur société cliente
- **EMPLOYEE** - Employé société cliente

Rôles dans une société cliente :

- **MANAGER** - Manager de la société
- **FINANCE** - Responsable financier
- **VIEWER** - Lecture seule

## Gestion des erreurs

Les clients Feign peuvent lever des exceptions en cas d'erreur :

- `FeignException` - Erreur de communication avec le service
- `FeignClientException` - Erreur côté serveur (4xx, 5xx)

Exemple de gestion :

```java
try {
    PermissionDto permission = permissionClient.findById(id);
} catch (FeignException e) {
    log.error("Erreur de communication avec authz-service", e);
    throw new ServiceUnavailableException("Service d'autorisation indisponible");
} catch (FeignClientException e) {
    if (e.status() == 404) {
        throw new ResourceNotFoundException("Permission non trouvée");
    }
    throw e;
}
```

## Points d'attention

1. **Toujours formater le code** avec `mvn spotless:apply` avant de commiter
2. **La dépendance compta-security-commons** est marquée `provided` - elle doit être fournie par le service consommateur
3. **Les headers sont propagés automatiquement** - ne les ajoutez pas manuellement dans les requêtes
4. **L'URL du service est configurable** via la propriété `authz.service.url`
5. **Les DTOs sont immutables** (records) - ne tentez pas de les modifier
