# Configuration Sentry - Compta API

## Vue d'ensemble

Sentry a été intégré dans tous les microservices de l'application Compta pour le monitoring des erreurs et des performances. La configuration de base est centralisée dans `compta-commons`, et chaque service a ses propres tags personnalisés.

## Architecture

### Configuration centralisée

La classe `SentryConfig` dans `compta-commons/src/main/java/tn/cyberious/compta/config/SentryConfig.java` fournit:

- Configuration automatique du tag `service` basé sur `spring.application.name`
- Configuration de l'environnement (development, staging, production)
- Activation du tracing et des performances
- Configuration du taux d'échantillonnage des traces
- Gestion automatique des exceptions non gérées

### Services configurés

Tous les services suivants ont Sentry configuré avec des tags spécifiques:

| Service | Port | Tag service | Tag module |
|---------|------|-------------|------------|
| accounting-service | 8082 | accounting-service | accounting |
| auth-service | 8083 | auth-service | authentication |
| document-service | 8085 | document-service | document |
| hr-service | 8084 | hr-service | human-resources |
| migration-service | 8081 | migration-service | migration |
| notification-service | 8086 | notification-service | notification |

## Configuration

### Variables d'environnement requises

Pour chaque service, vous devez configurer les variables d'environnement suivantes:

```bash
# DSN Sentry (obligatoire pour activer Sentry)
export SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id

# Environnement (optionnel, par défaut: development)
export SENTRY_ENVIRONMENT=production
```

### Configuration dans application.yml

Chaque service a une configuration Sentry dans son fichier `application.yml`:

```yaml
sentry:
  dsn: ${SENTRY_DSN:} # Vide par défaut = Sentry désactivé
  environment: ${SENTRY_ENVIRONMENT:development}
  traces-sample-rate: 1.0 # 100% en dev, réduire en production
  send-default-pii: false
  enable-tracing: true
  tags:
    service: service-name
    module: module-name
```

### Taux d'échantillonnage recommandés

- **Development**: `1.0` (100%) - Capture toutes les traces
- **Staging**: `0.5` (50%) - Capture la moitié des traces
- **Production**: `0.1` (10%) - Capture 10% des traces

Pour modifier le taux d'échantillonnage en production:

```yaml
sentry:
  traces-sample-rate: 0.1
```

## Utilisation

### Capture automatique

Sentry capture automatiquement:

- Toutes les exceptions non gérées
- Les erreurs HTTP 4xx et 5xx
- Les requêtes HTTP (breadcrumbs)
- Les performances des endpoints REST

### Capture manuelle

Pour capturer manuellement des événements ou des exceptions:

```java
import io.sentry.Sentry;

// Capturer une exception
try {
    // Code susceptible de lancer une exception
} catch (Exception e) {
    Sentry.captureException(e);
}

// Capturer un message
Sentry.captureMessage("Quelque chose d'important s'est produit");

// Ajouter un contexte
Sentry.configureScope(scope -> {
    scope.setTag("custom-tag", "custom-value");
    scope.setContexts("user", Map.of(
        "id", userId,
        "username", username
    ));
});
```

### Breadcrumbs personnalisés

Pour ajouter des breadcrumbs (traces d'exécution):

```java
import io.sentry.Breadcrumb;
import io.sentry.Sentry;

Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setMessage("User performed action");
breadcrumb.setCategory("user.action");
breadcrumb.setLevel(SentryLevel.INFO);
breadcrumb.setData("action", "delete-document");
Sentry.addBreadcrumb(breadcrumb);
```

## Filtrage des données sensibles

### PII (Personally Identifiable Information)

La configuration par défaut désactive l'envoi automatique des PII:

```yaml
sentry:
  send-default-pii: false
```

Cela signifie que:
- Les adresses IP ne sont pas envoyées
- Les cookies ne sont pas envoyés
- Les en-têtes HTTP sensibles sont filtrés

### Filtrer des exceptions spécifiques

Pour ignorer certaines exceptions, ajoutez dans `SentryConfig.java`:

```java
options.setBeforeSend((event, hint) -> {
    // Ignorer les exceptions de validation
    if (event.getThrowable() instanceof ValidationException) {
        return null;
    }
    return event;
});
```

## Tags disponibles

Chaque événement Sentry contient automatiquement les tags suivants:

- `service`: Nom du service (ex: `auth-service`)
- `module`: Module fonctionnel (ex: `authentication`)
- `environment`: Environnement d'exécution (ex: `production`)
- `release`: Version du service (ex: `auth-service@1.0.0`)

## Performance monitoring

### Traces automatiques

Sentry trace automatiquement:

- Requêtes HTTP entrantes
- Requêtes JDBC vers PostgreSQL
- Appels de méthodes annotées avec `@SentryTransaction`

### Transactions personnalisées

Pour créer des transactions personnalisées:

```java
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;

ITransaction transaction = Sentry.startTransaction("my-transaction", "task");
try {
    // Code à tracer
    transaction.setStatus(SpanStatus.OK);
} catch (Exception e) {
    transaction.setStatus(SpanStatus.INTERNAL_ERROR);
    throw e;
} finally {
    transaction.finish();
}
```

### Spans personnalisés

Pour mesurer des opérations spécifiques:

```java
import io.sentry.ISpan;
import io.sentry.Sentry;

ISpan span = Sentry.getSpan();
if (span != null) {
    ISpan childSpan = span.startChild("database.query");
    try {
        // Opération de base de données
    } finally {
        childSpan.finish();
    }
}
```

## Dashboard Sentry

### Création du projet Sentry

1. Créez un compte sur [sentry.io](https://sentry.io)
2. Créez un nouveau projet pour chaque service (ou un projet global)
3. Sélectionnez "Java" comme plateforme
4. Copiez le DSN fourni

### Organisation recommandée

Option 1: Un projet par service
- Créez 6 projets Sentry (un par service)
- Chaque projet a son propre DSN
- Permet une isolation complète des erreurs

Option 2: Un projet global avec tags
- Créez un seul projet Sentry
- Utilisez le même DSN pour tous les services
- Filtrez les erreurs par tag `service` dans le dashboard

## Troubleshooting

### Sentry ne capture pas les erreurs

1. Vérifiez que `SENTRY_DSN` est configuré
2. Vérifiez les logs au démarrage pour voir si Sentry s'initialise
3. Testez avec une exception manuelle:

```java
Sentry.captureMessage("Test Sentry");
```

### Trop d'événements envoyés

Réduisez le taux d'échantillonnage:

```yaml
sentry:
  traces-sample-rate: 0.1 # 10% au lieu de 100%
```

### Données sensibles dans Sentry

1. Vérifiez que `send-default-pii: false`
2. Ajoutez un filtre `beforeSend` pour nettoyer les données

## Exemples de configuration par environnement

### Development

```yaml
sentry:
  dsn: ${SENTRY_DSN:}
  environment: development
  traces-sample-rate: 1.0
  send-default-pii: false
```

### Staging

```yaml
sentry:
  dsn: ${SENTRY_DSN}
  environment: staging
  traces-sample-rate: 0.5
  send-default-pii: false
```

### Production

```yaml
sentry:
  dsn: ${SENTRY_DSN}
  environment: production
  traces-sample-rate: 0.1
  send-default-pii: false
```

## Intégration CI/CD

Pour déployer avec Sentry:

```bash
# Variables d'environnement à configurer dans votre CI/CD
export SENTRY_DSN=https://...@sentry.io/...
export SENTRY_ENVIRONMENT=production

# Démarrer le service
java -jar service.jar
```

### Docker Compose

```yaml
services:
  auth-service:
    image: auth-service:latest
    environment:
      - SENTRY_DSN=${SENTRY_DSN}
      - SENTRY_ENVIRONMENT=production
    ports:
      - "8083:8083"
```

### Kubernetes

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sentry-config
data:
  SENTRY_ENVIRONMENT: production
---
apiVersion: v1
kind: Secret
metadata:
  name: sentry-secret
type: Opaque
stringData:
  SENTRY_DSN: https://...@sentry.io/...
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  template:
    spec:
      containers:
      - name: auth-service
        envFrom:
        - configMapRef:
            name: sentry-config
        - secretRef:
            name: sentry-secret
```

## Ressources

- [Documentation Sentry Java](https://docs.sentry.io/platforms/java/)
- [Documentation Sentry Spring Boot](https://docs.sentry.io/platforms/java/guides/spring-boot/)
- [Meilleures pratiques Sentry](https://docs.sentry.io/product/sentry-basics/concepts/best-practices/)
