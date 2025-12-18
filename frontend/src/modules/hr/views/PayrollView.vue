<template>
  <div>
    <n-page-header title="Gestion de la paie" @back="router.back" />

    <n-card style="margin-top: 24px">
      <n-space vertical :size="24">
        <n-alert type="info">
          Masse salariale totale : {{ hrStore.totalPayroll.toFixed(2) }}€/mois
        </n-alert>

        <n-data-table
          :columns="columns"
          :data="hrStore.employees"
          :loading="hrStore.loading"
          :pagination="{ pageSize: 10 }"
        />
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { useHRStore } from '@stores'

const router = useRouter()
const hrStore = useHRStore()

const columns: DataTableColumns = [
  {
    title: 'Employé',
    key: 'name',
    render: (row: any) => `${row.firstName} ${row.lastName}`,
  },
  { title: 'Poste', key: 'position' },
  { title: 'Département', key: 'department' },
  { title: 'Salaire brut', key: 'salary', render: (row: any) => `${row.salary}€` },
  {
    title: 'Charges sociales (45%)',
    key: 'charges',
    render: (row: any) => `${(row.salary * 0.45).toFixed(2)}€`,
  },
  {
    title: 'Coût total',
    key: 'total',
    render: (row: any) => `${(row.salary * 1.45).toFixed(2)}€`,
  },
]

onMounted(() => {
  hrStore.fetchEmployees()
})
</script>
