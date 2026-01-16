import type { RouteRecordRaw } from 'vue-router'

export const referentielRoutes: RouteRecordRaw[] = [
  {
    path: 'referentiel',
    component: () => import('./views/ReferentielIndex.vue'),
    children: [
      {
        path: '',
        redirect: { name: 'referentiel-produits-list' },
      },
      // Produits
      {
        path: 'produits',
        name: 'referentiel-produits-list',
        component: () => import('./views/ProduitsList.vue'),
        meta: {
          title: 'Produits',
          requiresAuth: true,
        },
      },
      {
        path: 'produits/create',
        name: 'referentiel-produits-create',
        component: () => import('./views/ProduitCreate.vue'),
        meta: {
          title: 'Nouveau produit',
          requiresAuth: true,
        },
      },
      {
        path: 'produits/:id/edit',
        name: 'referentiel-produits-edit',
        component: () => import('./views/ProduitEdit.vue'),
        meta: {
          title: 'Modifier produit',
          requiresAuth: true,
        },
      },
      // Clients
      {
        path: 'clients',
        name: 'referentiel-clients-list',
        component: () => import('./views/ClientsList.vue'),
        meta: {
          title: 'Clients',
          requiresAuth: true,
        },
      },
      {
        path: 'clients/create',
        name: 'referentiel-clients-create',
        component: () => import('./views/ClientCreate.vue'),
        meta: {
          title: 'Nouveau client',
          requiresAuth: true,
        },
      },
      {
        path: 'clients/:id/edit',
        name: 'referentiel-clients-edit',
        component: () => import('./views/ClientEdit.vue'),
        meta: {
          title: 'Modifier client',
          requiresAuth: true,
        },
      },
      // Fournisseurs
      {
        path: 'fournisseurs',
        name: 'referentiel-fournisseurs-list',
        component: () => import('./views/FournisseursList.vue'),
        meta: {
          title: 'Fournisseurs',
          requiresAuth: true,
        },
      },
      {
        path: 'fournisseurs/create',
        name: 'referentiel-fournisseurs-create',
        component: () => import('./views/FournisseurCreate.vue'),
        meta: {
          title: 'Nouveau fournisseur',
          requiresAuth: true,
        },
      },
      {
        path: 'fournisseurs/:id/edit',
        name: 'referentiel-fournisseurs-edit',
        component: () => import('./views/FournisseurEdit.vue'),
        meta: {
          title: 'Modifier fournisseur',
          requiresAuth: true,
        },
      },
      // Familles Produits
      {
        path: 'familles',
        name: 'referentiel-familles-list',
        component: () => import('./views/FamillesList.vue'),
        meta: {
          title: 'Familles de produits',
          requiresAuth: true,
        },
      },
      {
        path: 'familles/create',
        name: 'referentiel-familles-create',
        component: () => import('./views/FamilleCreate.vue'),
        meta: {
          title: 'Nouvelle famille',
          requiresAuth: true,
        },
      },
      {
        path: 'familles/:id/edit',
        name: 'referentiel-familles-edit',
        component: () => import('./views/FamilleEdit.vue'),
        meta: {
          title: 'Modifier famille',
          requiresAuth: true,
        },
      },
    ],
  },
]
