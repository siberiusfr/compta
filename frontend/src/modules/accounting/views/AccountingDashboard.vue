<template>
  <div class="dashboard">
    <n-page-header title="Tableau de bord Comptabilité" />

    <n-space vertical :size="24" style="margin-top: 24px">
      <n-grid :cols="3" :x-gap="16">
        <n-gi>
          <n-card title="Revenus" hoverable>
            <n-statistic :value="accountingStore.totalRevenue">
              <template #suffix>€</template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="Dépenses" hoverable>
            <n-statistic :value="accountingStore.totalExpenses">
              <template #suffix>€</template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="Solde" hoverable>
            <n-statistic :value="accountingStore.balance">
              <template #suffix>€</template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="Actions rapides">
        <n-space>
          <n-button type="primary" @click="router.push({ name: 'accounting-invoices' })">
            Voir les factures
          </n-button>
          <n-button type="primary" @click="router.push({ name: 'accounting-expenses' })">
            Voir les dépenses
          </n-button>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { useAccountingStore } from '@stores/index'

const router = useRouter()
const accountingStore = useAccountingStore()

onMounted(() => {
  accountingStore.fetchInvoices()
  accountingStore.fetchExpenses()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}
</style>
