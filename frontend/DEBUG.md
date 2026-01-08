# Débogage de l'application

## Problèmes connus et solutions

### 1. Écran blanc au démarrage

**Symptôme**: L'application reste blanche après avoir rafraîchi la page avec des tokens enregistrés.

**Cause**: L'application n'était montée qu'après l'initialisation du store d'authentification.

**Solution appliquée**:
- ✅ L'application se monte maintenant immédiatement dans `main.ts`
- ✅ L'état de chargement est géré dans `App.vue`
- ✅ Un composant de chargement s'affiche pendant l'initialisation

**Test**:
```bash
npm run dev
# Connectez-vous avec OAuth2
# Arrêtez le serveur (Ctrl+C)
# Redémarrez le serveur (npm run dev)
# L'application devrait se charger avec vos tokens
```

### 2. Écrans vides dans le dashboard

**Symptôme**: En cliquant sur les pages du dashboard, les écrans sont vides.

**Causes possibles**:
1. Erreurs JavaScript dans les composants
2. Icônes non chargées
3. Stores Pinia non initialisés
4. Problème avec Tailwind CSS

**Solutions**:

#### Test 1: Vérifier la page de test
1. Allez sur `http://localhost:3000/test`
2. Si la page de test s'affiche correctement, Vue, Pinia et Tailwind fonctionnent
3. Le problème est dans un composant spécifique

#### Test 2: Vérifier la console du navigateur
1. Ouvrez les outils de développement (F12)
2. Allez dans l'onglet "Console"
3. Regardez s'il y a des erreurs rouges
4. Cliquez sur les liens pour voir où l'erreur se produit

#### Test 3: Vérifier le composant ErrorBoundary
1. L'application a maintenant un `ErrorBoundary` global
2. Si une erreur se produit, elle sera capturée et affichée
3. Vous verrez le message d'erreur et la pile d'appels

#### Test 4: Vérifier un module simple
1. Essayez d'aller sur `/dashboard`
2. Ouvrez la console et regardez les erreurs
3. Les erreurs les plus courantes:
   - Import manquant
   - Composant introuvable
   - Erreur de type TypeScript

### 3. Icônes non affichées

**Symptôme**: Les icônes ne s'affichent pas dans l'interface.

**Solution**:
Vérifiez que `lucide-vue-next` est installé:
```bash
npm list lucide-vue-next
```

Si non installé:
```bash
npm install lucide-vue-next
```

### 4. Erreurs d'import

**Symptôme**: Erreurs du type "Failed to resolve import"

**Vérifications**:
1. Vérifiez que les chemins d'import sont corrects
2. Vérifiez que les fichiers existent
3. Vérifiez le fichier `tsconfig.json` pour les alias

```bash
# Lister tous les fichiers Vue
find src -name "*.vue"
```

### 5. Erreurs de route

**Symptôme**: "Page not found" ou erreurs 404

**Solution**:
1. Vérifiez que les routes sont définies dans `src/router/index.ts`
2. Vérifiez que les composants importés existent
3. Vérifiez que les modules exportent leurs routes correctement

### Actions de débogage rapides

#### 1. Vider le cache et réinstaller
```bash
rm -rf node_modules
rm -rf .vite
npm install
```

#### 2. Vider le cache du navigateur
1. Ouvrez les outils de développement (F12)
2. Clic droit sur le bouton de rafraîchissement
3. Sélectionnez "Vider le cache et recharger"

#### 3. Tester avec les données mock
1. Tous les modules utilisent déjà des données mock
2. Les stores Pinia sont initialisés avec ces données
3. Pas besoin de backend pour tester

#### 4. Vérifier la configuration
```bash
# Vérifier que Vite fonctionne
npm run dev

# Ouvrir http://localhost:3000/test
```

### Structure de débogage

```
src/
├── core/
│   ├── components/
│   │   └── ErrorBoundary.vue  ✅ Capture les erreurs globales
│   ├── layouts/
│   │   └── DashboardLayout.vue  ✅ Layout principal
│   ├── router/
│   │   ├── index.ts  ✅ Configuration des routes
│   │   └── guards.ts  ✅ Guards d'authentification
│   └── views/
│       ├── LoginRedirect.vue  ✅ Redirection OAuth2
│       ├── AuthorizedCallback.vue  ✅ Callback OAuth2
│       ├── TestView.vue  ✅ Page de test
│       └── NotFoundView.vue  ✅ Page 404
│
├── modules/  ✅ Tous les modules avec données mock
└── App.vue  ✅ Gestion du chargement et ErrorBoundary
```

### Commandes utiles

```bash
# Démarrer en mode développement
npm run dev

# Vérifier les types TypeScript
npm run typecheck

# Linter le code
npm run lint

# Construire pour la production
npm run build
```

### Outils de débogage

1. **Vue DevTools**:
   - Installé par défaut avec `@tanstack/vue-query-devtools`
   - Ouvrir dans le navigateur pour inspecter les composants et stores

2. **Console du navigateur**:
   - Ouvrir avec F12
   - Voir les erreurs JavaScript
   - Voir les logs de l'application

3. **Network tab**:
   - Voir les requêtes réseau
   - Vérifier que les fichiers sont chargés
   - Vérifier les erreurs de chargement

### Rapporter un problème

Si vous rencontrez toujours un problème:

1. Capturez l'erreur dans la console
2. Copiez la pile d'appels depuis ErrorBoundary
3. Notez la page où l'erreur se produit
4. Vérifiez que tous les fichiers existent
5. Essayez la page de test (/test) pour isoler le problème
