# Configuration des Tests d'Intégration

## Vue d'ensemble

Cette configuration centralisée utilise **Testcontainers** pour fournir une base de données PostgreSQL réelle pendant les tests d'intégration. Tous les modules peuvent hériter de `AbstractIntegrationTest` pour bénéficier de cette configuration.

## Prérequis

### Docker

**Testcontainers nécessite Docker pour fonctionner.**

- **Windows** : [Docker Desktop pour Windows](https://docs.docker.com/desktop/install/windows-install/)
- **Mac** : [Docker Desktop pour Mac](https://docs.docker.com/desktop/install/mac-install/)
- **Linux** : [Docker Engine](https://docs.docker.com/engine/install/)

Vérifiez que Docker est en cours d'exécution :
```bash
docker --version
docker ps
```

## Utilisation

### Dans vos tests

Héritez simplement de `AbstractIntegrationTest` :

```java
package tn.cyberious.compta;

import org.junit.jupiter.api.Test;
import tn.cyberious.compta.test.AbstractIntegrationTest;

class MyIntegrationTest extends AbstractIntegrationTest {

    @Test
    void myTest() {
        // Votre code de test avec accès à une vraie base PostgreSQL
        // Flyway a déjà exécuté les migrations
    }
}
```

### Configuration automatique

La classe `AbstractIntegrationTest` configure automatiquement :

- ✅ **Conteneur PostgreSQL** (postgres:17-alpine)
- ✅ **DataSource** configurée automatiquement via `@ServiceConnection`
- ✅ **Flyway** activé et migrations exécutées
- ✅ **JOOQ** configuré avec le dialecte PostgreSQL
- ✅ **Réutilisation du conteneur** entre les tests (performance)

### Profil activé

Les tests utilisent automatiquement le profil `integration-test` configuré dans `application-integration-test.properties`.

## Avantages

### Vs H2

- ✅ **Tests avec PostgreSQL réel** : pas de problèmes de compatibilité SQL
- ✅ **Migrations Flyway fonctionnelles** : syntaxe PL/pgSQL supportée
- ✅ **Types de données exacts** : JSON, JSONB, arrays PostgreSQL, etc.
- ✅ **Fonctions et triggers** : tout le SQL PostgreSQL est supporté

### Performance

- 🚀 **Conteneur réutilisé** : avec `withReuse(true)`, le conteneur est partagé entre les exécutions
- 🚀 **Démarrage rapide** : PostgreSQL Alpine est léger (~15s au premier démarrage)
- 🚀 **Tests parallèles** : chaque module peut avoir son propre conteneur ou partager

## Configuration personnalisée

### Profil additionnel

Si vous avez besoin d'un profil spécifique supplémentaire :

```java
@ActiveProfiles({"integration-test", "my-custom-profile"})
class MyTest extends AbstractIntegrationTest {
    // ...
}
```

### Configuration spécifique au module

Créez `application-integration-test.properties` dans votre module pour surcharger la configuration :

```properties
# Désactiver une fonctionnalité spécifique pour les tests
spring.flyway.locations=classpath:db/migration,classpath:db/test-data
```

## Dépannage

### Docker non disponible

```
Could not find a valid Docker environment
```

**Solution** : Démarrez Docker Desktop ou Docker Engine.

### Port déjà utilisé

Testcontainers utilise des ports aléatoires, mais si vous avez un conflit :

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
    .withDatabaseName("testdb")
    .withExposedPorts(5432); // Port personnalisé si nécessaire
```

### Logs Docker

Pour voir les logs du conteneur :

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers")));
```

## Ressources

- [Documentation Testcontainers](https://testcontainers.com/)
- [Testcontainers Spring Boot](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Testcontainers PostgreSQL](https://testcontainers.com/modules/postgresql/)
