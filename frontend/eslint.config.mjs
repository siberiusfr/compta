import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'
import skipPrettier from 'eslint-config-prettier'
import pluginQuery from '@tanstack/eslint-plugin-query'

export default tseslint.config(
  // Désactive les fichiers inutiles
  { ignores: ['dist/**', 'node_modules/**', 'public/**'] },

  // Base TypeScript & Vue
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/essential'], // ou 'flat/recommended' pour plus de rigueur
  ...pluginQuery.configs['flat/recommended'],

  {
    files: ['**/*.{ts,vue}'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
      },
    },
    rules: {
      // Tes règles personnalisées ici
      'vue/multi-word-component-names': 'off',
      '@typescript-eslint/no-unused-vars': 'warn',
    },
  },

  // Toujours à la fin pour désactiver les conflits de formatage
  skipPrettier
)