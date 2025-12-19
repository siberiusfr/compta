import { defineConfig } from 'orval'

export default defineConfig({
  auth: {
    input: {
      target: 'http://localhost:8083/v3/api-docs',
    },
    output: {
      client: 'vue-query',
      mode: 'single',
      target: './src/modules/auth/api/generated/auth-api.ts',
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customAxios'
        }
      }
    }
  },

  comptabilite: {
    input: {
      target: 'http://localhost:8081/v3/api-docs',
    },
    output: {
      target: './src/api/comptabilite.ts',
      client: 'axios',
      mode: 'single',
      clean: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customAxios'
        }
      }
    }
  },

  facturation: {
    input: {
      target: 'http://localhost:8082/v3/api-docs',
    },
    output: {
      target: './src/api/facturation.ts',
      client: 'axios',
      mode: 'single',
      clean: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customAxios'
        }
      }
    }
  }
})
