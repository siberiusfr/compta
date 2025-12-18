<template>
  <div>
    <n-page-header title="Factures" @back="router.back">
      <template #extra>
        <n-button type="primary" @click="showModal = true">Créer une facture</n-button>
      </template>
    </n-page-header>

    <n-card style="margin-top: 24px">
      <n-data-table
        :columns="columns"
        :data="accountingStore.invoices"
        :loading="accountingStore.loading"
        :pagination="{ pageSize: 10 }"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="dialog" title="Nouvelle facture">
      <n-form ref="formRef" :model="formValue" :rules="rules">
        <n-form-item path="number" label="Numéro">
          <n-input v-model:value="formValue.number" placeholder="INV-001" />
        </n-form-item>
        <n-form-item path="client" label="Client">
          <n-input v-model:value="formValue.client" placeholder="Nom du client" />
        </n-form-item>
        <n-form-item path="amount" label="Montant">
          <n-input-number v-model:value="formValue.amount" placeholder="0.00" :min="0" />
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
  number: '',
  client: '',
  amount: 0,
  date: Date.now(),
})

const rules = {
  number: { required: true, message: 'Numéro requis', trigger: 'blur' },
  client: { required: true, message: 'Client requis', trigger: 'blur' },
  amount: { required: true, type: 'number', message: 'Montant requis', trigger: 'blur' },
}

const columns: DataTableColumns = [
  { title: 'Numéro', key: 'number' },
  { title: 'Client', key: 'client' },
  { title: 'Montant', key: 'amount', render: (row: any) => `${row.amount}€` },
  { title: 'Statut', key: 'status' },
  { title: 'Date', key: 'date' },
]

async function handleCreate() {
  const result = await accountingStore.createInvoice({
    number: formValue.value.number,
    client: formValue.value.client,
    amount: formValue.value.amount,
    status: 'draft',
    date: new Date(formValue.value.date).toISOString(),
  })

  if (result.success) {
    message.success('Facture créée')
    showModal.value = false
    accountingStore.fetchInvoices()
  } else {
    message.error('Erreur lors de la création')
  }
}

onMounted(() => {
  accountingStore.fetchInvoices()
})
</script>
