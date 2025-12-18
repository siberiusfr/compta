# Structure Modulaire

Ce projet est organisé en modules indépendants pour une meilleure maintenabilité.

## Modules disponibles

### 1. Auth (Authentication)
- **Routes**: `/login`, `/register`
- **Store**: `useAuthStore`
- **Fonctionnalités**: Connexion, inscription, gestion de session

### 2. Accounting (Comptabilité)
- **Routes**: `/accounting`, `/accounting/invoices`, `/accounting/expenses`
- **Store**: `useAccountingStore`
- **Fonctionnalités**: Gestion des factures et dépenses

### 3. HR (Ressources Humaines)
- **Routes**: `/hr`, `/hr/employees`, `/hr/payroll`
- **Store**: `useHRStore`
- **Fonctionnalités**: Gestion des employés et de la paie

### 4. Documents
- **Routes**: `/documents`, `/documents/upload`
- **Store**: `useDocumentsStore`
- **Fonctionnalités**: Téléversement et gestion de documents

## Structure d'un module

Chaque module suit cette structure:

```
module-name/
├── views/          # Pages du module (lazy-loaded)
├── stores/         # Pinia stores du module
├── routes.ts       # Définition des routes
├── components/     # Composants spécifiques au module
├── composables/    # Composables spécifiques au module
└── types/          # Types TypeScript du module
```

## Ajouter un nouveau module

1. Créer la structure de dossiers dans `src/modules/`
2. Créer le fichier `routes.ts` avec les routes du module
3. Créer les stores Pinia dans `stores/`
4. Créer les vues dans `views/`
5. Importer les routes dans `src/router/index.ts`
6. Exporter les stores dans `src/stores/index.ts`
