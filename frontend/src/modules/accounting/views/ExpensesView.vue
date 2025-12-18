<template>
  <div>
    <n-page-header title="Dépenses" @back="router.back">
      <template #extra>
        <n-button type="primary" @click="showModal = true">Créer une dépense</n-button>
      </template>
    </n-page-header>

    <n-card style="margin-top: 24px">
      <n-data-table
        :columns="columns"
        :data="accountingStore.expenses"
        :loading="accountingStore.loading"
        :pagination="{ pageSize: 10 }"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="dialog" title="Nouvelle dépense">
      <n-form ref="formRef" :model="formValue" :rules="rules">
        <n-form-item path="description" label="Description">
          <n-input v-model:value="formValue.description" placeholder="Description" />
        </n-form-item>
        <n-form-item path="amount" label="Montant">
          <n-input-number v-model:value="formValue.amount" placeholder="0.00" :min="0" />
        </n-form-item>
        <n-form-item path="category" label="Catégorie">
          <n-input v-model:value="formValue.category" placeholder="Catégorie" />
        </n-form-item>
        <n-form-item path="date" label="Date">
          <n-date-picker v-model:value="formValue.date" type="date" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showModal = false">Annuler</n-button>
          <n-button type="primary" @click="handleCreate">Créer</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { useAccountingStore } from '@stores'

const router = useRouter()
const message = useMessage()
const accountingStore = useAccountingStore()

const showModal = ref(false)
const formRef = ref(null)

const formValue = ref({
  description: '',
  amount: 0,
  category: '',
  date: Date.now(),
})

const rules = {
  description: { required: true, message: 'Description requise', trigger: 'blur' },
  amount: { required: true, type: 'number', message: 'Montant requis', trigger: 'blur' },
  category: { required: true, message: 'Catégorie requise', trigger: 'blur' },
}

const columns: DataTableColumns = [
  { title: 'Description', key: 'description' },
  { title: 'Montant', key: 'amount', render: (row: any) => `${row.amount}€` },
  { title: 'Catégorie', key: 'category' },
  { title: 'Date', key: 'date' },
]

async function handleCreate() {
  const result = await accountingStore.createExpense({
    description: formValue.value.description,
    amount: formValue.value.amount,
    category: formValue.value.category,
    date: new Date(formValue.value.date).toISOString(),
  })

  if (result.success) {
    message.success('Dépense créée')
    showModal.value = false
    accountingStore.fetchExpenses()
  } else {
    message.error('Erreur lors de la création')
  }
}

onMounted(() => {
  accountingStore.fetchExpenses()
})
</script>
