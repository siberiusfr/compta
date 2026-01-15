# Architecture des Migrations de Base de Données

## Vue d'ensemble

Ce document décrit l'architecture des migrations de base de données pour le projet Compta API.

## Architecture

### Principe

Les migrations de base de données sont gérées de manière **décentralisée** avec **isolation par schéma** :

- **Flyway est configuré dans `compta-commons`** et hérité par tous les services
- **Chaque service gère ses propres migrations** au démarrage
- **Chaque service a son propre schéma PostgreSQL** pour une isolation complète
- **`migration-service` sert uniquement à consulter** l'état des migrations

### Avantages

1. **Isolation des données** - Chaque service a son propre schéma PostgreSQL
2. **Autonomie des services** - Chaque service peut évoluer indépendamment
3. **Déploiement simplifié** - Pas besoin d'exécuter un service de migration séparé
4. **Cohérence** - Les migrations sont appliquées automatiquement au démarrage
5. **Sécurité** - Isolation des données entre services
6. **Traçabilité** - Le `migration-service` permet de consulter l'état global

## Structure des migrations

### Schémas PostgreSQL

Chaque service utilise son propre schéma :

| Service              | Schéma PostgreSQL    | Port |
|----------------------|----------------------|------|
| accounting-service   | `accounting`         | 8082 |
| auth-service         | `auth`               | 8083 |
| hr-service           | `hr`                 | 8084 |
| document-service     | `document`           | 8085 |
| notification-service | `notification`       | 8086 |
| migration-service    | (consultation seule) | 8081 |

### Répertoires

```
accounting-service/
└── src/main/resources/db/migration/
    └── V1__init_accounting_schema.sql    # Crée le schéma accounting + tables

auth-service/
└── src/main/resources/db/migration/
    └── V1__init_auth_schema.sql          # Crée le schéma auth + tables
    
authz-service/
└── src/main/resources/db/migration/
    └── V1__init_authz_schema.sql          # Crée le schéma auth + tables

hr-service/
└── src/main/resources/db/migration/
    └── V1__init_hr_schema.sql            # Crée le schéma hr + tables

document-service/
└── src/main/resources/db/migration/
    └── V1__init_document_schema.sql      # Crée le schéma document + tables

notification-service/
└── src/main/resources/db/migration/
    └── V1__init_notification_schema.sql  # Crée le schéma notification + tables

migration-service/
├── src/main/resources/db/migration/
│   └── V1__create_referentiel_schema.sql               # Copie pour consultation
└── README.md                              # Documentation du service
```

### Convention de nommage

Les migrations suivent la convention Flyway :

- **Format** : `V{version}__{description}.sql`
- **Exemples** :
    - `V1__init_accounting_schema.sql` - Migration initiale avec création du schéma
    - `V2__add_invoice_table.sql` - Ajout d'une table
    - `V3__add_audit_columns.sql` - Ajout de colonnes d'audit

### Structure d'une migration V1

Chaque service crée son propre schéma dans sa migration V1 :

```sql
-- Créer le schéma
CREATE SCHEMA IF NOT EXISTS {service_schema};

-- Créer les fonctions utilitaires
CREATE OR REPLACE FUNCTION {service_schema}.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Créer les tables dans le schéma
CREATE TABLE IF NOT EXISTS {service_schema}.table_name (
    id BIGSERIAL PRIMARY KEY,
    ...
);
```

## Configuration

### Dans compta-commons

Le fichier [`compta-commons/pom.xml`](compta-commons/pom.xml) inclut les dépendances Flyway :

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Le fichier [`compta-commons/src/main/resources/application.yml`](compta-commons/src/main/resources/application.yml) configure Flyway :

```yaml
spring:
  flyway:
    enabled: false                    # Désactivé par défaut
    baseline-on-migrate: true
    locations: classpath:db/migration
    table: flyway_schema_history
    validate-on-migrate: true
    clean-disabled: true
```

La classe [`FlywayConfig.java`](compta-commons/src/main/java/tn/cyberious/compta/config/FlywayConfig.java) fournit la stratégie de migration.

### Dans chaque service

Pour activer Flyway avec un schéma spécifique, configurez dans `application.yml` :

```yaml
spring:
  application:
    name: {service-name}
  
  flyway:
    enabled: true
    schemas: {service_schema}
    default-schema: {service_schema}

server:
  port: {service_port}
```

**Services configurés :**

- ✅ [`accounting-service`](accounting-service/src/main/resources/application.yml) - Schéma `accounting`
- ✅ [`auth-service`](auth-service/src/main/resources/application.yml) - Schéma `auth`
- ✅ [`hr-service`](hr-service/src/main/resources/application.yml) - Schéma `hr`
- ✅ [`document-service`](document-service/src/main/resources/application.yml) - Schéma `document`
- ✅ [`notification-service`](notification-service/src/main/resources/application.yml) - Schéma `notification`

### Migration Service

Le [`migration-service`](migration-service/README.md) est configuré pour **ne pas exécuter** les migrations :

```java
@Bean
public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
        // Ne rien faire - service en lecture seule
    };
}
```

## Ordre d'exécution

Lorsqu'un service démarre avec Flyway activé :

1. **Connexion à la base de données PostgreSQL**
2. **Vérification/Création du schéma spécifique** (ex: `accounting`)
3. **Vérification de la table `flyway_schema_history`** dans le schéma
4. **Exécution des migrations** (V1, V2, V3, etc.)
5. **Mise à jour de `flyway_schema_history`** dans le schéma

### Exemple pour accounting-service

```
1. Connexion à PostgreSQL
2. Création du schéma 'accounting' si nécessaire
3. V1__init_accounting_schema.sql
   → Crée le schéma accounting
   → Crée les tables: companies, accounts, journal_entries, etc.
   → Crée les index et triggers
4. V2__add_invoice_table.sql (si existe)
   → Ajoute la table invoices dans le schéma accounting
```

## Consultation des migrations

Le [`migration-service`](migration-service/README.md) expose des endpoints REST :

### Endpoints

| Endpoint                      | Description                 |
|-------------------------------|-----------------------------|
| `GET /api/migrations`         | Liste toutes les migrations |
| `GET /api/migrations/applied` | Migrations appliquées       |
| `GET /api/migrations/pending` | Migrations en attente       |
| `GET /api/migrations/failed`  | Migrations échouées         |
| `GET /api/migrations/info`    | Informations générales      |

### Exemple d'utilisation

```bash
# Démarrer le service
cd migration-service
mvn spring-boot:run

# Consulter les migrations
curl http://localhost:8081/api/migrations/info
```

## Workflow de développement

### Ajouter une nouvelle migration

1. **Créer le fichier SQL** dans le service concerné :
   ```
   {service}/src/main/resources/db/migration/V{n}__{description}.sql
   ```

2. **Écrire la migration** dans le schéma du service :
   ```sql
   -- V2__add_invoice_table.sql
   CREATE TABLE IF NOT EXISTS accounting.invoices (
       id BIGSERIAL PRIMARY KEY,
       invoice_number VARCHAR(50) NOT NULL UNIQUE,
       ...
   );
   
   CREATE INDEX idx_invoices_number ON accounting.invoices(invoice_number);
   ```

3. **Tester localement** :
   ```bash
   mvn spring-boot:run
   ```

4. **Vérifier l'application** :
   ```bash
   curl http://localhost:8081/api/migrations/info
   ```

### Références entre schémas

Si vous devez référencer une table d'un autre service (à éviter si possible) :

```sql
-- Dans accounting-service, référencer auth.users
CREATE TABLE accounting.user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    -- Note: Pas de FOREIGN KEY vers auth.users pour garder l'indépendance
    ...
);
```

**Recommandation** : Évitez les références entre schémas. Préférez dupliquer les données nécessaires ou utiliser des événements.

### Modifier une migration non appliquée

Si la migration n'a **jamais été appliquée** en production :

- Vous pouvez modifier le fichier directement
- Supprimez la ligne correspondante dans `{schema}.flyway_schema_history` en développement

### Corriger une migration appliquée

Si la migration a **déjà été appliquée** :

- **Ne jamais modifier le fichier existant**
- Créer une nouvelle migration pour corriger :
  ```sql
  -- V3__fix_accounts_table.sql
  ALTER TABLE accounting.accounts DROP COLUMN IF EXISTS wrong_column;
  ALTER TABLE accounting.accounts ADD COLUMN IF NOT EXISTS correct_column VARCHAR(100);
  ```

## Bonnes pratiques

### ✅ À faire

1. **Migrations idempotentes** - Utilisez `IF NOT EXISTS`, `IF EXISTS`
2. **Préfixer les objets** - Toujours utiliser le schéma : `accounting.table_name`
3. **Petites migrations** - Une migration = un changement logique
4. **Commentaires clairs** - Documentez le but de chaque migration
5. **Tester en local** - Avant de commiter
6. **Isolation des services** - Évitez les références entre schémas

### ❌ À éviter

1. **Modifier une migration appliquée** - Créez une nouvelle migration
2. **Migrations destructives** - Évitez `DROP TABLE` sans sauvegarde
3. **Références croisées** - Évitez les FOREIGN KEY entre schémas
4. **Migrations longues** - Divisez en plusieurs migrations
5. **Oublier le schéma** - Toujours préfixer : `schema.table`

## Tables créées par service

### accounting-service (schéma: accounting)

- `companies` - Entreprises
- `accounts` - Comptes comptables
- `journal_entries` - Écritures comptables
- `journal_entry_lines` - Lignes d'écriture

### auth-service (schéma: auth)

- `users` - Utilisateurs
- `roles` - Rôles
- `user_roles` - Liaison utilisateurs-rôles
- `permissions` - Permissions
- `role_permissions` - Liaison rôles-permissions
- `refresh_tokens` - Tokens de rafraîchissement
- `login_audit` - Audit des connexions

### hr-service (schéma: hr)

- `employees` - Employés
- `departments` - Départements
- `contracts` - Contrats
- `leaves` - Congés
- `evaluations` - Évaluations
- `payslips` - Fiches de paie

### document-service (schéma: document)

- `categories` - Catégories de documents
- `documents` - Documents
- `document_versions` - Versions de documents
- `tags` - Tags
- `document_tags` - Liaison documents-tags
- `document_shares` - Partages de documents
- `document_metadata` - Métadonnées personnalisées

### notification-service (schéma: notification)

- `templates` - Templates de notifications
- `notifications` - Notifications
- `user_preferences` - Préférences utilisateur
- `subscriptions` - Abonnements
- `delivery_logs` - Logs d'envoi
- `webhooks` - Webhooks

## Dépannage

### Erreur : "Validate failed: Migration checksum mismatch"

**Cause** : Une migration a été modifiée après application

**Solution** :

1. En développement : Supprimez la ligne dans `{schema}.flyway_schema_history`
2. En production : Créez une nouvelle migration corrective

### Erreur : "Schema does not exist"

**Cause** : Le schéma n'a pas été créé

**Solution** :

1. Vérifiez que la migration V1 crée bien le schéma : `CREATE SCHEMA IF NOT EXISTS {schema}`
2. Vérifiez la configuration : `spring.flyway.schemas` et `default-schema`

### Les migrations ne s'exécutent pas

**Vérifiez** :

1. `spring.flyway.enabled=true` dans `application.yml`
2. `spring.flyway.schemas` et `default-schema` sont configurés
3. Les fichiers sont dans `src/main/resources/db/migration/`
4. Le format de nommage est correct : `V{n}__{description}.sql`
5. La connexion à la base de données fonctionne

### Erreur de permission sur le schéma

**Solution** :

```sql
-- Donner les droits à l'utilisateur
GRANT ALL ON SCHEMA {schema} TO {user};
GRANT ALL ON ALL TABLES IN SCHEMA {schema} TO {user};
GRANT ALL ON ALL SEQUENCES IN SCHEMA {schema} TO {user};
```

## Références

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [Documentation migration-service](migration-service/README.md)
- [Configuration Flyway](compta-commons/src/main/java/tn/cyberious/compta/config/FlywayConfig.java)
- [PostgreSQL Schemas](https://www.postgresql.org/docs/current/ddl-schemas.html)
