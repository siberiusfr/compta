<script setup lang="ts">
import { Button } from '@/components/ui/button'
import {
  Share2,
  FileText,
  Calendar,
  User,
  Download,
  Eye,
  Loader2,
  Lock,
  Edit,
} from 'lucide-vue-next'
import { useDocuments } from '../composables/useDocuments'
import { useDocumentsStore } from '../stores/documentsStore'
import { storeToRefs } from 'pinia'
import DocumentDetailModal from '../components/DocumentDetailModal.vue'
import DocumentShareModal from '../components/DocumentShareModal.vue'
import type { DocumentShareResponse } from '@/api/documents'

const store = useDocumentsStore()
const { selectedDocumentId, isDetailModalOpen, isShareModalOpen } = storeToRefs(store)

const { sharedWithMe, isLoading, formatDate, formatDateTime } = useDocuments()

function getPermissionIcon(permission?: string) {
  return permission === 'WRITE' ? Edit : Eye
}

function getPermissionLabel(permission?: string): string {
  switch (permission) {
    case 'READ':
      return 'Lecture'
    case 'WRITE':
      return 'Ecriture'
    default:
      return permission || '-'
  }
}

function handleView(share: DocumentShareResponse) {
  if (share.documentId) {
    store.selectDocument(share.documentId)
  }
}

function handleDownload(share: DocumentShareResponse) {
  // TODO: Implement download from share
  console.log('Download:', share.documentId)
}

function handleDetailClose() {
  store.closeDetailModal()
}

function handleShareFromDetail(id: number) {
  store.closeDetailModal()
  store.openShareModal(id)
}

function handleDeleteFromDetail(id: number) {
  console.log('Delete:', id)
  store.closeDetailModal()
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Share2 class="h-6 w-6" />
          Partages avec moi
        </h1>
        <p class="text-muted-foreground">Documents partages par d'autres utilisateurs</p>
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="flex items-center justify-center py-12 text-muted-foreground"
    >
      <Loader2 class="h-6 w-6 animate-spin mr-2" />
      Chargement...
    </div>

    <!-- Empty state -->
    <div
      v-else-if="!sharedWithMe || sharedWithMe.length === 0"
      class="text-center py-12"
    >
      <Lock class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun document partage</p>
      <p class="text-muted-foreground">Les documents partages avec vous apparaitront ici</p>
    </div>

    <!-- Shared documents list -->
    <div
      v-else
      class="rounded-xl border bg-card overflow-hidden"
    >
      <table class="w-full">
        <thead class="bg-muted/50">
          <tr>
            <th class="text-left p-4 font-medium">Document</th>
            <th class="text-left p-4 font-medium">Partage par</th>
            <th class="text-left p-4 font-medium">Permission</th>
            <th class="text-left p-4 font-medium">Expire le</th>
            <th class="text-left p-4 font-medium">Statut</th>
            <th class="text-right p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr
            v-for="share in sharedWithMe as DocumentShareResponse[]"
            :key="share.id"
            class="hover:bg-muted/30 transition-colors"
          >
            <td class="p-4">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <FileText class="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p class="font-medium">{{ share.documentTitle || 'Document' }}</p>
                  <p class="text-xs text-muted-foreground">
                    Partage le {{ formatDateTime(share.createdAt) }}
                  </p>
                </div>
              </div>
            </td>
            <td class="p-4">
              <div class="flex items-center gap-2">
                <User class="h-4 w-4 text-muted-foreground" />
                <span class="text-sm">{{ share.createdBy || '-' }}</span>
              </div>
            </td>
            <td class="p-4">
              <span class="inline-flex items-center gap-1 text-sm">
                <component
                  :is="getPermissionIcon(share.permission)"
                  class="h-4 w-4"
                />
                {{ getPermissionLabel(share.permission) }}
              </span>
            </td>
            <td class="p-4">
              <div
                v-if="share.expiresAt"
                class="flex items-center gap-1 text-sm"
              >
                <Calendar class="h-4 w-4 text-muted-foreground" />
                {{ formatDate(share.expiresAt) }}
              </div>
              <span
                v-else
                class="text-muted-foreground"
                >Permanent</span
              >
            </td>
            <td class="p-4">
              <span
                class="text-xs px-2 py-1 rounded-full"
                :class="
                  share.isActive
                    ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                    : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                "
              >
                {{ share.isActive ? 'Actif' : 'Expire' }}
              </span>
            </td>
            <td class="p-4">
              <div class="flex items-center justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Voir"
                  :disabled="!share.isActive"
                  @click="handleView(share)"
                >
                  <Eye class="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Telecharger"
                  :disabled="!share.isActive"
                  @click="handleDownload(share)"
                >
                  <Download class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modals -->
    <DocumentDetailModal
      :open="isDetailModalOpen"
      :document-id="selectedDocumentId"
      @close="handleDetailClose"
      @share="handleShareFromDetail"
      @delete="handleDeleteFromDetail"
    />

    <DocumentShareModal
      :open="isShareModalOpen"
      :document-id="selectedDocumentId"
      @close="store.closeShareModal"
    />
  </div>
</template>
