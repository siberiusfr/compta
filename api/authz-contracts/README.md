# authz-contracts

Librairie contenant les DTOs et les clients Feign pour communiquer avec le service d'autorisation (authz-service).

## Contenu

### DTOs

- `PermissionDto` - Représente une permission granulaire
- `RolePermissionDto` - Association entre un rôle et une permission
- `SocieteDto` - Représente une société cliente
- `UserSocietesDto` - Association entre un utilisateur et une société cliente

### Clients Feign

- `AuthzPermissionClient` - Gestion des permissions et des rôles
- `AuthzUserSocietesClient` - Gestion des associations utilisateur-société
- `AuthzSocieteClient` - Gestion des sociétés clientes

## Utilisation

### 1. Ajouter la dépendance

```xml
<dependency>
    <groupId>tn.cyberious.compta</groupId>
    <artifactId>authz-contracts</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Activer Feign

Ajouter l'annotation `@EnableFeignClients` avec le package des clients :

```java
@EnableFeignClients(basePackages = "tn.cyberious.compta.authz.client")
@SpringBootApplication
public class YourApplication {

  public static void main(String[] args) {
    SpringApplication.run(YourApplication.class, args);
  }
}
```

### 3. Configurer l'URL du service authz

Dans `application.yml` :

```yaml
authz:
  service:
    url: http://localhost:8085 # ou l'URL du service authz-service
```

### 4. Utiliser les clients

```java
@Service
@RequiredArgsConstructor
public class YourService {

  private final AuthzPermissionClient permissionClient;
  private final AuthzUserSocietesClient userSocietesClient;
  private final AuthzSocieteClient societeClient;

  public void checkPermissions() {
    List<PermissionDto> permissions = permissionClient.findAll();
    Boolean hasAccess = permissionClient.hasPermission("COMPTABLE", "JOURNAL_ENTRY_CREATE");
  }

  public void getUserSociete(Long userId) {
    UserSocietesDto userSociete = userSocietesClient.findActiveByUserId(userId);
    SocieteDto societe = societeClient.findById(userSociete.societeId());
  }
}
```

## Configuration avancée

### Configuration Feign personnalisée

La classe `AuthzFeignConfig` fournit une configuration par défaut :

- Logger niveau BASIC
- Header `Content-Type: application/json` sur toutes les requêtes

Vous pouvez surcharger cette configuration dans votre propre classe :

```java
@Configuration
public class CustomFeignConfig {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public RequestInterceptor authzRequestInterceptor() {
    return (template) -> {
      template.header("Authorization", "Bearer " + getToken());
      template.header("Content-Type", "application/json");
    };
  }
}
```

## Méthodes disponibles

### AuthzPermissionClient

- `findById(Long id)` - Récupérer une permission par ID
- `findByCode(String code)` - Récupérer une permission par code
- `findAll()` - Lister toutes les permissions
- `findByResource(String resource)` - Lister les permissions d'une ressource
- `findByAction(String action)` - Lister les permissions d'une action
- `findDistinctResources()` - Lister les ressources distinctes
- `findDistinctActions()` - Lister les actions distinctes
- `findPermissionsByRole(String role)` - Lister les permissions d'un rôle
- `findRolePermissionsByRole(String role)` - Lister les assignations d'un rôle
- `findDistinctRoles()` - Lister les rôles
- `hasPermission(String role, String permissionCode)` - Vérifier une permission
- `hasPermissionOnResource(String role, String resource, String action)` - Vérifier l'accès ressource

### AuthzUserSocietesClient

- `findById(Long id)` - Récupérer une assignation par ID
- `findByUserId(Long userId)` - Récupérer l'assignation d'un utilisateur
- `findActiveByUserId(Long userId)` - Récupérer l'assignation active
- `findBySocieteId(Long societeId)` - Lister les utilisateurs d'une société
- `findActiveBySocieteId(Long societeId)` - Lister les utilisateurs actifs
- `findManagerBySocieteId(Long societeId)` - Récupérer le manager
- `findSocieteByUserId(Long userId)` - Récupérer la société d'un utilisateur

### AuthzSocieteClient

- `findById(Long id)` - Récupérer une société par ID
- `findByMatriculeFiscale(String matriculeFiscale)` - Récupérer par matricule fiscale
- `findAll()` - Lister toutes les sociétés
- `findAllActive()` - Lister les sociétés actives
- `findBySocieteComptableId(Long societeComptableId)` - Lister par cabinet
- `findActiveBySocieteComptableId(Long societeComptableId)` - Lister les actives par cabinet
- `search(String q)` - Rechercher par raison sociale
- `findBySecteur(String secteur)` - Lister par secteur d'activité
