# TASKS.md - Taches d'Architecture

## Legende

- [ ] A faire
- [x] Termine
- [~] En cours

---

## 1. Structure de Base (Priorite Haute)

### Dossiers et Organisation

- [ ] Creer la structure de dossiers complete
  ```
  src/
  ├── api/
  │   ├── client.ts
  │   └── endpoints/
  ├── components/
  │   └── ui/
  ├── composables/
  ├── layouts/
  ├── pages/
  ├── router/
  ├── stores/
  ├── types/
  └── validators/
  ```
- [ ] Configurer les alias TypeScript si manquants
- [x] Configurer shadcn-vue (reka-ui)

### Configuration API

- [ ] Creer `api/client.ts` avec instance Axios
- [ ] Configurer les intercepteurs (auth, erreurs)
- [ ] Creer fichier `.env` avec `VITE_API_URL`
- [ ] Creer `.env.example` pour la documentation

---

## 2. Routing (Priorite Haute)

- [ ] Creer `router/index.ts`
- [ ] Configurer les routes de base
- [ ] Implementer les guards d'authentification
- [ ] Configurer le lazy loading des pages
- [ ] Ajouter les meta pour les titres de pages

```ts
// Structure cible
const routes = [
  { path: '/', component: () => import('@/pages/Home.vue') },
  { path: '/login', component: () => import('@/pages/Login.vue') },
  {
    path: '/dashboard',
    component: () => import('@/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [...]
  }
]
```

---

## 3. Authentification (Priorite Haute)

### Store Auth

- [ ] Creer `stores/authStore.ts`
- [ ] Gerer le token JWT (storage + state)
- [ ] Actions: login, logout, refreshToken
- [ ] Computed: isAuthenticated, currentUser

### Composables

- [ ] Creer `composables/useAuth.ts`
- [ ] Hook pour login/logout
- [ ] Hook pour verifier les permissions

### Guards

- [ ] Guard de route pour pages protegees
- [ ] Redirection si non authentifie
- [ ] Gestion du refresh token expire

---

## 4. Layouts (Priorite Moyenne)

- [ ] Creer `layouts/DefaultLayout.vue`
- [ ] Creer `layouts/AuthLayout.vue` (login/register)
- [ ] Creer `layouts/DashboardLayout.vue`
- [ ] Implementer la sidebar
- [ ] Implementer le header avec user menu
- [ ] Support dark mode

---

## 5. Composants UI (Priorite Moyenne)

### Composants de Base (shadcn-vue)

- [x] Button
- [ ] Input
- [ ] Card
- [ ] Dialog/Modal
- [ ] Dropdown Menu
- [ ] Table
- [ ] Form (avec labels, errors)
- [ ] Toast/Notifications
- [ ] Loading/Spinner
- [ ] Avatar
- [ ] Badge

### Composants Applicatifs

- [ ] AppHeader
- [ ] AppSidebar
- [ ] AppBreadcrumb
- [ ] DataTable (avec pagination, tri, filtres)
- [ ] ConfirmDialog
- [ ] EmptyState
- [ ] ErrorBoundary

---

## 6. Gestion des Erreurs (Priorite Moyenne)

- [ ] Creer composant ErrorBoundary
- [ ] Configurer error handler global Vue
- [ ] Toast pour erreurs API
- [ ] Page 404
- [ ] Page 500/erreur generique
- [ ] Logging des erreurs (optionnel: Sentry)

---

## 7. Types TypeScript (Priorite Moyenne)

- [ ] Creer `types/index.ts` - exports
- [ ] Creer `types/api.ts` - types reponses API
- [ ] Creer `types/models.ts` - entites metier
- [ ] Creer `types/forms.ts` - types formulaires

```ts
// Exemples de types a definir
interface User { ... }
interface ApiResponse<T> { ... }
interface PaginatedResponse<T> { ... }
```

---

## 8. Formulaires et Validation (Priorite Moyenne)

- [ ] Installer `@vee-validate/zod`
- [ ] Creer schemas Zod de base dans `validators/`
- [ ] Creer composant FormField reutilisable
- [ ] Helper pour afficher les erreurs serveur

---

## 9. Testing (Priorite Basse)

### Setup

- [ ] Installer Vitest
- [ ] Configurer vitest.config.ts
- [ ] Installer @vue/test-utils
- [ ] Installer @testing-library/vue (optionnel)

### Tests a Ecrire

- [ ] Tests unitaires des stores
- [ ] Tests unitaires des composables
- [ ] Tests des composants critiques
- [ ] Tests d'integration des formulaires

### E2E (Plus tard)

- [ ] Installer Playwright
- [ ] Configurer playwright.config.ts
- [ ] Tests du parcours login
- [ ] Tests des parcours critiques

---

## 10. Performance et Optimisation (Priorite Basse)

- [ ] Configurer le code splitting par route
- [ ] Optimiser les imports (tree-shaking)
- [ ] Lazy load des composants lourds
- [ ] Preload des routes critiques
- [ ] Analyser le bundle (vite-bundle-visualizer)

---

## 11. DevX et Qualite (Priorite Basse)

- [ ] Configurer ESLint avec rules Vue/TS
- [ ] Configurer Prettier
- [ ] Ajouter lint-staged + husky
- [ ] Configurer les devtools Vue Query
- [ ] Ajouter des snippets VS Code

---

## 12. Documentation (Priorite Basse)

- [ ] Documenter les composants principaux
- [ ] Creer un Storybook (optionnel)
- [ ] README avec instructions de setup
- [ ] Documentation API interne

---

## Ordre de Realisation Suggere

1. **Sprint 1 - Fondations**
   - Structure dossiers
   - API client
   - Router de base
   - Auth store

2. **Sprint 2 - UI de Base**
   - Layouts
   - Composants UI essentiels
   - Pages login/register

3. **Sprint 3 - Features**
   - Pages metier
   - Formulaires
   - Gestion erreurs

4. **Sprint 4 - Polish**
   - Tests
   - Performance
   - Documentation

---

## Notes

- Prioriser les taches selon les besoins metier
- Ne pas sur-architecturer au debut
- Iterer et refactorer au fur et a mesure
- Garder ce fichier a jour
