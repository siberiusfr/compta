# Script de correction des vues - Guide

## Problème identifié

Plusieurs vues ont le même problème: elles essaient d'accéder à des propriétés de données qui peuvent être `undefined` au moment du chargement.

**Erreur typique**: `Cannot read properties of undefined (reading 'length')`

## Vues corrigées

✅ DashboardView.vue - Ajouté vérification de `recentActivities`
✅ NotificationsInbox.vue - Ajouté vérification de `notifications`

## Vues à corriger

Le même modèle doit être appliqué à toutes ces vues:

### Notifications
- ❌ NotificationsSent.vue
- ❌ NotificationsSettings.vue
- ❌ NotificationsTemplates.vue

### OAuth
- ❌ OAuthApplications.vue
- ❌ OAuthTokens.vue
- ❌ OAuthConsents.vue

### Documents
- ❌ DocumentsAll.vue
- ❌ DocumentsCategories.vue
- ❌ DocumentsUpload.vue

### Permissions
- ❌ PermissionsRoles.vue
- ❌ PermissionsUsers.vue
- ❌ PermissionsAudit.vue

### Companies
- ❌ CompaniesAll.vue
- ❌ CompaniesCreate.vue
- ❌ CompaniesSettings.vue

### HR
- ❌ HrEmployees.vue
- ❌ HrContracts.vue
- ❌ HrLeaves.vue
- ❌ HrPayroll.vue

### Accounting
- ❌ AccountingJournal.vue
- ❌ AccountingLedger.vue
- ❌ AccountingBalanceSheet.vue
- ❌ AccountingIncomeStatement.vue
- ❌ AccountingInvoices.vue
- ❌ AccountingExpenses.vue
- ❌ AccountingReports.vue

## Pattern de correction

Pour chaque vue, appliquer le pattern suivant:

### 1. Dans le script setup

**Avant:**
```typescript
const { items } = useStore()
const filteredItems = computed(() => items.value)
```

**Après:**
```typescript
const { items } = useStore()

// Vérification de sécurité
const safeItems = computed(() => {
  if (!items.value || !Array.isArray(items.value)) {
    return []
  }
  return items.value
})

const filteredItems = computed(() => {
  if (typeFilter.value === 'active') {
    return safeItems.value.filter(i => i.active)
  }
  return safeItems.value
})
```

### 2. Dans le template

**Avant:**
```vue
<div v-for="item in filteredItems" :key="item.id">
  {{ item.name }}
</div>

<div v-if="filteredItems.length === 0">
  Aucun item
</div>
```

**Après:**
```vue
<!-- État de chargement -->
<div v-if="!items" class="loading">
  Chargement...
</div>

<!-- Contenu quand les données sont prêtes -->
<div v-else>
  <div v-for="item in filteredItems" :key="item.id">
    {{ item.name }}
  </div>

  <div v-if="filteredItems.length === 0">
    Aucun item
  </div>
</div>
```

## Correction automatique

Le script suivant peut être utilisé pour identifier les vues qui ont des problèmes:

```powershell
# Chercher tous les fichiers .value qui n'ont pas de vérification
Select-String -Path "src/modules/**/views/*.vue" -Pattern "\.value\[" -CaseSensitive:$false
```

## Test rapide

Pour chaque vue corrigée, tester:
1. Aller sur la page
2. Vérifier qu'il n'y a pas d'erreur dans la console
3. Vérifier que les données s'affichent correctement
4. Rafraîchir la page pour tester le chargement

## Priorité de correction

1. **Haute priorité**: Dashboard, Notifications, OAuth (core functionality)
2. **Moyenne priorité**: Documents, Permissions, Companies
3. **Basse priorité**: HR, Accounting (modules spécifiques)
