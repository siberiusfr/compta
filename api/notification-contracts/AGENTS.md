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

## Taches courantes

### Ajouter un nouveau type de notification

1. **Definir le schema Zod** dans `src/email-contracts.ts`:

```typescript
/**
 * Job: Description du job
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: nom-de-la-queue
 */
export const MonNouveauJobSchema = z.object({
  userId: z.string().uuid(),
  email: z.string().email(),
  // ... autres champs
});

export type MonNouveauJob = z.infer<typeof MonNouveauJobSchema>;

// Ajouter fonction de validation
export function validateMonNouveauJob(data: unknown): MonNouveauJob {
  return MonNouveauJobSchema.parse(data);
}

export function safeParseMonNouveauJob(data: unknown) {
  return MonNouveauJobSchema.safeParse(data);
}
```

2. **Exporter dans `src/index.ts`**:

```typescript
export {
  MonNouveauJobSchema,
  type MonNouveauJob,
  validateMonNouveauJob,
  safeParseMonNouveauJob,
} from './email-contracts';
```

3. **Ajouter au generateur** dans `src/generate-json-schemas.ts`:

```typescript
const schemas = [
  // ... schemas existants
  {
    name: 'MonNouveauJob',
    schema: MonNouveauJobSchema,
    description: 'Description du job',
  },
];
```

4. **Ajouter la queue** dans `email-contracts.ts`:

```typescript
export const QueueNames = {
  // ... queues existantes
  MON_NOUVEAU: 'mon-nouveau',
} as const;
```

5. **Regenerer les fichiers**:

```bash
npm run build:all
mvn clean compile
```

6. **Mettre a jour la documentation** dans `CONTRACTS.md`.

### Modifier un schema existant

1. Modifier le schema dans `src/email-contracts.ts`
2. Regenerer: `npm run build:all && mvn clean compile`
3. Mettre a jour `CONTRACTS.md`
4. Verifier les impacts dans:
   - `oauth2-server/` (producer)
   - `notification-service/` (consumer)

### Ajouter un champ optionnel

```typescript
export const SendVerificationEmailJobSchema = z.object({
  // ... champs existants
  nouveauChamp: z.string().optional(), // Optionnel = retrocompatible
});
```

### Ajouter un champ requis (BREAKING CHANGE)

```typescript
export const SendVerificationEmailJobSchema = z.object({
  // ... champs existants
  nouveauChampRequis: z.string().min(1), // Requis = breaking change
});
```

**Attention**: Un champ requis est un breaking change. Il faut:
1. Mettre a jour oauth2-server pour envoyer le champ
2. Deployer oauth2-server EN PREMIER
3. Puis deployer notification-service

## Conventions de code

### Schemas Zod

```typescript
// Nommer le schema avec le suffixe "Schema"
export const MonJobSchema = z.object({...});

// Type infere sans suffixe
export type MonJob = z.infer<typeof MonJobSchema>;

// Validation avec prefixe validate/safeParse
export function validateMonJob(data: unknown): MonJob {...}
export function safeParseMonJob(data: unknown) {...}
```

### Constantes

```typescript
// Utiliser "as const" pour les constantes
export const QueueNames = {
  EMAIL_VERIFICATION: 'email-verification',
} as const;

// Type derive
export type QueueName = (typeof QueueNames)[keyof typeof QueueNames];
```

### Documentation JSDoc

```typescript
/**
 * Job: Demande d'envoi d'email de verification
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: email-verification
 */
export const SendVerificationEmailJobSchema = z.object({
  /** Identifiant unique de l'utilisateur */
  userId: z.string().uuid(),
  // ...
});
```

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

## Integration avec les autres services

### oauth2-server (Java/Spring Boot)

Le service utilise les classes Java generees:

```java
// Import
import tn.cyberious.compta.contracts.notification.SendVerificationEmailJob;

// Utilisation avec builder pattern
SendVerificationEmailJob job = new SendVerificationEmailJob()
    .withUserId(UUID.fromString(userId))
    .withEmail(email)
    .withUsername(username)
    .withToken(token)
    .withVerificationLink(URI.create(link))
    .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
    .withLocale(SendVerificationEmailJob.Locale.FR);

// Serialisation Jackson
String json = objectMapper.writeValueAsString(job);
```

### notification-service (TypeScript/NestJS)

Le service importe directement les types Zod:

```typescript
import {
  QueueNames,
  SendVerificationEmailJob,
  safeParseSendVerificationEmailJob,
} from '@compta/notification-contracts';

@Processor(QueueNames.EMAIL_VERIFICATION)
export class EmailVerificationProcessor extends WorkerHost {
  async process(job: Job<SendVerificationEmailJob>) {
    const result = safeParseSendVerificationEmailJob(job.data);
    if (!result.success) {
      throw new Error(`Invalid payload: ${result.error.message}`);
    }
    // ...
  }
}
```

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

## Points d'attention

1. **Retrocompatibilite**: Les champs optionnels sont retrocompatibles, les champs requis non.

2. **Validation**: La validation Zod se fait cote consumer (notification-service). Le producer (oauth2-server) fait confiance aux types Java.

3. **Serialisation**: Les dates sont en format ISO 8601 (`z.string().datetime()`). Java utilise `Instant`.

4. **Enums**: Les enums Zod (`z.enum(['fr', 'en', 'ar'])`) deviennent des enums Java imbriquees.

5. **UUID**: `z.string().uuid()` devient `java.util.UUID` en Java.

6. **URI**: `z.string().url()` devient `java.net.URI` en Java.

## Tests

Pour tester les schemas:

```typescript
import { SendVerificationEmailJobSchema } from './email-contracts';

// Test valide
const valid = SendVerificationEmailJobSchema.parse({
  userId: '550e8400-e29b-41d4-a716-446655440000',
  email: 'test@example.com',
  username: 'testuser',
  token: 'a'.repeat(32),
  verificationLink: 'https://example.com/verify?token=abc',
  expiresAt: new Date().toISOString(),
});

// Test invalide (devrait throw)
try {
  SendVerificationEmailJobSchema.parse({ invalid: 'data' });
} catch (e) {
  console.log('Validation error:', e.errors);
}
```

## Ressources

- [Zod Documentation](https://zod.dev)
- [jsonschema2pojo](https://www.jsonschema2pojo.org/)
- [BullMQ Documentation](https://docs.bullmq.io/)
- [AsyncAPI Specification](https://www.asyncapi.com/)
