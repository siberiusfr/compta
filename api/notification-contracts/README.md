# @compta/notification-contracts

Module de contrats partages pour la communication asynchrone entre services COMPTA via Redis/BullMQ.

## Vue d'ensemble

Ce module definit les **contrats de communication** entre:
- **oauth2-server** (Spring Boot) - Producer des jobs
- **notification-service** (NestJS) - Consumer des jobs

### Source de verite: Zod

Les schemas sont definis en **TypeScript avec Zod** et sont ensuite:
- Importes directement dans notification-service (NestJS)
- Convertis en JSON Schema puis en classes Java pour oauth2-server

```
src/email-contracts.ts (Zod)
        |
        +---> dist/ (TypeScript compile)
        |         \--> notification-service (import direct)
        |
        +---> generated/json-schemas/ (JSON Schema)
                      \--> target/generated-sources/ (Java classes)
                                \--> oauth2-server (Maven dependency)
```

## Structure du projet

```
notification-contracts/
├── src/
│   ├── email-contracts.ts        # Schemas Zod (SOURCE DE VERITE)
│   ├── generate-json-schemas.ts  # Script de generation
│   └── index.ts                  # Re-exports publics
├── scripts/
│   └── generate-java.sh          # Script generation Java
├── generated/
│   └── json-schemas/             # JSON Schemas generes (gitignore)
├── dist/                         # TypeScript compile (gitignore)
├── target/                       # Classes Java generees (gitignore)
├── package.json                  # Config npm + Zod
├── pom.xml                       # Config Maven + jsonschema2pojo
├── tsconfig.json                 # Config TypeScript
├── CONTRACTS.md                  # Documentation detaillee des contrats
└── AGENTS.md                     # Instructions pour agents IA
```

## Installation

### Prerequisites

- Node.js 18+
- Maven 3.8+
- Java 21+

### Setup

```bash
cd notification-contracts

# Installer les dependances npm
npm install

# Build complet (TypeScript + JSON Schemas)
npm run build:all

# Generer les classes Java
mvn generate-sources
```

## Utilisation

### TypeScript (notification-service)

```typescript
import {
  // Constantes
  QueueNames,
  JobNames,
  DefaultJobOptions,

  // Types
  SendVerificationEmailJob,
  SendPasswordResetEmailJob,

  // Validation
  safeParseSendVerificationEmailJob,
  validateSendPasswordResetEmailJob,
} from '@compta/notification-contracts';

// Exemple: Validation d'un job
const result = safeParseSendVerificationEmailJob(job.data);
if (!result.success) {
  console.error('Invalid payload:', result.error.message);
  return;
}
const { email, username, verificationLink } = result.data;
```

### Java (oauth2-server)

Ajouter la dependance Maven:

```xml
<dependency>
    <groupId>tn.cyberious.compta</groupId>
    <artifactId>notification-contracts</artifactId>
    <version>${project.version}</version>
</dependency>
```

Utiliser les classes generees:

```java
import tn.cyberious.compta.contracts.notification.SendVerificationEmailJob;

SendVerificationEmailJob job = new SendVerificationEmailJob()
    .withUserId(UUID.fromString(userId))
    .withEmail(email)
    .withUsername(username)
    .withToken(token)
    .withVerificationLink(URI.create(link))
    .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
    .withLocale(SendVerificationEmailJob.Locale.FR);
```

## Schemas disponibles

### Jobs (oauth2-server -> notification-service)

| Schema | Queue | Description |
|--------|-------|-------------|
| `SendVerificationEmailJob` | `email-verification` | Demande d'envoi d'email de verification |
| `SendPasswordResetEmailJob` | `password-reset` | Demande d'envoi d'email de reset |

### Events (notification-service -> oauth2-server)

| Schema | Description |
|--------|-------------|
| `EmailVerificationSentEvent` | Email de verification envoye |
| `EmailVerificationFailedEvent` | Echec d'envoi email verification |
| `PasswordResetSentEvent` | Email de reset envoye |
| `PasswordResetFailedEvent` | Echec d'envoi email reset |

### Configuration

| Schema | Description |
|--------|-------------|
| `BullMQJobOptions` | Options de configuration des jobs |

## Scripts npm

| Script | Description |
|--------|-------------|
| `npm run build` | Compile TypeScript vers dist/ |
| `npm run build:all` | Build + generation JSON Schemas |
| `npm run generate:schemas` | Genere JSON Schemas depuis Zod |
| `npm run generate:java` | Genere classes Java (via Maven) |
| `npm run clean` | Supprime dist/ |
| `npm run clean:all` | Supprime dist/ et generated/ |

## Workflow de modification

### 1. Modifier le schema Zod

Editer `src/email-contracts.ts`:

```typescript
export const MonNouveauJobSchema = z.object({
  userId: z.string().uuid(),
  // ...
});

export type MonNouveauJob = z.infer<typeof MonNouveauJobSchema>;
```

### 2. Exporter dans index.ts

```typescript
export {
  MonNouveauJobSchema,
  type MonNouveauJob,
} from './email-contracts';
```

### 3. Ajouter au generateur

Editer `src/generate-json-schemas.ts`:

```typescript
const schemas = [
  // ...
  {
    name: 'MonNouveauJob',
    schema: MonNouveauJobSchema,
    description: 'Description du job',
  },
];
```

### 4. Regenerer

```bash
npm run build:all
mvn clean compile
```

### 5. Mettre a jour la documentation

Editer `CONTRACTS.md` avec les nouveaux schemas.

## Architecture des queues

```
oauth2-server                    notification-service
     |                                   |
     | EmailVerificationQueuePublisher   |
     +---------------------------------->|
     |    Queue: email-verification      | EmailVerificationProcessor
     |                                   |
     | PasswordResetQueuePublisher       |
     +---------------------------------->|
     |    Queue: password-reset          | PasswordResetProcessor
     |                                   |
                    Redis/BullMQ
```

## Configuration BullMQ

```typescript
const DefaultJobOptions = {
  attempts: 3,           // 3 tentatives max
  removeOnComplete: true,
  removeOnFail: false,   // Conserver pour debug
  backoff: {
    type: 'exponential',
    delay: 1000,         // 1s, 2s, 4s...
  },
};
```

## Documentation

- [CONTRACTS.md](./CONTRACTS.md) - Documentation detaillee des contrats
- [AGENTS.md](./AGENTS.md) - Instructions pour agents IA

## Liens

- oauth2-server: `../oauth2-server/`
- notification-service: `../notification-service/`
