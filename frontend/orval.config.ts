import { defineConfig } from 'orval'

export default defineConfig({
  oauth2: {
    input: {
      target: './openapi/oauth2.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split', // ou 'single', 'split'
      target: './src/api/oauth/gen/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'oauthInstance',
        },
      },
    },
  },
  documents: {
    input: {
      target: './openapi/documents.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split', // ou 'single', 'split'
      target: './src/api/documents/gen/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'documentsInstance',
        },
      },
    },
  },
  authz: {
    input: {
      target: './openapi/authz.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split', // ou 'single', 'split'
      target: './src/api/authz/gen/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'authzInstance',
        },
      },
    },
  },
  referentiel: {
    input: {
      target: './openapi/referentiel.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split', // ou 'single', 'split'
      target: './src/api/referentiel/gen/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'referentielInstance',
        },
      },
    },
  },
})
