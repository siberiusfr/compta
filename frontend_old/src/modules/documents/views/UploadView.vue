<template>
  <div>
    <n-page-header title="Téléverser un document" @back="router.back" />

    <n-card style="margin-top: 24px">
      <n-form ref="formRef" :model="formValue" :rules="rules">
        <n-form-item path="category" label="Catégorie">
          <n-select
            v-model:value="formValue.category"
            :options="categoryOptions"
            placeholder="Sélectionnez une catégorie"
          />
        </n-form-item>

        <n-form-item path="file" label="Fichier">
          <n-upload
            :max="1"
            :default-upload="false"
            @change="handleFileChange"
            @remove="handleFileRemove"
          >
            <n-button>Sélectionner un fichier</n-button>
          </n-upload>
        </n-form-item>

        <n-form-item>
          <n-space>
            <n-button @click="router.back">Annuler</n-button>
            <n-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleUpload">
              Téléverser
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  useMessage,
  type FormInst,
  type FormRules,
  type UploadFileInfo,
  NPageHeader,
  NCard,
  NForm,
  NFormItem,
  NSelect,
  NUpload,
  NButton,
  NSpace,
} from 'naive-ui'
import { useDocumentsStore } from '@/modules/documents/stores/documentsStore'

const router = useRouter()
const message = useMessage()
const documentsStore = useDocumentsStore()

const formRef = ref<FormInst | null>(null)
const uploading = ref(false)
const selectedFile = ref<File | null>(null)

const formValue = ref({
  category: null,
})

const categoryOptions = [
  { label: 'Factures', value: 'invoices' },
  { label: 'Contrats', value: 'contracts' },
  { label: 'Rapports', value: 'reports' },
  { label: 'RH', value: 'hr' },
  { label: 'Autres', value: 'other' },
]

const rules = {
  category: { required: true, message: 'Catégorie requise', trigger: 'change' },
}

function handleFileChange(options: { fileList: UploadFileInfo[] }) {
  if (options.fileList.length > 0) {
    selectedFile.value = options.fileList[0].file as File
  }
}

function handleFileRemove() {
  selectedFile.value = null
}

async function handleUpload() {
  if (!formRef.value) return
  
  formRef.value.validate(async (errors) => {
    if (!errors) {
      if (!selectedFile.value || !formValue.value.category) {
        message.warning('Veuillez sélectionner un fichier et une catégorie')
        return
      }

      uploading.value = true

      try {
        const result = await documentsStore.uploadDocument(
          selectedFile.value,
          formValue.value.category
        )

        if (result.success) {
          message.success('Document téléversé avec succès')
          router.push({ name: 'documents' })
        } else {
          message.error('Erreur lors du téléversement')
        }
      } catch (error) {
        console.error('Upload error:', error)
        message.error('Erreur lors du téléversement')
      } finally {
        uploading.value = false
      }
    }
  })
}
</script>
