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
  SendVerificationEmailJobSchema,
  SendPasswordResetEmailJobSchema,
  EmailVerificationSentEventSchema,
  EmailVerificationFailedEventSchema,
  PasswordResetSentEventSchema,
  PasswordResetFailedEventSchema,
  BullMQJobOptionsSchema,
} from './email-contracts';

// Configuration
const OUTPUT_DIR = path.join(__dirname, '..', 'generated', 'json-schemas');
const JAVA_PACKAGE = 'tn.cyberious.compta.contracts.notification';

// Liste des schemas a generer
const schemas = [
  {
    name: 'SendVerificationEmailJob',
    schema: SendVerificationEmailJobSchema,
    description: 'Job de demande d\'envoi d\'email de verification',
  },
  {
    name: 'SendPasswordResetEmailJob',
    schema: SendPasswordResetEmailJobSchema,
    description: 'Job de demande d\'envoi d\'email de reset de mot de passe',
  },
  {
    name: 'EmailVerificationSentEvent',
    schema: EmailVerificationSentEventSchema,
    description: 'Event: email de verification envoye avec succes',
  },
  {
    name: 'EmailVerificationFailedEvent',
    schema: EmailVerificationFailedEventSchema,
    description: 'Event: echec d\'envoi de l\'email de verification',
  },
  {
    name: 'PasswordResetSentEvent',
    schema: PasswordResetSentEventSchema,
    description: 'Event: email de reset envoye avec succes',
  },
  {
    name: 'PasswordResetFailedEvent',
    schema: PasswordResetFailedEventSchema,
    description: 'Event: echec d\'envoi de l\'email de reset',
  },
  {
    name: 'BullMQJobOptions',
    schema: BullMQJobOptionsSchema,
    description: 'Options de configuration des jobs BullMQ',
  },
];

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
 */
function generateJsonSchema(
  name: string,
  zodSchema: any,
  description: string,
): object {
  const jsonSchema = zodToJsonSchema(zodSchema, {
    name,
    $refStrategy: 'none',
  });

  // Ajouter des metadonnees pour jsonschema2pojo
  return {
    $schema: 'http://json-schema.org/draft-07/schema#',
    $id: `${JAVA_PACKAGE}.${name}`,
    title: name,
    description,
    javaType: `${JAVA_PACKAGE}.${name}`,
    ...jsonSchema,
  };
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
    description: 'Liste des schemas de contrats disponibles',
    generatedAt: new Date().toISOString(),
    javaPackage: JAVA_PACKAGE,
    schemas: schemas.map((s) => ({
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

  // Creer le repertoire de sortie
  ensureOutputDir();

  // Generer chaque schema
  for (const { name, schema, description } of schemas) {
    try {
      const jsonSchema = generateJsonSchema(name, schema, description);
      writeJsonSchema(name, jsonSchema);
    } catch (error) {
      console.error(`Error generating ${name}:`, error);
      process.exit(1);
    }
  }

  // Generer l'index
  generateIndex();

  console.log('='.repeat(60));
  console.log(`Successfully generated ${schemas.length} JSON Schemas`);
  console.log(`Output directory: ${OUTPUT_DIR}`);
  console.log('='.repeat(60));
}

// Executer
main();
