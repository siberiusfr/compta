# Compta - Application de Gestion d'Entreprise

Application moderne de gestion d'entreprise construite avec Vue 3, TypeScript, et Vite.

## 🚀 Stack Technique

- **Vue 3** - Framework JavaScript progressif
- **TypeScript** - Typage statique
- **Vite** - Build tool ultra-rapide
- **Pinia** - State management
- **Vue Router** - Routing avec lazy loading
- **Naive UI** - Bibliothèque de composants UI
- **Axios** - Client HTTP
- **Tanstack Query** - Gestion des requêtes API
- **VueUse** - Collection de composables
- **Vee-validate** - Validation de formulaires
- **Zod** - Schémas de validation
- **Vitest** - Framework de tests
- **ESLint + Prettier** - Linting et formatage

## 📁 Structure du Projet

```
src/
├── api/              # Couche d'API (axios + endpoints)
├── modules/          # Modules métier (auth, accounting, hr, documents)
│   ├── auth/
│   ├── accounting/
│   ├── hr/
│   └── documents/
├── layouts/          # Layouts (Auth, Default)
├── router/           # Configuration du router
├── stores/           # Stores Pinia centralisés
├── composables/      # Composables réutilisables
├── utils/            # Fonctions utilitaires
├── types/            # Types TypeScript globaux
└── views/            # Vues partagées (404, etc.)
```

## 🎯 Modules

### 1. Authentication (Auth)
- Connexion / Inscription
- Gestion de session
- Protection des routes

### 2. Comptabilité (Accounting)
- Gestion des factures
- Gestion des dépenses
- Tableau de bord financier

### 3. Ressources Humaines (HR)
- Gestion des employés
- Gestion de la paie
- Statistiques RH

### 4. Documents
- Téléversement de documents
- Organisation par catégorie
- Gestion des fichiers

## 🛠️ Installation

```bash
# Installer les dépendances
pnpm install

# Copier le fichier d'environnement
cp .env.example .env
```

## 🏃 Développement

```bash
# Démarrer le serveur de développement
pnpm dev

# Compiler et vérifier les types
pnpm build

# Lancer les tests
pnpm test

# Linter le code
pnpm lint

# Formater le code
pnpm format
```

## 🔧 Configuration

### Variables d'environnement

Créer un fichier `.env` à la racine du projet:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=30000
VITE_APP_NAME=Compta
VITE_ENABLE_DEVTOOLS=true
```

### Path Aliases

Les alias suivants sont configurés:

- `@/` → `src/`
- `@modules/` → `src/modules/`
- `@components/` → `src/components/`
- `@layouts/` → `src/layouts/`
- `@utils/` → `src/utils/`
- `@api/` → `src/api/`
- `@stores/` → `src/stores/`
- `@types/` → `src/types/`

## 📝 Conventions de Code

- **Composants Vue**: PascalCase (ex: `UserProfile.vue`)
- **Fichiers TS/JS**: camelCase (ex: `formatDate.ts`)
- **Stores**: prefixe `use` (ex: `useAuthStore`)
- **Composables**: prefixe `use` (ex: `useAsync`)
- **Types**: PascalCase (ex: `User`, `ApiResponse`)

## 🧪 Tests

```bash
# Lancer les tests en mode watch
pnpm test

# Lancer les tests avec coverage
pnpm test -- --coverage
```

## 📦 Build

```bash
# Build pour production
pnpm build

# Prévisualiser le build
pnpm preview
```

## 🔐 Authentification

L'authentification utilise JWT tokens stockés dans localStorage.
Les routes protégées redirigent automatiquement vers `/login` si non authentifié.

## 🎨 UI/UX

L'application utilise Naive UI pour une interface moderne et réactive.
Tous les composants sont auto-importés grâce à `unplugin-vue-components`.

## 📚 Documentation

- [Structure des modules](src/modules/README.md)
- [Vue 3](https://vuejs.org/)
- [TypeScript](https://www.typescriptlang.org/)
- [Naive UI](https://www.naiveui.com/)
- [Pinia](https://pinia.vuejs.org/)

## 🤝 Contribution

1. Créer une branche feature (`git checkout -b feature/amazing-feature`)
2. Commit les changements (`git commit -m 'Add amazing feature'`)
3. Push sur la branche (`git push origin feature/amazing-feature`)
4. Ouvrir une Pull Request

## 📄 License

MIT
