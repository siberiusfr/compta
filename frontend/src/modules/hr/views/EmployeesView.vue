<template>
  <div>
    <n-page-header title="Employés" @back="router.back">
      <template #extra>
        <n-button type="primary" @click="showModal = true">Ajouter un employé</n-button>
      </template>
    </n-page-header>

    <n-card style="margin-top: 24px">
      <n-data-table
        :columns="columns"
        :data="hrStore.employees"
        :loading="hrStore.loading"
        :pagination="{ pageSize: 10 }"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="dialog" title="Nouvel employé" style="width: 600px">
      <n-form ref="formRef" :model="formValue" :rules="rules">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item path="firstName" label="Prénom">
              <n-input v-model:value="formValue.firstName" placeholder="Prénom" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="lastName" label="Nom">
              <n-input v-model:value="formValue.lastName" placeholder="Nom" />
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item path="email" label="Email">
              <n-input v-model:value="formValue.email" placeholder="email@example.com" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="position" label="Poste">
              <n-input v-model:value="formValue.position" placeholder="Poste" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="department" label="Département">
              <n-input v-model:value="formValue.department" placeholder="Département" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="salary" label="Salaire">
              <n-input-number v-model:value="formValue.salary" placeholder="0.00" :min="0" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="hireDate" label="Date d'embauche">
              <n-date-picker v-model:value="formValue.hireDate" type="date" />
            </n-form-item>
          </n-gi>
        </n-grid>
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
import { useHRStore } from '@stores'

const router = useRouter()
const message = useMessage()
const hrStore = useHRStore()

const showModal = ref(false)
const formRef = ref(null)

const formValue = ref({
  firstName: '',
  lastName: '',
  email: '',
  position: '',
  department: '',
  salary: 0,
  hireDate: Date.now(),
})

const rules = {
  firstName: { required: true, message: 'Prénom requis', trigger: 'blur' },
  lastName: { required: true, message: 'Nom requis', trigger: 'blur' },
  email: {
    required: true,
    type: 'email',
    message: 'Email valide requis',
    trigger: 'blur',
  },
  position: { required: true, message: 'Poste requis', trigger: 'blur' },
  department: { required: true, message: 'Département requis', trigger: 'blur' },
  salary: { required: true, type: 'number', message: 'Salaire requis', trigger: 'blur' },
}

const columns: DataTableColumns = [
  { title: 'Prénom', key: 'firstName' },
  { title: 'Nom', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Poste', key: 'position' },
  { title: 'Département', key: 'department' },
  { title: 'Salaire', key: 'salary', render: (row: any) => `${row.salary}€` },
]

async function handleCreate() {
  const result = await hrStore.createEmployee({
    firstName: formValue.value.firstName,
    lastName: formValue.value.lastName,
    email: formValue.value.email,
    position: formValue.value.position,
    department: formValue.value.department,
    salary: formValue.value.salary,
    hireDate: new Date(formValue.value.hireDate).toISOString(),
  })

  if (result.success) {
    message.success('Employé créé')
    showModal.value = false
    hrStore.fetchEmployees()
  } else {
    message.error('Erreur lors de la création')
  }
}

onMounted(() => {
  hrStore.fetchEmployees()
})
</script>
