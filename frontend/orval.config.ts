import { defineConfig } from 'orval';

export default defineConfig({
  oauth2: {
    input: {
      target: './openapi/oauth2.json',
    },
    output: {
      client: 'vue-query',
      mode: 'single', // ou 'single', 'split'
      target: './src/modules/oauth/api/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
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
      target: './src/modules/documents/api/generated.ts',
      mock: false,
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
});