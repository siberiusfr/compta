# Fonctionnalités et Configuration

Ce document décrit toutes les fonctionnalités et configurations du projet.

## 📦 Modules

### Auth (Authentification)
- **Routes**: `/login`, `/register`
- **Store**: `useAuthStore`
- **Types**: LoginCredentials, RegisterData, AuthState, AuthResponse
- **Fonctionnalités**:
  - Connexion utilisateur
  - Inscription
  - Gestion de session JWT
  - Vérification d'authentification

### Accounting (Comptabilité)
- **Routes**: `/accounting`, `/accounting/invoices`, `/accounting/expenses`
- **Store**: `useAccountingStore`
- **Types**: Invoice, Expense, InvoiceItem, AccountingStats
- **Fonctionnalités**:
  - Gestion des factures (CRUD)
  - Gestion des dépenses (CRUD)
  - Statistiques financières
  - Calcul du solde

### HR (Ressources Humaines)
- **Routes**: `/hr`, `/hr/employees`, `/hr/payroll`
- **Store**: `useHRStore`
- **Types**: Employee, PayrollEntry, Department, HRStats
- **Fonctionnalités**:
  - Gestion des employés
  - Gestion de la paie
  - Statistiques RH par département
  - Calcul de la masse salariale

### Documents
- **Routes**: `/documents`, `/documents/upload`
- **Store**: `useDocumentsStore`
- **Types**: Document, DocumentCategory, DocumentMetadata
- **Fonctionnalités**:
  - Téléversement de documents
  - Catégorisation
  - Filtrage et recherche
  - Gestion des versions

## 🎨 Configuration UI

### Thème Personnalisé
- Fichier: `src/config/theme.ts`
- Couleurs primaires personnalisées
- Typographie cohérente
- Espacements et bordures harmonisés
- Tokens de design réutilisables

### Design Tokens
```typescript
spacing: { xs, sm, md, lg, xl, xxl }
colors: { primary, secondary, success, warning, error, info }
shadows: { sm, base, md, lg, xl }
transitions: { fast, base, slow }
zIndex: { dropdown, sticky, fixed, modal, popover, tooltip }
```

## 🛣️ Router & Middlewares

### Middlewares Disponibles

1. **loggingMiddleware**
   - Logs de navigation (dev only)
   - Affiche from/to/meta

2. **progressMiddleware**
   - Barre de progression pendant la navigation
   - Gestion des erreurs

3. **permissionsMiddleware**
   - Vérification des rôles utilisateur
   - Vérification des permissions
   - Redirection vers 403 si accès refusé

4. **analyticsMiddleware**
   - Tracking des pages vues
   - Intégration Google Analytics
   - Logs personnalisés

5. **maintenanceMiddleware**
   - Mode maintenance
   - Redirection automatique

### Configuration des Routes
```typescript
meta: {
  requiresAuth: boolean
  roles: string[]
  permissions: string[]
  layout: 'default' | 'auth'
  title: string
}
```

## 🧩 Composants Réutilisables

### Base Components (`src/components/base/`)

1. **LoadingSpinner**
   - Props: size, text, fullscreen
   - Mode plein écran disponible

2. **ErrorDisplay**
   - Props: type, title, message, closable
   - Types: error, warning, info, success

3. **ConfirmDialog**
   - Props: title, message, positiveText, negativeText
   - Événements: confirm, cancel

4. **EmptyState**
   - Props: description, actionText, actionType
   - Support des slots pour icône personnalisée

5. **PageHeader**
   - Props: title, subtitle, showBack
   - Slots: avatar, header, extra, footer

### ErrorBoundary
- Capture les erreurs Vue
- Affichage d'erreur gracieux
- Option de réinitialisation

## 🔧 Utilitaires

### Format (`src/utils/format.ts`)
- `formatCurrency(amount, currency)` - Formatage monétaire
- `formatDate(date, locale)` - Formatage de date
- `formatDateTime(date, locale)` - Formatage date + heure
- `formatFileSize(bytes)` - Taille de fichier lisible
- `truncate(str, length, suffix)` - Troncature de texte

### Validation (`src/utils/validation.ts`)
- `isValidEmail(email)` - Validation email
- `isValidPhone(phone)` - Validation téléphone FR
- `isValidSIRET(siret)` - Validation SIRET
- `isNotEmpty(value)` - Vérification non vide
- `minLength(value, min)` - Longueur minimale
- `maxLength(value, max)` - Longueur maximale

### Storage (`src/utils/storage.ts`)
- Wrapper localStorage avec JSON
- Wrapper sessionStorage avec JSON
- Gestion d'erreurs intégrée
- Méthodes: get, set, remove, clear, has

## 🧪 Tests

### Coverage
- **33 tests** au total, tous passants
- Format: 10 tests
- Validation: 12 tests
- AuthStore: 5 tests
- Composants: 6 tests

### Configuration
- Environment: happy-dom
- Coverage provider: v8
- Setup global: vitest.setup.ts
- Mock localStorage complet

### Scripts de Test
```bash
pnpm test           # Mode watch
pnpm test:run       # Une seule fois
pnpm test:ui        # Interface UI
pnpm test:coverage  # Avec coverage
```

## 📝 Constants

### App (`src/constants/app.ts`)
- API_BASE_URL
- API_TIMEOUT
- APP_NAME, APP_VERSION
- ENABLE_DEVTOOLS

### Routes (`src/constants/routes.ts`)
- ROUTE_NAMES - Noms de routes typés
- ROUTE_PATHS - Chemins de routes

### Status (`src/constants/status.ts`)
- INVOICE_STATUS + labels FR
- DOCUMENT_CATEGORIES + labels FR
- USER_ROLES + labels FR

### Messages (`src/constants/messages.ts`)
- ERROR_MESSAGES - Messages d'erreur
- SUCCESS_MESSAGES - Messages de succès
- CONFIRMATION_MESSAGES - Confirmations
- INFO_MESSAGES - Messages informatifs

## 🚀 Scripts NPM

### Développement
```bash
pnpm dev            # Démarrer serveur dev
pnpm dev:host       # Dev avec --host
```

### Build
```bash
pnpm build          # Build production
pnpm build:analyze  # Build avec analyse
pnpm build:staging  # Build staging
```

### Qualité de Code
```bash
pnpm type-check     # Vérification TypeScript
pnpm lint           # ESLint (avec fix)
pnpm lint:check     # ESLint (sans fix)
pnpm format         # Prettier (avec fix)
pnpm format:check   # Prettier (sans fix)
pnpm check          # Tout vérifier
```

### Tests
```bash
pnpm test           # Tests en mode watch
pnpm test:run       # Tests une fois
pnpm test:ui        # Interface Vitest UI
pnpm test:coverage  # Coverage
pnpm test:watch     # Mode watch explicite
```

### Maintenance
```bash
pnpm clean          # Nettoyer fichiers générés
pnpm clean:install  # Clean + réinstaller
```

### Preview
```bash
pnpm preview        # Prévisualiser build
pnpm preview:https  # Preview en HTTPS
```

## 🌍 Environnements

### Fichiers .env
- `.env.example` - Template
- `.env.development` - Développement
- `.env.staging` - Staging
- `.env.production` - Production

### Variables Disponibles
```env
VITE_API_BASE_URL
VITE_API_TIMEOUT
VITE_APP_NAME
VITE_APP_VERSION
VITE_ENABLE_DEVTOOLS
VITE_MAINTENANCE_MODE
```

## 🎯 VSCode

### Extensions Recommandées
- Vue - Official (Volar)
- ESLint
- Prettier
- TypeScript Vue Plugin

### Configuration
- Format on save activé
- ESLint auto-fix activé
- Prettier comme formateur par défaut
- TypeScript strict mode

### Debugging
- Configuration Chrome/Firefox
- Configuration tests
- Source maps activées

## 📊 Pages d'Erreur

- **404** - Page non trouvée (`/not-found`)
- **403** - Accès refusé (`/403`)
- **500** - Erreur serveur (`/500`)

## 🏗️ Structure Finale

```
src/
├── api/              # API layer (axios + endpoints)
├── components/
│   └── base/         # Composants réutilisables
├── composables/      # Composables Vue
├── config/           # Configuration (theme, naive-ui)
├── constants/        # Constantes globales
├── layouts/          # Layouts (Default, Auth)
├── modules/          # Modules métier
│   ├── auth/
│   │   ├── types/
│   │   ├── stores/
│   │   ├── views/
│   │   └── routes.ts
│   ├── accounting/
│   ├── hr/
│   └── documents/
├── router/
│   ├── middleware/   # Middlewares de navigation
│   └── index.ts
├── stores/           # Index des stores
├── types/            # Types TypeScript globaux
├── utils/            # Utilitaires
└── views/            # Vues (404, 403, 500)
```

## 🎓 Conventions

### Nommage
- **Composants**: PascalCase (`UserProfile.vue`)
- **Fichiers**: camelCase (`formatDate.ts`)
- **Stores**: prefixe `use` (`useAuthStore`)
- **Composables**: prefixe `use` (`useAsync`)
- **Types**: PascalCase (`User`, `Invoice`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`)

### Import Aliases
```typescript
@/          → src/
@modules/   → src/modules/
@components/→ src/components/
@layouts/   → src/layouts/
@utils/     → src/utils/
@api/       → src/api/
@stores/    → src/stores/
@types/     → src/types/
```

## 📈 Performance

- **Lazy Loading**: Tous les modules et routes
- **Code Splitting**: Automatique par module
- **Tree Shaking**: Configuration optimale
- **Compression**: Gzip pour tous les assets
- **Chunking**: Optimisé par Vite

## 🔐 Sécurité

- JWT tokens dans localStorage
- CSRF protection (à configurer)
- XSS protection via Vue
- Content Security Policy (à configurer)
- HTTPS en production

## 📚 Documentation

- README.md - Documentation principale
- FEATURES.md - Ce fichier
- src/modules/README.md - Structure modulaire
- Commentaires JSDoc dans le code
