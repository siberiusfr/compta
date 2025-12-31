# notification-contracts

Module de contrats partages pour la communication asynchrone entre services COMPTA.

## Description

Ce module contient:
- **asyncapi.yaml**: Specification AsyncAPI 3.0.0 des evenements de notification
- **DTOs Java**: Classes Java pour oauth2-server (Spring Boot)
- **Types TypeScript**: Interfaces TypeScript pour notification-service (NestJS)

## Architecture

```
oauth2-server (Spring Boot)
        |
        | Publie EmailVerificationRequested
        v
   Redis/BullMQ
   (queue: email-verification)
        |
        | Consomme
        v
notification-service (NestJS)
```

## Evenements

### email-verification

Queue BullMQ pour les demandes de verification d'email.

**Payload:**
| Champ | Type | Description |
|-------|------|-------------|
| userId | UUID | Identifiant unique de l'utilisateur |
| email | string | Adresse email a verifier |
| username | string | Nom d'utilisateur |
| token | string | Token de verification |
| verificationLink | string | Lien complet de verification |
| expiresAt | ISO 8601 | Date d'expiration du token |

## Utilisation

### Java (oauth2-server)

```java
// Le module est une dependance Maven
<dependency>
    <groupId>tn.cyberious.compta</groupId>
    <artifactId>notification-contracts</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### TypeScript (notification-service)

```typescript
// Importer les types
import { EmailVerificationPayload } from '@compta/notification-contracts';
```

## Build

### Maven (Java)
```bash
mvn clean install
```

### NPM (TypeScript)
```bash
npm install
npm run build
```
