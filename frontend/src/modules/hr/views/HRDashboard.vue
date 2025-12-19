<template>
  <div class="dashboard">
    <n-page-header title="Tableau de bord RH" />

    <n-space vertical :size="24" style="margin-top: 24px">
      <n-grid :cols="3" :x-gap="16">
        <n-gi>
          <n-card title="Employés" hoverable>
            <n-statistic :value="hrStore.employeeCount" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="Masse salariale" hoverable>
            <n-statistic :value="hrStore.totalPayroll">
              <template #suffix>€/mois</template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="Départements" hoverable>
            <n-statistic :value="Object.keys(hrStore.employeesByDepartment).length" />
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="Actions rapides">
        <n-space>
          <n-button type="primary" @click="router.push({ name: 'hr-employees' })">
            Voir les employés
          </n-button>
          <n-button type="primary" @click="router.push({ name: 'hr-payroll' })">
            Gestion de la paie
          </n-button>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { useHRStore } from '@stores/index'

const router = useRouter()
const hrStore = useHRStore()

onMounted(() => {
  hrStore.fetchEmployees()
})
</script>

