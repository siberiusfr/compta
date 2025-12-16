# Migration Service

## Vue d'ensemble

Le service `migration-service` a été reconfiguré pour servir uniquement de **service de consultation** des migrations de base de données. Il ne gère plus l'exécution des migrations.

## Changement d'architecture

### Ancienne architecture
- `migration-service` gérait toutes les migrations de manière centralisée
- Les migrations étaient exécutées par ce service unique
- Tous les services dépendaient de `migration-service` pour les migrations

### Nouvelle architecture
- **Flyway est intégré dans `compta-commons`** - Tous les services héritent de la configuration Flyway
- **Chaque service gère ses propres migrations** - Les migrations sont exécutées au démarrage de chaque service
- **`migration-service` est en lecture seule** - Il fournit uniquement des endpoints de consultation

## Fonctionnalités

Le service expose les endpoints suivants pour consulter l'état des migrations:

### Endpoints disponibles

#### GET `/api/migrations`
Liste toutes les migrations (appliquées et en attente)

**Réponse:**
```json
[
  {
    "version": "1",
    "description": "init schema",
    "type": "SQL",
    "script": "V1__init_schema.sql",
    "checksum": 123456789,
    "installedOn": "2024-01-15T10:30:00",
    "installedBy": "compta_user",
    "executionTime": 150,
    "state": "SUCCESS"
  }
]
```

#### GET `/api/migrations/applied`
Liste uniquement les migrations qui ont été appliquées avec succès

#### GET `/api/migrations/pending`
Liste les migrations en attente d'exécution

#### GET `/api/migrations/failed`
Liste les migrations qui ont échoué

#### GET `/api/migrations/info`
Retourne des informations générales sur l'état des migrations

**Réponse:**
```json
{
  "current": {
    "version": "2",
    "description": "auth schema",
    "state": "SUCCESS"
  },
  "totalCount": 3,
  "appliedCount": 2,
  "pendingCount": 1,
  "failedCount": 0
}
```

## Configuration

### Application.yml

```yaml
spring:
  application:
    name: migration-service

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
    table: flyway_schema_history
    validate-on-migrate: false
    clean-disabled: true

server:
  port: 8081
```

### MigrationServiceConfig

Une configuration spéciale empêche l'exécution automatique des migrations:

```java
@Bean
public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
        // Ne rien faire - ce service est en lecture seule
    };
}
```

## Utilisation

### Démarrage du service

```bash
mvn spring-boot:run
```

### Accès à la documentation API

Une fois le service démarré, accédez à:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

### Exemple d'utilisation avec curl

```bash
# Lister toutes les migrations
curl http://localhost:8081/api/migrations

# Voir les migrations en attente
curl http://localhost:8081/api/migrations/pending

# Obtenir des informations générales
curl http://localhost:8081/api/migrations/info
```

## Architecture des migrations

### Structure des répertoires

```
compta-commons/
└── src/main/resources/db/migration/
    ├── V1__init_schema.sql          # Migrations communes
    └── README.md

accounting-service/
└── src/main/resources/db/migration/
    └── V2__accounting_schema.sql    # Migrations spécifiques

auth-service/
└── src/main/resources/db/migration/
    └── V2__auth_schema.sql          # Migrations spécifiques

migration-service/
└── src/main/resources/db/migration/
    └── V1__init_schema.sql          # Copie pour consultation
```

### Ordre d'exécution

1. **Migrations communes** (V1__*) de `compta-commons` - Exécutées en premier
2. **Migrations spécifiques** (V2__*, V3__*, etc.) de chaque service - Exécutées ensuite

### Activation de Flyway dans un service

Pour activer les migrations dans un service, ajoutez dans `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
```

## Bonnes pratiques

1. **Ne jamais modifier une migration appliquée** - Créez toujours une nouvelle migration
2. **Utiliser des migrations idempotentes** - Utilisez `IF NOT EXISTS`, `IF EXISTS`
3. **Tester avant de déployer** - Testez toujours sur un environnement de développement
4. **Documenter les migrations** - Ajoutez des commentaires clairs dans les fichiers SQL
5. **Versionner correctement** - Suivez la convention Flyway: `V{version}__{description}.sql`

## Dépannage

### Les migrations ne s'affichent pas

Vérifiez que:
1. La base de données est accessible
2. La table `flyway_schema_history` existe
3. Les migrations sont dans le bon répertoire (`db/migration`)

### Erreur de checksum

Si une migration a été modifiée après application:
1. Ne modifiez jamais une migration appliquée
2. Créez une nouvelle migration pour corriger
3. En développement, vous pouvez supprimer la table `flyway_schema_history` et recommencer

## Liens utiles

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [Guide des migrations](../compta-commons/src/main/resources/db/migration/README.md)
