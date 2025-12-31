/**
 * Script de generation des JSON Schemas depuis les schemas Zod
 *
 * Usage: npx ts-node src/generate-json-schemas.ts
 *
 * Genere les fichiers JSON Schema dans generated/json-schemas/
 * Ces fichiers sont utilises pour generer les classes Java via jsonschema2pojo
 */

import * as fs from 'fs';
import * as path from 'path';
import { zodToJsonSchema } from 'zod-to-json-schema';
import {
  // Messages complets (enveloppe + payload)
  EmailVerificationRequestedSchema,
  PasswordResetRequestedSchema,
  EmailVerificationSentSchema,
  EmailVerificationFailedSchema,
  PasswordResetSentSchema,
  PasswordResetFailedSchema,
  // Payloads seuls (pour reference)
  SendVerificationEmailPayloadSchema,
  SendPasswordResetEmailPayloadSchema,
  EmailVerificationSentPayloadSchema,
  EmailVerificationFailedPayloadSchema,
  PasswordResetSentPayloadSchema,
  PasswordResetFailedPayloadSchema,
  // Options BullMQ
  BullMQJobOptionsSchema,
} from './email-contracts';

// Configuration
const OUTPUT_DIR = path.join(__dirname, '..', 'generated', 'json-schemas');
const JAVA_PACKAGE = 'tn.cyberious.compta.contracts.notification';

// Liste des schemas de messages complets (enveloppe + payload)
// payloadType indique le nom de la classe Java a utiliser pour le payload
const messageSchemas = [
  {
    name: 'EmailVerificationRequested',
    schema: EmailVerificationRequestedSchema,
    payloadType: 'SendVerificationEmailPayload',
    description:
      'Message complet: Demande d\'envoi d\'email de verification (oauth2-server -> notification-service)',
  },
  {
    name: 'PasswordResetRequested',
    schema: PasswordResetRequestedSchema,
    payloadType: 'SendPasswordResetEmailPayload',
    description:
      'Message complet: Demande d\'envoi d\'email de reset (oauth2-server -> notification-service)',
  },
  {
    name: 'EmailVerificationSent',
    schema: EmailVerificationSentSchema,
    payloadType: 'EmailVerificationSentPayload',
    description:
      'Message complet: Email de verification envoye (notification-service -> oauth2-server)',
  },
  {
    name: 'EmailVerificationFailed',
    schema: EmailVerificationFailedSchema,
    payloadType: 'EmailVerificationFailedPayload',
    description:
      'Message complet: Echec d\'envoi email de verification (notification-service -> oauth2-server)',
  },
  {
    name: 'PasswordResetSent',
    schema: PasswordResetSentSchema,
    payloadType: 'PasswordResetSentPayload',
    description:
      'Message complet: Email de reset envoye (notification-service -> oauth2-server)',
  },
  {
    name: 'PasswordResetFailed',
    schema: PasswordResetFailedSchema,
    payloadType: 'PasswordResetFailedPayload',
    description:
      'Message complet: Echec d\'envoi email de reset (notification-service -> oauth2-server)',
  },
];

// Liste des schemas de payload uniquement (sans enveloppe)
const payloadSchemas = [
  {
    name: 'SendVerificationEmailPayload',
    schema: SendVerificationEmailPayloadSchema,
    description: 'Payload: Donnees pour l\'envoi d\'email de verification',
  },
  {
    name: 'SendPasswordResetEmailPayload',
    schema: SendPasswordResetEmailPayloadSchema,
    description: 'Payload: Donnees pour l\'envoi d\'email de reset',
  },
  {
    name: 'EmailVerificationSentPayload',
    schema: EmailVerificationSentPayloadSchema,
    description: 'Payload: Confirmation d\'envoi email de verification',
  },
  {
    name: 'EmailVerificationFailedPayload',
    schema: EmailVerificationFailedPayloadSchema,
    description: 'Payload: Echec d\'envoi email de verification',
  },
  {
    name: 'PasswordResetSentPayload',
    schema: PasswordResetSentPayloadSchema,
    description: 'Payload: Confirmation d\'envoi email de reset',
  },
  {
    name: 'PasswordResetFailedPayload',
    schema: PasswordResetFailedPayloadSchema,
    description: 'Payload: Echec d\'envoi email de reset',
  },
];

// Schemas utilitaires
const utilitySchemas = [
  {
    name: 'BullMQJobOptions',
    schema: BullMQJobOptionsSchema,
    description: 'Options de configuration des jobs BullMQ',
  },
];

// Tous les schemas a generer
const allSchemas = [...messageSchemas, ...payloadSchemas, ...utilitySchemas];

/**
 * Cree le repertoire de sortie s'il n'existe pas
 */
function ensureOutputDir(): void {
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    console.log(`Created output directory: ${OUTPUT_DIR}`);
  }
}

/**
 * Genere un JSON Schema depuis un schema Zod
 * @param payloadType Si specifie, ajoute javaType au payload pour referencier la classe Java correspondante
 */
function generateJsonSchema(
  name: string,
  zodSchema: any,
  description: string,
  payloadType?: string,
): object {
  const jsonSchema = zodToJsonSchema(zodSchema, {
    name,
    $refStrategy: 'none',
  }) as any;

  // Ajouter des metadonnees pour jsonschema2pojo
  const result: any = {
    $schema: 'http://json-schema.org/draft-07/schema#',
    $id: `${JAVA_PACKAGE}.${name}`,
    title: name,
    description,
    javaType: `${JAVA_PACKAGE}.${name}`,
    ...jsonSchema,
  };

  // Si un payloadType est specifie, ajouter javaType au payload
  // pour que jsonschema2pojo utilise la bonne classe Java
  if (payloadType && result.definitions?.[name]?.properties?.payload) {
    result.definitions[name].properties.payload.javaType = `${JAVA_PACKAGE}.${payloadType}`;
  }

  return result;
}

/**
 * Ecrit un JSON Schema dans un fichier
 */
function writeJsonSchema(name: string, schema: object): void {
  const filePath = path.join(OUTPUT_DIR, `${name}.schema.json`);
  fs.writeFileSync(filePath, JSON.stringify(schema, null, 2));
  console.log(`Generated: ${filePath}`);
}

/**
 * Genere un fichier index listant tous les schemas
 */
function generateIndex(): void {
  const index = {
    $schema: 'http://json-schema.org/draft-07/schema#',
    title: 'COMPTA Notification Contracts Index',
    description:
      'Liste des schemas de contrats disponibles. Tous les messages utilisent le format d\'enveloppe standard.',
    generatedAt: new Date().toISOString(),
    javaPackage: JAVA_PACKAGE,
    envelopeFormat: {
      eventId: 'UUID v4 - Identifiant unique de l\'evenement',
      eventType: 'String - Type de l\'evenement (ex: EmailVerificationRequested)',
      eventVersion: 'Integer - Version du schema (default: 1)',
      occurredAt: 'ISO 8601 - Timestamp de creation',
      producer: 'String - Service producteur (oauth2-server ou notification-service)',
      payload: 'Object - Donnees specifiques a l\'evenement',
    },
    messageSchemas: messageSchemas.map((s) => ({
      name: s.name,
      file: `${s.name}.schema.json`,
      description: s.description,
    })),
    payloadSchemas: payloadSchemas.map((s) => ({
      name: s.name,
      file: `${s.name}.schema.json`,
      description: s.description,
    })),
    utilitySchemas: utilitySchemas.map((s) => ({
      name: s.name,
      file: `${s.name}.schema.json`,
      description: s.description,
    })),
  };

  const filePath = path.join(OUTPUT_DIR, '_index.json');
  fs.writeFileSync(filePath, JSON.stringify(index, null, 2));
  console.log(`Generated index: ${filePath}`);
}

/**
 * Point d'entree principal
 */
function main(): void {
  console.log('='.repeat(60));
  console.log('Generating JSON Schemas from Zod schemas');
  console.log('='.repeat(60));
  console.log('');
  console.log('ENVELOPE FORMAT:');
  console.log('{');
  console.log('  eventId: "uuid",');
  console.log('  eventType: "NomDuType",');
  console.log('  eventVersion: 1,');
  console.log('  occurredAt: "ISO8601",');
  console.log('  producer: "nom-du-service",');
  console.log('  payload: { ... }');
  console.log('}');
  console.log('');

  // Creer le repertoire de sortie
  ensureOutputDir();

  // Generer chaque schema
  console.log('Generating message schemas (envelope + payload)...');
  for (const { name, schema, description, payloadType } of messageSchemas) {
    try {
      const jsonSchema = generateJsonSchema(name, schema, description, payloadType);
      writeJsonSchema(name, jsonSchema);
    } catch (error) {
      console.error(`Error generating ${name}:`, error);
      process.exit(1);
    }
  }

  console.log('');
  console.log('Generating payload schemas...');
  for (const { name, schema, description } of payloadSchemas) {
    try {
      const jsonSchema = generateJsonSchema(name, schema, description);
      writeJsonSchema(name, jsonSchema);
    } catch (error) {
      console.error(`Error generating ${name}:`, error);
      process.exit(1);
    }
  }

  console.log('');
  console.log('Generating utility schemas...');
  for (const { name, schema, description } of utilitySchemas) {
    try {
      const jsonSchema = generateJsonSchema(name, schema, description);
      writeJsonSchema(name, jsonSchema);
    } catch (error) {
      console.error(`Error generating ${name}:`, error);
      process.exit(1);
    }
  }

  // Generer l'index
  console.log('');
  generateIndex();

  console.log('');
  console.log('='.repeat(60));
  console.log(`Successfully generated ${allSchemas.length} JSON Schemas`);
  console.log(`  - ${messageSchemas.length} message schemas (envelope + payload)`);
  console.log(`  - ${payloadSchemas.length} payload schemas`);
  console.log(`  - ${utilitySchemas.length} utility schemas`);
  console.log(`Output directory: ${OUTPUT_DIR}`);
  console.log('='.repeat(60));
}

// Executer
main();
