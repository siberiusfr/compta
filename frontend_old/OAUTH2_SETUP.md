# Configuration OAuth2 pour Compta Frontend

## Configuration du Serveur OAuth2

Le serveur OAuth2 doit être configuré avec les paramètres suivants :

### Client OAuth2
- **Client ID**: `public-client`
- **Type**: Public client (pas de secret client)
- **PKCE**: Activé (obligatoire)
- **Authorization Grant Types**: `authorization_code`, `refresh_token`

### Scopes disponibles
- `openid`
- `read`
- `write`

### Redirect URIs configurés
Le serveur OAuth2 doit autoriser les redirect URIs suivants :

```
http://localhost:3000/authorized
https://app.compta.tn/authorized
```

### Post Logout Redirect URIs
```
http://localhost:3000
https://app.compta.tn
```

## Configuration de l'Application Frontend

### Variables d'environnement (.env)

```env
# OAuth2 Configuration
VITE_OAUTH2_AUTHORITY=http://localhost:9000
VITE_OAUTH2_REDIRECT_URI=http://localhost:3000/authorized
VITE_OAUTH2_POST_LOGOUT_REDIRECT_URI=http://localhost:3000
VITE_OAUTH2_SILENT_REDIRECT_URI=http://localhost:3000/silent-refresh
```

### Important
- L'application est configurée pour utiliser le port 3000 (voir [`vite.config.ts`](vite.config.ts))
- Assurez-vous que le port dans `VITE_OAUTH2_REDIRECT_URI` correspond au port configuré dans Vite

## Utilisateurs de Test

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |

## Dépannage

### Problème : La redirection vers OAuth2 ne fonctionne pas

1. **Vérifiez la console du navigateur** :
   - Ouvrez les outils de développement (F12)
   - Allez dans l'onglet Console
   - Recherchez les logs commençant par "OAuth2 Configuration" ou "Starting OAuth2 login flow"

2. **Vérifiez les variables d'environnement** :
   - Ouvrez le fichier `.env` à la racine du projet
   - Vérifiez que `VITE_OAUTH2_AUTHORITY` pointe vers votre serveur OAuth2
   - Vérifiez que `VITE_OAUTH2_REDIRECT_URI` correspond au port 3000

3. **Vérifiez la configuration du serveur OAuth2** :
   - Assurez-vous que le serveur OAuth2 est démarré
   - Vérifiez que le redirect_uri est configuré correctement dans le serveur OAuth2
   - Le redirect_uri doit correspondre exactement à `VITE_OAUTH2_REDIRECT_URI`

4. **Vérifiez les erreurs réseau** :
   - Allez dans l'onglet Network des outils de développement
   - Recherchez les requêtes vers le serveur OAuth2
   - Vérifiez qu'il n'y a pas d'erreurs CORS

### Problème : Erreur CORS

Si vous voyez une erreur CORS dans la console :

1. Vérifiez que le serveur OAuth2 autorise les requêtes depuis votre frontend
2. Les origines autorisées doivent inclure :
   - `http://localhost:3000`
   - `https://app.compta.tn`

### Problème : Token expiré

Les tokens expirent après 30 minutes. Le rafraîchissement automatique est activé.

Si vous êtes déconnecté après 30 minutes :
1. Vérifiez que le silent refresh fonctionne
2. Vérifiez que `VITE_OAUTH2_SILENT_REDIRECT_URI` est correctement configuré
3. Vérifiez les logs de la console pour les erreurs de rafraîchissement

## Test de l'Application

1. **Démarrer le serveur OAuth2** :
   ```bash
   # Assurez-vous que le serveur OAuth2 est démarré sur http://localhost:9000
   ```

2. **Démarrer l'application frontend** :
   ```bash
   pnpm dev
   ```

3. **Accéder à l'application** :
   - Ouvrez http://localhost:3000 dans votre navigateur
   - Vous devriez être redirigé vers la page de login

4. **Se connecter** :
   - Utilisez les identifiants de test (admin/admin123 ou user/user123)
   - Après une connexion réussie, vous devriez être redirigé vers le dashboard

5. **Vérifier l'authentification** :
   - Ouvrez les outils de développement (F12)
   - Allez dans l'onglet Application
   - Vérifiez que les tokens sont stockés dans le localStorage/sessionStorage

## Structure de l'implémentation

### Fichiers créés/modifiés

- `src/stores/oauth2Auth.ts` - Store Pinia pour OAuth2
- `src/composables/useAuth.ts` - Composable pour l'authentification
- `src/views/AuthorizedView.vue` - Vue de callback OAuth2
- `src/modules/auth/views/LoginView.vue` - Vue de login (redirection OAuth2)
- `src/api/client.ts` - Client Axios avec intercepteurs OAuth2
- `src/router/index.ts` - Configuration du routeur avec guards OAuth2
- `.env` - Variables d'environnement OAuth2

### Flux d'authentification

1. L'utilisateur accède à une route protégée
2. Le routeur vérifie l'authentification via `useOAuth2AuthStore`
3. Si non authentifié, redirection vers `/login`
4. La vue Login appelle `authStore.login()`
5. `oidc-client-ts` redirige vers le serveur OAuth2
6. L'utilisateur s'authentifie sur le serveur OAuth2
7. Le serveur OAuth2 redirige vers `/authorized` avec un code d'autorisation
8. La vue Authorized appelle `authStore.handleCallback()`
9. Le code est échangé contre des tokens
10. L'utilisateur est redirigé vers la page demandée

## Support

Si vous rencontrez des problèmes :

1. Consultez les logs de la console du navigateur
2. Consultez les logs du serveur OAuth2
3. Vérifiez la configuration des variables d'environnement
4. Assurez-vous que le serveur OAuth2 est accessible depuis votre frontend
