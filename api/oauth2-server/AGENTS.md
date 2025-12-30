# OAuth2 Server - Agents

## Architecture Overview

Le serveur OAuth2 est le point central d'autorisation pour tous les microservices du projet Compta. Il fournit des tokens d'accès JWT et gère l'authentification des utilisateurs.

## Key Components

### Configuration
- **AuthorizationServerConfig**: Configuration du serveur OAuth2 avec Spring Authorization Server
  - Gestion des clients OAuth2 (confidentiels et publics avec PKCE)
  - Configuration des clés RSA pour la signature JWT
  - Configuration des flux: Authorization Code, Refresh Token, Client Credentials
  - Support PKCE (Proof Key for Code Exchange) pour les clients publics

### Security
- **CustomUserDetailsService**: Service de détails utilisateur personnalisé
  - Chargement des utilisateurs depuis la base de données via JOOQ
  - Chargement des rôles utilisateur
  - Implémentation de l'interface `UserDetailsService` de Spring Security

- **CustomUserDetails**: Implémentation personnalisée de UserDetails
  - Adaptation des enregistrements JOOQ aux UserDetails Spring Security
  - Gestion des autorités (rôles)

### Data Access
- **UserRepository**: Repository pour les opérations utilisateur via JOOQ
  - Recherche par username/email
  - Vérification d'existence
  - Insertion d'utilisateurs
  - Utilisation de `DSLContext` pour les requêtes JOOQ

### OAuth2 Features

#### Flux Supportés
1. **Authorization Code Flow**: Pour les applications web/SPAs
   - Utilise PKCE pour les clients publics
   - Redirection URI configurable
   - Consentement utilisateur requis

2. **Refresh Token Flow**: Renouvellement des tokens
   - Tokens d'accès avec durée de vie configurable (30 minutes)
   - Codes d'autorisation avec durée de vie configurable (5 minutes)

3. **Client Credentials Flow**: Pour l'authentification service-à-service
   - Authentification directe sans interaction utilisateur
   - Idéal pour les communications service-à-service

4. **OpenID Connect (OIDC)**:
   - Endpoint userinfo disponible
   - Scopes `openid`, `read`, `write`
   - JWT standard avec claims OIDC

#### Clients Configurés

| Client ID | Type | Secret | PKCE | Grants | Scopes |
|------------|------|--------|------|--------|--------|
| public-client | Public | None | Yes | authorization_code, refresh_token | openid, read, write |
| accounting-service | Confidential | accounting-secret | Yes | authorization_code, refresh_token, client_credentials | openid, read, write |
| authz-service | Confidential | authz-secret | Yes | authorization_code, refresh_token, client_credentials | openid, read, write |
| hr-service | Confidential | hr-secret | Yes | authorization_code, refresh_token, client_credentials | openid, read, write |

### Database Schema

#### Tables OAuth2 (Schema: oauth2)
- `oauth2_registered_client`: Clients OAuth2 enregistrés
- `oauth2_authorization`: Autorisations et tokens
- `oauth2_authorization_consent`: Consentements utilisateur

#### Tables Authentification (Schema: oauth2)
- `users`: Comptes utilisateurs
- `roles`: Rôles disponibles
- `user_roles`: Association utilisateurs-rôles

### Endpoints OAuth2

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `POST /oauth2/token` | POST | Échange de code pour token, refresh token, client credentials |
| `GET /oauth2/authorize` | GET | Endpoint d'autorisation (Authorization Code Flow) |
| `GET /.well-known/jwks.json` | GET | Clés publiques JWT |
| `POST /oauth2/revoke` | POST | Révocation de token |
| `POST /oauth2/introspect` | POST | Validation de token |

### Security Considerations

1. **PKCE Implementation**:
   - `requireProofKey(true)` activé pour tous les clients
   - Les clients publics utilisent PKCE (Proof Key for Code Exchange)
   - Les clients confidentiels utilisent PKCE pour la sécurité

2. **Token Security**:
   - Durée de vie des tokens d'accès: 30 minutes
   - Durée de vie des codes d'autorisation: 5 minutes
   - `reuseRefreshTokens(false)` pour éviter la réutilisation
   - Signature RSA 2048 bits pour JWT

3. **Client Secrets**:
   - Les secrets clients sont encodés avec BCrypt
   - Stockage sécurisé requis en production
   - Rotation périodique recommandée

4. **User Authentication**:
   - Mots de passe encodés avec BCrypt
   - Comptes désactivables (enabled, account_non_locked, etc.)
   - Rôles basés sur les permissions

### Integration with Services

Les microservices peuvent s'authentifier via OAuth2:

#### Service-to-Service (Client Credentials Flow)
```bash
# Exemple pour accounting-service
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "accounting-service:accounting-secret" \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

#### User Authorization Flow (Authorization Code)
1. Redirection vers l'endpoint d'autorisation
2. Utilisateur se connecte (username/password)
3. Consentement aux scopes
4. Redirection avec code d'autorisation
5. Échange du code contre token d'accès

### Configuration Properties

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/compta
    username: postgres
    password: password
  
  flyway:
    enabled: true
    schemas: oauth2
    default-schema: oauth2
    baseline-on-migrate: true

oauth2:
  issuer: http://localhost:9000
  authorization-code:
    access-token-validity: 1800  # 30 minutes
    refresh-token-validity: 86400  # 24 heures
  client-credentials:
    access-token-validity: 3600  # 1 heure
```

### Default Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |

Ces utilisateurs sont créés automatiquement par les migrations Flyway.

### Monitoring & Observability

- **Logging**: Configuration détaillée pour OAuth2 et Spring Security
- **Health Checks**: Actuator endpoint disponible
- **Metrics**: Métriques Spring Boot disponibles
- **Sentry**: Intégration pour la gestion d'erreurs

### Development Notes

#### JOOQ Code Generation
Les classes JOOQ sont générées automatiquement depuis la base de données:
- `mvn generate-sources` ou `mvn compile` génère les classes
- Package: `tn.cyberious.compta.oauth2.generated`
- Tables: `Users`, `Roles`, `UserRoles`, `Oauth2RegisteredClient`, etc.

#### Database Migrations
Les migrations Flyway sont exécutées automatiquement au démarrage:
- V1: Création de la table `oauth2_registered_client`
- V2: Création de la table `oauth2_authorization`
- V3: Création de la table `oauth2_authorization_consent`
- V4: Création des tables d'authentification utilisateurs

### Testing

```bash
# Exécuter les tests
cd oauth2-server
mvn test

# Lancer le serveur
mvn spring-boot:run
```

### Documentation

- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **OpenAPI Spec**: http://localhost:9000/v3/api-docs
- **RFC 6749**: Authorization Code Flow standard
- **RFC 6819**: Token Introspection standard
- **RFC 7009**: Token Revocation standard
