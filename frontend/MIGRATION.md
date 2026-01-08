# Migration vers Architecture Modulaire avec OAuth2

## Modifications effectuées

### 1. Suppression des anciennes pages
- ❌ `src/pages/HomePage.vue` - Supprimé
- ❌ `src/pages/DashboardPage.vue` - Supprimé
- ❌ `src/pages/` - Dossier supprimé (après migration de AuthorizedPage)

### 2. Conservation de l'authentification OAuth2
- ✅ `src/stores/authStore.ts` - Store d'authentification Pinia
- ✅ `src/services/authService.ts` - Service OAuth2 avec oidc-client-ts
- ✅ `src/components/LoginButton.vue` - Composant de connexion OAuth2
- ✅ `src/core/views/AuthorizedCallback.vue` - Page de callback OAuth2 (anciennement AuthorizedPage)

### 3. Mise à jour des composants Core

#### Router (`src/core/router/index.ts`)
- Ajout de la route `/authorized` pour le callback OAuth2
- Utilisation de l'architecture modulaire avec tous les modules

#### Guards (`src/core/router/guards.ts`)
- Mise à jour pour utiliser `useAuthStore` au lieu de `localStorage.getItem('token')`
- Conservation de l'URL de retour dans `sessionStorage`

#### LoginView (`src/core/views/LoginView.vue`)
- Intégration complète avec `useAuthStore`
- Affichage de l'état de connexion
- Utilisation du composant `LoginButton` pour OAuth2
- Redirection automatique si déjà connecté

#### Topbar (`src/core/components/Topbar.vue`)
- Affichage du profil utilisateur OAuth2
- Bouton de déconnexion OAuth2
- Informations utilisateur (nom, email)

#### Sidebar (`src/core/components/Sidebar.vue`)
- Bouton de déconnexion mis à jour pour utiliser `authStore.logout()`

### 4. Structure de l'application

```
src/
├── core/                      # Infrastructure Core
│   ├── components/
│   │   ├── Sidebar.vue       # ✅ Mis à jour avec OAuth2
│   │   └── Topbar.vue        # ✅ Mis à jour avec OAuth2
│   ├── layouts/
│   │   └── DashboardLayout.vue
│   ├── router/
│   │   ├── index.ts          # ✅ Route /authorized ajoutée
│   │   ├── guards.ts         # ✅ Mis à jour avec useAuthStore
│   │   └── types.ts
│   └── views/
│       ├── LoginView.vue             # ✅ Mis à jour avec OAuth2
│       ├── AuthorizedCallback.vue     # ✅ Page callback OAuth2
│       └── NotFoundView.vue
│
├── modules/                   # Modules métier (tous créés)
│   ├── accounting/
│   ├── companies/
│   ├── dashboard/
│   ├── documents/
│   ├── hr/
│   ├── notifications/
│   ├── oauth/
│   └── permissions/
│
├── services/
│   └── authService.ts        # ✅ Service OAuth2 (conservé)
│
├── stores/
│   └── authStore.ts          # ✅ Store OAuth2 (conservé)
│
├── components/
│   └── LoginButton.vue        # ✅ Bouton de connexion OAuth2 (conservé)
│
└── main.ts                   # ✅ Initialisation OAuth2 (déjà en place)
```

## Flux d'authentification

### 1. Connexion
1. Utilisateur clique sur "Se connecter" (LoginView)
2. `LoginButton` appelle `authStore.login()`
3. Redirection vers `http://localhost:9000/oauth2/authorize`
4. Utilisateur s'authentifie sur le serveur OAuth2
5. Redirection vers `http://localhost:3000/authorized`

### 2. Callback
1. Route `/authorized` charge `AuthorizedCallback.vue`
2. Appel de `authStore.handleCallback()`
3. Échange du code contre un access token
4. Récupération du profil utilisateur
5. Redirection vers l'URL de retour stockée ou `/dashboard`

### 3. Protection des routes
1. Guard `authGuard` vérifie `authStore.isAuthenticated`
2. Si non authentifié, redirige vers `/login`
3. Stockage de l'URL de retour dans `sessionStorage`

### 4. Déconnexion
1. Bouton de déconnexion (Sidebar ou Topbar)
2. Appel de `authStore.logout()`
3. Redirection vers `http://localhost:9000/connect/logout`
4. Redirection vers `http://localhost:3000`
5. Nettoyage local de l'utilisateur

## Modules disponibles

Tous les modules sont créés et fonctionnent avec l'architecture modulaire:

- **Dashboard** (`/dashboard`)
- **Notifications** (`/notifications/*`)
- **OAuth** (`/oauth/*`)
- **Documents** (`/documents/*`)
- **Permissions** (`/permissions/*`)
- **Companies** (`/companies/*`)
- **HR** (`/hr/*`)
- **Accounting** (`/accounting/*`)

## Configuration OAuth2

La configuration OAuth2 est dans `src/services/authService.ts`:

```typescript
const settings: UserManagerSettings = {
  authority: 'http://localhost:9000',
  client_id: 'public-client',
  redirect_uri: 'http://localhost:3000/authorized',
  post_logout_redirect_uri: 'http://localhost:3000',
  response_type: 'code',
  scope: 'openid read write',
  // ... autres options
}
```

## Démarrage

```bash
npm install
npm run dev
```

L'application sera accessible sur `http://localhost:3000`

Le serveur OAuth2 doit être accessible sur `http://localhost:9000`

## Avantages de cette architecture

✅ **Authentification OAuth2 complète** - Système d'authentification robuste
✅ **Architecture modulaire** - Chaque module est auto-suffisant
✅ **Type-safe** - TypeScript strict partout
✅ **Scalable** - Facile d'ajouter de nouveaux modules
✅ **Maintenable** - Code organisé et prévisible
✅ **Performance** - Lazy loading par module
✅ **Prêt pour le backend** - Intégration facile avec Spring Boot
