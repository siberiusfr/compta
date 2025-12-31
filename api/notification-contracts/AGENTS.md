# AGENTS.md

Instructions pour les agents IA (Claude Code, GitHub Copilot, Cursor, etc.) travaillant sur ce module.

## Vue d'ensemble du module

`notification-contracts` est le module de **contrats partages** pour la communication asynchrone entre services COMPTA. Il definit les schemas de messages echanges via Redis/BullMQ.

### Concept cle: Source de verite unique

Les schemas **Zod** dans `src/email-contracts.ts` sont la **source de verite unique**. Tout le reste est genere:

```
email-contracts.ts (Zod) --> JSON Schema --> Classes Java
                        \--> Types TypeScript (inferes)
```

**NE JAMAIS** modifier directement:
- Les fichiers dans `generated/`
- Les fichiers dans `target/`
- Les fichiers dans `dist/`

---

## REGLE OBLIGATOIRE: Format d'enveloppe standard

**TOUS les messages doivent suivre le format d'enveloppe standard:**

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "EmailVerificationRequested",
  "eventVersion": 1,
  "occurredAt": "2025-01-01T12:00:00Z",
  "producer": "oauth2-server",
  "payload": {
    // Donnees specifiques au message
  }
}
```

### Champs de l'enveloppe

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `eventId` | UUID v4 | Identifiant unique de l'evenement | Oui |
| `eventType` | string | Nom du type d'evenement (PascalCase) | Oui |
| `eventVersion` | integer | Version du schema (pour evolution) | Oui (default: 1) |
| `occurredAt` | ISO 8601 | Timestamp de creation de l'evenement | Oui |
| `producer` | string | Service producteur (`oauth2-server` ou `notification-service`) | Oui |
| `payload` | object | Donnees specifiques a l'evenement | Oui |

### Utiliser EventEnvelopeSchema

Pour creer un nouveau message, utiliser la fonction `EventEnvelopeSchema`:

```typescript
import { z } from 'zod';
import { EventEnvelopeSchema, Producers, EventTypes } from './email-contracts';

// 1. Definir le schema du payload
export const MonNouveauPayloadSchema = z.object({
  userId: z.string().uuid(),
  email: z.string().email(),
  // ... autres champs specifiques
});

// 2. Ajouter le type d'evenement
export const EventTypes = {
  // ... types existants
  MON_NOUVEAU_EVENT: 'MonNouveauEvent',
} as const;

// 3. Creer le schema complet avec enveloppe
export const MonNouveauEventSchema = EventEnvelopeSchema(
  EventTypes.MON_NOUVEAU_EVENT,
  MonNouveauPayloadSchema,
);

export type MonNouveauEvent = z.infer<typeof MonNouveauEventSchema>;
```

### Factory helper

Utiliser `createEventEnvelope` pour creer des messages:

```typescript
import { createEventEnvelope, EventTypes, Producers } from '@compta/notification-contracts';

const message = createEventEnvelope(
  EventTypes.EMAIL_VERIFICATION_REQUESTED,
  Producers.OAUTH2_SERVER,
  {
    userId: 'xxx',
    email: 'user@example.com',
    // ... payload
  },
);
// Resultat:
// {
//   eventId: "auto-generated-uuid",
//   eventType: "EmailVerificationRequested",
//   eventVersion: 1,
//   occurredAt: "2025-01-01T12:00:00Z",
//   producer: "oauth2-server",
//   payload: { userId: "xxx", email: "user@example.com", ... }
// }
```

---

## Architecture du projet

```
notification-contracts/
├── src/
│   ├── email-contracts.ts        # SOURCE DE VERITE - Schemas Zod
│   ├── generate-json-schemas.ts  # Script de generation JSON Schema
│   └── index.ts                  # Re-exports publics
├── scripts/
│   └── generate-java.sh          # Script generation Java (bash)
├── generated/                    # GENERE - Ne pas modifier
│   └── json-schemas/             # JSON Schemas pour Java
├── dist/                         # GENERE - TypeScript compile
├── target/                       # GENERE - Classes Java
├── package.json                  # Dependencies npm
├── pom.xml                       # Config Maven + jsonschema2pojo
└── tsconfig.json                 # Config TypeScript
```

---

## Taches courantes

### Ajouter un nouveau type de message

1. **Definir le payload** dans `src/email-contracts.ts`:

```typescript
/**
 * Payload pour mon nouveau message
 */
export const MonNouveauPayloadSchema = z.object({
  /** Description du champ */
  userId: z.string().uuid(),
  /** Description du champ */
  email: z.string().email(),
  // ... autres champs
});

export type MonNouveauPayload = z.infer<typeof MonNouveauPayloadSchema>;
```

2. **Ajouter le type d'evenement** dans `EventTypes`:

```typescript
export const EventTypes = {
  // ... existants
  MON_NOUVEAU_EVENT: 'MonNouveauEvent',
} as const;
```

3. **Creer le schema complet** (enveloppe + payload):

```typescript
/**
 * Message complet: Description du message
 * Producer: oauth2-server ou notification-service
 * Consumer: le service consommateur
 * Queue: nom-de-la-queue (si applicable)
 */
export const MonNouveauEventSchema = EventEnvelopeSchema(
  EventTypes.MON_NOUVEAU_EVENT,
  MonNouveauPayloadSchema,
);

export type MonNouveauEvent = z.infer<typeof MonNouveauEventSchema>;
```

4. **Ajouter les fonctions de validation**:

```typescript
export function validateMonNouveauEvent(data: unknown): MonNouveauEvent {
  return MonNouveauEventSchema.parse(data);
}

export function safeParseMonNouveauEvent(data: unknown) {
  return MonNouveauEventSchema.safeParse(data);
}
```

5. **Exporter dans `src/index.ts`**:

```typescript
export {
  MonNouveauPayloadSchema,
  type MonNouveauPayload,
  MonNouveauEventSchema,
  type MonNouveauEvent,
  validateMonNouveauEvent,
  safeParseMonNouveauEvent,
} from './email-contracts';
```

6. **Ajouter au generateur** dans `src/generate-json-schemas.ts`:

```typescript
// Dans messageSchemas ou payloadSchemas selon le cas
const messageSchemas = [
  // ... existants
  {
    name: 'MonNouveauEvent',
    schema: MonNouveauEventSchema,
    description: 'Message complet: Description',
  },
];
```

7. **Regenerer**:

```bash
npm run build:all
mvn clean compile
```

8. **Mettre a jour la documentation** dans `CONTRACTS.md`.

### Ajouter une nouvelle queue

1. Ajouter dans `QueueNames`:

```typescript
export const QueueNames = {
  // ... existantes
  MA_NOUVELLE_QUEUE: 'ma-nouvelle-queue',
} as const;
```

2. Ajouter dans `JobNames` si necessaire:

```typescript
export const JobNames = {
  // ... existants
  MON_NOUVEAU_JOB: 'mon-nouveau-job',
} as const;
```

### Ajouter un nouveau producteur

1. Ajouter dans `Producers`:

```typescript
export const Producers = {
  OAUTH2_SERVER: 'oauth2-server',
  NOTIFICATION_SERVICE: 'notification-service',
  MON_NOUVEAU_SERVICE: 'mon-nouveau-service', // Nouveau
} as const;
```

2. Mettre a jour `EventEnvelopeSchema` si necessaire.

---

## Conventions de code

### Nommage

| Type | Convention | Exemple |
|------|------------|---------|
| Payload Schema | `{Name}PayloadSchema` | `SendVerificationEmailPayloadSchema` |
| Message Schema | `{EventType}Schema` | `EmailVerificationRequestedSchema` |
| Type | Sans suffixe | `EmailVerificationRequested` |
| Validation | `validate{Name}` / `safeParse{Name}` | `validateEmailVerificationRequested` |
| Event Type | PascalCase | `EmailVerificationRequested` |
| Queue Name | kebab-case | `email-verification` |
| Producer | kebab-case | `oauth2-server` |

### Documentation JSDoc

```typescript
/**
 * Message complet: Description courte
 * Producer: nom-du-service
 * Consumer: nom-du-service (optionnel si non applicable)
 * Queue: nom-de-la-queue
 */
export const MonMessageSchema = EventEnvelopeSchema(...);
```

### Constantes

```typescript
// Utiliser "as const" pour les constantes
export const MesConstantes = {
  VALEUR_1: 'valeur-1',
  VALEUR_2: 'valeur-2',
} as const;

// Type derive
export type MaConstante = (typeof MesConstantes)[keyof typeof MesConstantes];
```

---

## Commandes utiles

```bash
# Build TypeScript + JSON Schemas
npm run build:all

# Generer uniquement les JSON Schemas
npm run generate:schemas

# Compiler Maven (genere les classes Java)
mvn clean compile

# Verifier les types TypeScript
npx tsc --noEmit

# Lister les exports du module
node -e "console.log(Object.keys(require('./dist')))"
```

---

## Integration avec les autres services

### oauth2-server (Java/Spring Boot)

Le service utilise les classes Java generees:

```java
import tn.cyberious.compta.contracts.notification.EmailVerificationRequested;
import tn.cyberious.compta.contracts.notification.SendVerificationEmailPayload;

// Creer le message avec enveloppe
EmailVerificationRequested message = new EmailVerificationRequested()
    .withEventId(UUID.randomUUID().toString())
    .withEventType("EmailVerificationRequested")
    .withEventVersion(1)
    .withOccurredAt(Instant.now().toString())
    .withProducer("oauth2-server")
    .withPayload(new SendVerificationEmailPayload()
        .withUserId(userId)
        .withEmail(email)
        .withUsername(username)
        .withToken(token)
        .withVerificationLink(link)
        .withExpiresAt(expiresAt)
        .withLocale(SendVerificationEmailPayload.Locale.FR));

// Serialisation Jackson
String json = objectMapper.writeValueAsString(message);
```

### notification-service (TypeScript/NestJS)

Le service importe directement les types Zod:

```typescript
import {
  QueueNames,
  EmailVerificationRequested,
  safeParseEmailVerificationRequested,
} from '@compta/notification-contracts';

@Processor(QueueNames.EMAIL_VERIFICATION)
export class EmailVerificationProcessor extends WorkerHost {
  async process(job: Job<EmailVerificationRequested>) {
    // Valider le message complet (enveloppe + payload)
    const result = safeParseEmailVerificationRequested(job.data);
    if (!result.success) {
      throw new Error(`Invalid message: ${result.error.message}`);
    }

    // Extraire le payload
    const { eventId, eventType, occurredAt, producer, payload } = result.data;
    const { email, username, verificationLink } = payload;

    this.logger.log(`Processing ${eventType} (${eventId}) from ${producer}`);
    // ...
  }
}
```

---

## Erreurs courantes

### "Cannot find module '@compta/notification-contracts'"

Le module n'est pas build ou pas installe:

```bash
cd notification-contracts
npm run build:all

cd ../notification-service
npm install
```

### "Cannot resolve symbol" dans Java

Les classes Java ne sont pas generees:

```bash
cd notification-contracts
npm run generate:schemas  # Generer JSON Schemas
mvn clean compile         # Generer classes Java
```

### Types out of sync entre TypeScript et Java

Toujours regenerer les deux apres modification:

```bash
npm run build:all && mvn clean compile
```

### Message sans enveloppe

**ERREUR**: Ne pas envoyer de message sans enveloppe!

```typescript
// INCORRECT - payload seul
const message = {
  userId: 'xxx',
  email: 'user@example.com',
};

// CORRECT - utiliser createEventEnvelope
const message = createEventEnvelope(
  EventTypes.EMAIL_VERIFICATION_REQUESTED,
  Producers.OAUTH2_SERVER,
  { userId: 'xxx', email: 'user@example.com', ... },
);
```

---

## Points d'attention

1. **Format d'enveloppe obligatoire**: TOUS les messages doivent utiliser `EventEnvelopeSchema`.

2. **eventVersion**: Incrementer lors de changements breaking dans le payload.

3. **eventId**: Generer un UUID v4 unique pour chaque message.

4. **occurredAt**: Toujours en format ISO 8601 UTC.

5. **producer**: Utiliser les constantes `Producers.*`.

6. **Retrocompatibilite**: Les champs optionnels dans le payload sont retrocompatibles, les champs requis non.

7. **Validation**: La validation Zod se fait cote consumer. Le producer fait confiance aux types.

8. **Serialisation**: Les dates sont en format ISO 8601 (`z.string().datetime()`).

---

## Checklist nouveau message

- [ ] Payload schema defini avec `z.object({...})`
- [ ] Type d'evenement ajoute dans `EventTypes`
- [ ] Message schema cree avec `EventEnvelopeSchema()`
- [ ] Fonctions `validate*` et `safeParse*` ajoutees
- [ ] Exports ajoutes dans `index.ts`
- [ ] Schema ajoute dans `generate-json-schemas.ts`
- [ ] `npm run build:all` execute
- [ ] `mvn clean compile` execute
- [ ] Documentation mise a jour dans `CONTRACTS.md`
- [ ] Tests ajoutes si necessaire

---

## Ressources

- [Zod Documentation](https://zod.dev)
- [jsonschema2pojo](https://www.jsonschema2pojo.org/)
- [BullMQ Documentation](https://docs.bullmq.io/)
- [CloudEvents Specification](https://cloudevents.io/) (inspiration pour le format d'enveloppe)
