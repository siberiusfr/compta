<template>
  <n-space vertical :size="24" :style="{ maxWidth: '1200px' }">
    <n-page-header title="Gestion des documents" />

    <n-grid :cols="2" :x-gap="16">
      <n-gi>
        <n-card title="Total de documents" hoverable>
          <n-statistic :value="documentsStore.documentCount" />
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="Actions" hoverable>
          <n-space>
            <n-button type="primary" @click="router.push({ name: 'documents-upload' })">
              Téléverser un document
            </n-button>
          </n-space>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="Documents récents">
      <n-list bordered>
        <n-list-item v-for="doc in documentsStore.recentDocuments" :key="doc.id">
          <n-thing :title="doc.name">
            <template #description>
              {{ doc.category }} • {{ formatDate(doc.uploadedAt) }} • {{ formatSize(doc.size) }}
            </template>
          </n-thing>
        </n-list-item>
        <n-empty v-if="documentsStore.recentDocuments.length === 0" description="Aucun document" />
      </n-list>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpace,
  NPageHeader,
  NGrid,
  NGi,
  NCard,
  NStatistic,
  NButton,
  NList,
  NListItem,
  NThing,
  NEmpty,
} from 'naive-ui'
import { useDocumentsStore } from '@/modules/documents/stores/documentsStore'

const router = useRouter()
const documentsStore = useDocumentsStore()

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('fr-FR')
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

onMounted(() => {
  documentsStore.fetchDocuments()
})
</script>
