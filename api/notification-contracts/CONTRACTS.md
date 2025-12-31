# COMPTA Notification Contracts

Documentation des contrats de communication asynchrone entre `oauth2-server` (Spring Boot) et `notification-service` (NestJS) via Redis/BullMQ.

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

### Format Redis

Les queues utilisent le prefixe `bull:` dans Redis:
- `bull:email-verification:*` - Jobs de verification d'email
- `bull:password-reset:*` - Jobs de reset de mot de passe

---

## Queue: email-verification

### Description
Demande de verification d'adresse email lors de l'inscription d'un utilisateur.

### Producer (oauth2-server)

**Classe**: `EmailVerificationQueuePublisher.java`

```java
@Service
public class EmailVerificationQueuePublisher {
    public String publishEmailVerificationRequested(EmailVerificationMessage message);
}
```

**Declencheur**: `EmailVerificationService.initiateEmailVerification()`

### Consumer (notification-service)

**Classe**: `email-verification.processor.ts`

```typescript
@Processor('email-verification')
export class EmailVerificationProcessor extends WorkerHost {
    async process(job: Job<EmailVerificationPayload>): Promise<any>;
}
```

### Payload

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "utilisateur@example.com",
  "username": "jean.dupont",
  "token": "abc123def456ghi789jkl012mno345pqr",
  "verificationLink": "https://app.compta.tn/verify-email?token=abc123def456ghi789jkl012mno345pqr",
  "expiresAt": "2025-01-02T12:00:00"
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

### Template Email

**Fichier**: `src/templates/email-verification.mjml`

**Variables disponibles**:
- `{{username}}` - Nom d'utilisateur
- `{{verificationLink}}` - Lien de verification
- `{{expiresAt}}` - Date d'expiration formatee

**Sujet**: `Verification de votre adresse email - COMPTA`

---

## Queue: password-reset

### Description
Demande de reinitialisation de mot de passe.

### Producer (oauth2-server)

**Classe**: `PasswordResetQueuePublisher.java`

```java
@Service
public class PasswordResetQueuePublisher {
    public String publishPasswordResetRequested(PasswordResetMessage message);
}
```

**Declencheur**: `PasswordResetService.initiatePasswordReset()`

### Consumer (notification-service)

**Classe**: `password-reset.processor.ts`

```typescript
@Processor('password-reset')
export class PasswordResetProcessor extends WorkerHost {
    async process(job: Job<PasswordResetPayload>): Promise<any>;
}
```

### Payload

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "utilisateur@example.com",
  "username": "jean.dupont",
  "token": "xyz789abc456def123ghi012jkl345mno",
  "resetLink": "https://app.compta.tn/reset-password?token=xyz789abc456def123ghi012jkl345mno",
  "expiresAt": "2025-01-01T13:00:00"
}
```

| Champ | Type | Description | Contraintes |
|-------|------|-------------|-------------|
| `userId` | UUID | Identifiant unique de l'utilisateur | Required |
| `email` | string | Adresse email de l'utilisateur | Required, format email |
| `username` | string | Nom d'utilisateur | Required, 1-255 chars |
| `token` | string | Token de reset unique | Required, 32-64 chars |
| `resetLink` | string | URL complete de reset | Required, format URI |
| `expiresAt` | ISO 8601 | Date d'expiration (1h apres creation) | Required |

### Template Email

**Fichier**: `src/templates/password-reset.mjml`

**Variables disponibles**:
- `{{username}}` - Nom d'utilisateur
- `{{resetLink}}` - Lien de reinitialisation
- `{{expiresAt}}` - Date d'expiration formatee

**Sujet**: `Reinitialisation de votre mot de passe - COMPTA`

---

## Configuration BullMQ

### Options des Jobs

Tous les jobs sont configures avec les options suivantes:

```json
{
  "attempts": 3,
  "removeOnComplete": true,
  "removeOnFail": false,
  "backoff": {
    "type": "exponential",
    "delay": 1000
  }
}
```

| Option | Valeur | Description |
|--------|--------|-------------|
| `attempts` | 3 | Nombre maximum de tentatives |
| `removeOnComplete` | true | Supprime le job apres succes |
| `removeOnFail` | false | Conserve le job en echec pour analyse |
| `backoff.type` | exponential | Delai exponentiel entre retries |
| `backoff.delay` | 1000ms | Delai initial (1s, 2s, 4s...) |

### Structure Redis du Job

Chaque job est stocke dans Redis avec la structure suivante:

```
bull:{queue-name}:{job-id}
├── data      → JSON du payload complet
├── name      → Nom du job (ex: "email-verification-requested")
├── opts      → Options JSON
├── timestamp → Timestamp de creation (ms)
├── attemptsMade → Nombre de tentatives effectuees
├── processedOn  → Timestamp de debut de traitement
└── finishedOn   → Timestamp de fin de traitement
```

La liste d'attente:
```
bull:{queue-name}:wait → Liste des job IDs en attente
```

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

## Configuration

### oauth2-server (application.yml)

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

notification:
  queue:
    enabled: ${NOTIFICATION_QUEUE_ENABLED:true}
```

### notification-service (app.module.ts)

```typescript
BullModule.forRoot({
  connection: {
    host: 'localhost',
    port: 6379,
  },
}),
BullModule.registerQueue({ name: 'email-verification' }),
BullModule.registerQueue({ name: 'password-reset' }),
```

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

## Types TypeScript

### EmailVerificationPayload

```typescript
interface EmailVerificationPayload {
  userId: string;
  email: string;
  username: string;
  token: string;
  verificationLink: string;
  expiresAt: string;
}
```

### PasswordResetPayload

```typescript
interface PasswordResetPayload {
  userId: string;
  email: string;
  username: string;
  token: string;
  resetLink: string;
  expiresAt: string;
}
```

### Constantes

```typescript
const NotificationQueues = {
  EMAIL_VERIFICATION: 'email-verification',
  PASSWORD_RESET: 'password-reset',
  BULL_PREFIX: 'bull',
} as const;

const JobNames = {
  EMAIL_VERIFICATION_REQUESTED: 'email-verification-requested',
  PASSWORD_RESET_REQUESTED: 'password-reset-requested',
} as const;
```

---

## Classes Java

### EmailVerificationMessage

```java
@Data
@Builder
public class EmailVerificationMessage {
    private String userId;
    private String email;
    private String username;
    private String token;
    private String verificationLink;
    private LocalDateTime expiresAt;
}
```

### PasswordResetMessage

```java
@Data
@Builder
public class PasswordResetMessage {
    private String userId;
    private String email;
    private String username;
    private String token;
    private String resetLink;
    private LocalDateTime expiresAt;
}
```

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
7. Charger email-verification.mjml
   │
8. Compiler MJML → HTML
   │
9. MailerService.sendMail()
   │
10. Email envoye via SMTP
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
7. Charger password-reset.mjml
   │
8. Compiler MJML → HTML
   │
9. MailerService.sendMail()
   │
10. Email envoye via SMTP
```

---

## Specification AsyncAPI

Voir `asyncapi.yaml` pour la specification complete au format AsyncAPI 3.0.0.
