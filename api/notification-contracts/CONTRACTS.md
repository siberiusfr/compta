# COMPTA Notification Contracts

Documentation des contrats de communication asynchrone entre `oauth2-server` (Spring Boot) et `notification-service` (NestJS) via Redis/BullMQ.

## Source de Verite: Schemas Zod

Les contrats sont definis en TypeScript avec **Zod** dans `src/email-contracts.ts`. Ce fichier est la source de verite unique pour:
- **notification-service** (NestJS): Import direct des schemas Zod pour validation runtime
- **oauth2-server** (Spring Boot): Generation de classes Java via JSON Schema

```
notification-contracts/
├── src/
│   ├── email-contracts.ts        # Source de verite (Zod schemas)
│   ├── generate-json-schemas.ts  # Script de generation JSON Schema
│   └── index.ts                  # Re-exports publics
├── scripts/
│   └── generate-java.sh          # Script de generation Java
├── generated/
│   └── json-schemas/             # JSON Schemas generes
├── package.json                  # TypeScript/Zod dependencies
└── pom.xml                       # Maven avec jsonschema2pojo
```

## Workflow de Generation

### 1. Modifier les contrats
Editer `src/email-contracts.ts` pour ajouter/modifier des schemas.

### 2. Generer les JSON Schemas
```bash
cd notification-contracts
npm run generate:schemas
```

### 3. Generer les classes Java
```bash
npm run generate:java
# ou
mvn generate-sources
```

### 4. Importer dans les projets

**notification-service (TypeScript)**:
```typescript
import {
  QueueNames,
  SendVerificationEmailJob,
  safeParseSendVerificationEmailJob,
} from '@compta/notification-contracts';
```

**oauth2-server (Java)** - via dependency Maven:
```xml
<dependency>
  <groupId>tn.cyberious.compta</groupId>
  <artifactId>notification-contracts</artifactId>
</dependency>
```

---

## Architecture

```
oauth2-server (Spring Boot)
    │
    ├── EmailVerificationQueuePublisher
    │   └── Queue: email-verification
    │
    └── PasswordResetQueuePublisher
        └── Queue: password-reset
                │
                ▼
          Redis/BullMQ
          (localhost:6379)
                │
                ▼
      notification-service (NestJS)
                │
        ├── EmailVerificationProcessor
        │   └── Template: email-verification.mjml
        │
        └── PasswordResetProcessor
            └── Template: password-reset.mjml
                │
                ▼
            SMTP Server
```

## Queues BullMQ

| Queue | Producer | Consumer | TTL Token |
|-------|----------|----------|-----------|
| `email-verification` | oauth2-server | notification-service | 24 heures |
| `password-reset` | oauth2-server | notification-service | 1 heure |

### Constantes TypeScript

```typescript
import { QueueNames } from '@compta/notification-contracts';

QueueNames.EMAIL_VERIFICATION  // 'email-verification'
QueueNames.PASSWORD_RESET      // 'password-reset'
```

### Format Redis

Les queues utilisent le prefixe `bull:` dans Redis:
- `bull:email-verification:*` - Jobs de verification d'email
- `bull:password-reset:*` - Jobs de reset de mot de passe

---

## Schemas Zod

### SendVerificationEmailJob

Job de demande d'envoi d'email de verification.

```typescript
import { z } from 'zod';

export const SendVerificationEmailJobSchema = z.object({
  userId: z.string().uuid(),
  email: z.string().email(),
  username: z.string().min(1).max(255),
  token: z.string().min(32).max(64),
  verificationLink: z.string().url(),
  expiresAt: z.string().datetime(),
  locale: z.enum(['fr', 'en', 'ar']).default('fr'),
});

export type SendVerificationEmailJob = z.infer<typeof SendVerificationEmailJobSchema>;
```

**Exemple de payload**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "utilisateur@example.com",
  "username": "jean.dupont",
  "token": "abc123def456ghi789jkl012mno345pqr",
  "verificationLink": "https://app.compta.tn/verify-email?token=abc123def456ghi789jkl012mno345pqr",
  "expiresAt": "2025-01-02T12:00:00Z",
  "locale": "fr"
}
```

| Champ | Type | Description | Contraintes |
|-------|------|-------------|-------------|
| `userId` | UUID | Identifiant unique de l'utilisateur | Required |
| `email` | string | Adresse email a verifier | Required, format email |
| `username` | string | Nom d'utilisateur | Required, 1-255 chars |
| `token` | string | Token de verification unique | Required, 32-64 chars |
| `verificationLink` | string | URL complete de verification | Required, format URI |
| `expiresAt` | ISO 8601 | Date d'expiration (24h apres creation) | Required |
| `locale` | enum | Langue du template | Optional, default: 'fr' |

### SendPasswordResetEmailJob

Job de demande d'envoi d'email de reset de mot de passe.

```typescript
export const SendPasswordResetEmailJobSchema = z.object({
  userId: z.string().uuid(),
  email: z.string().email(),
  username: z.string().min(1).max(255),
  token: z.string().min(32).max(64),
  resetLink: z.string().url(),
  expiresAt: z.string().datetime(),
  locale: z.enum(['fr', 'en', 'ar']).default('fr'),
});

export type SendPasswordResetEmailJob = z.infer<typeof SendPasswordResetEmailJobSchema>;
```

**Exemple de payload**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "utilisateur@example.com",
  "username": "jean.dupont",
  "token": "xyz789abc456def123ghi012jkl345mno",
  "resetLink": "https://app.compta.tn/reset-password?token=xyz789abc456def123ghi012jkl345mno",
  "expiresAt": "2025-01-01T13:00:00Z",
  "locale": "fr"
}
```

---

## Events de Retour

Schemas pour les events de retour notification-service -> oauth2-server (optionnel).

### EmailVerificationSentEvent

```typescript
export const EmailVerificationSentEventSchema = z.object({
  jobId: z.string().min(1),
  userId: z.string().uuid(),
  email: z.string().email(),
  messageId: z.string().optional(),
  sentAt: z.string().datetime(),
});
```

### EmailVerificationFailedEvent

```typescript
export const EmailVerificationFailedEventSchema = z.object({
  jobId: z.string().min(1),
  userId: z.string().uuid(),
  email: z.string().email(),
  errorCode: z.string().optional(),
  errorMessage: z.string(),
  attemptsMade: z.number().int().min(1),
  failedAt: z.string().datetime(),
});
```

### PasswordResetSentEvent / PasswordResetFailedEvent

Schemas similaires pour le password reset.

---

## Validation Runtime

### TypeScript (notification-service)

```typescript
import {
  safeParseSendVerificationEmailJob,
  validateSendVerificationEmailJob,
} from '@compta/notification-contracts';

// Safe parse (retourne success/error)
const result = safeParseSendVerificationEmailJob(job.data);
if (!result.success) {
  console.error('Invalid payload:', result.error.message);
}

// Validation stricte (throw si invalide)
const validData = validateSendVerificationEmailJob(job.data);
```

### Java (oauth2-server)

Les classes Java generees incluent les annotations Jakarta Validation:

```java
import tn.cyberious.compta.contracts.notification.SendVerificationEmailJob;
import jakarta.validation.constraints.*;

public class SendVerificationEmailJob {
    @NotNull
    private String userId;

    @Email
    @NotNull
    private String email;

    @Size(min = 1, max = 255)
    @NotNull
    private String username;

    // ...
}
```

---

## Configuration BullMQ

### Options des Jobs

```typescript
export const DefaultJobOptions = {
  attempts: 3,
  removeOnComplete: true,
  removeOnFail: false,
  backoff: {
    type: 'exponential',
    delay: 1000,
  },
};
```

| Option | Valeur | Description |
|--------|--------|-------------|
| `attempts` | 3 | Nombre maximum de tentatives |
| `removeOnComplete` | true | Supprime le job apres succes |
| `removeOnFail` | false | Conserve le job en echec pour analyse |
| `backoff.type` | exponential | Delai exponentiel entre retries |
| `backoff.delay` | 1000ms | Delai initial (1s, 2s, 4s...) |

---

## Templates Email

### email-verification.mjml

**Fichier**: `notification-service/src/templates/email-verification.mjml`

**Variables disponibles**:
- `{{username}}` - Nom d'utilisateur
- `{{verificationLink}}` - Lien de verification
- `{{expiresAt}}` - Date d'expiration formatee

**Sujet**: `Verification de votre adresse email - COMPTA`

### password-reset.mjml

**Fichier**: `notification-service/src/templates/password-reset.mjml`

**Variables disponibles**:
- `{{username}}` - Nom d'utilisateur
- `{{resetLink}}` - Lien de reinitialisation
- `{{expiresAt}}` - Date d'expiration formatee

**Sujet**: `Reinitialisation de votre mot de passe - COMPTA`

---

## Monitoring

### BullBoard UI

**URL**: `http://localhost:3000/queues`

Permet de:
- Visualiser les jobs en attente, actifs, completes, echoues
- Inspecter le payload des jobs
- Retenter les jobs echoues
- Supprimer les jobs

### Queues disponibles

1. `email-verification` - Verification d'email
2. `password-reset` - Reset de mot de passe
3. `mail_queue` - Queue generique (legacy)

---

## Gestion des Erreurs

### Cote Producer (oauth2-server)

Si Redis est indisponible:
- `RuntimeException` avec message "Redis unavailable"
- Le token est deja stocke en base de donnees
- L'utilisateur peut redemander la verification/reset

### Cote Consumer (notification-service)

En cas d'echec d'envoi SMTP:
1. Le job est retente jusqu'a 3 fois (backoff exponentiel)
2. Apres 3 echecs, le job reste dans la queue "failed"
3. Les erreurs sont loggees avec stack trace complete

---

## Flux de Donnees

### Email Verification

```
1. POST /api/users/email/verify (oauth2-server)
   │
2. EmailVerificationService.initiateEmailVerification()
   │
3. INSERT INTO email_verification_tokens
   │
4. EmailVerificationQueuePublisher.publishEmailVerificationRequested()
   │
5. Redis LPUSH bull:email-verification:wait {jobId}
   │
6. EmailVerificationProcessor.process() (notification-service)
   │
7. Validation Zod du payload
   │
8. Charger email-verification.mjml
   │
9. Compiler MJML → HTML
   │
10. MailerService.sendMail()
   │
11. Email envoye via SMTP
```

### Password Reset

```
1. POST /api/users/password/reset (oauth2-server)
   │
2. PasswordResetService.initiatePasswordReset()
   │
3. INSERT INTO password_reset_tokens
   │
4. PasswordResetQueuePublisher.publishPasswordResetRequested()
   │
5. Redis LPUSH bull:password-reset:wait {jobId}
   │
6. PasswordResetProcessor.process() (notification-service)
   │
7. Validation Zod du payload
   │
8. Charger password-reset.mjml
   │
9. Compiler MJML → HTML
   │
10. MailerService.sendMail()
   │
11. Email envoye via SMTP
```

---

## Exports Disponibles

```typescript
import {
  // Constantes
  QueueNames,
  JobNames,
  DefaultJobOptions,

  // Schemas Zod
  SendVerificationEmailJobSchema,
  SendPasswordResetEmailJobSchema,
  EmailVerificationSentEventSchema,
  EmailVerificationFailedEventSchema,
  PasswordResetSentEventSchema,
  PasswordResetFailedEventSchema,

  // Types inferes
  type SendVerificationEmailJob,
  type SendPasswordResetEmailJob,
  type EmailVerificationSentEvent,
  type EmailVerificationFailedEvent,
  type PasswordResetSentEvent,
  type PasswordResetFailedEvent,

  // Fonctions de validation
  validateSendVerificationEmailJob,
  validateSendPasswordResetEmailJob,
  safeParseSendVerificationEmailJob,
  safeParseSendPasswordResetEmailJob,

  // Groupes de schemas
  JobSchemas,
  EventSchemas,
} from '@compta/notification-contracts';
```
