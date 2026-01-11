<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { Button } from '@/components/ui/button'
import {
  X,
  Download,
  Share2,
  Trash2,
  FileText,
  Calendar,
  User,
  Tag,
  Folder,
  Clock,
  Upload,
  Loader2,
  Lock,
  Globe,
} from 'lucide-vue-next'
import { useDocument } from '../composables/useDocumentsApi'
import { useDocuments } from '../composables/useDocuments'
import type { DocumentVersionResponse } from '@/api/documents'

const props = defineProps<{
  open: boolean
  documentId: number | null
}>()

const emit = defineEmits<{
  close: []
  share: [id: number]
  delete: [id: number]
}>()

const { formatFileSize, formatDate, formatDateTime } = useDocuments()

const activeTab = ref<'details' | 'versions' | 'shares'>('details')
const versionFile = ref<File | null>(null)
const versionDescription = ref('')
const versionInputRef = ref<HTMLInputElement | null>(null)

const documentIdRef = computed(() => props.documentId ?? 0)

const { document, versions, shares, isLoading, download, uploadNewVersion, isUploadingVersion } =
  useDocument(() => documentIdRef.value)

watch(
  () => props.open,
  (open) => {
    if (!open) {
      activeTab.value = 'details'
      versionFile.value = null
      versionDescription.value = ''
    }
  }
)

function handleDownload() {
  download()
}

function handleShare() {
  if (props.documentId) {
    emit('share', props.documentId)
  }
}

function handleDelete() {
  if (props.documentId) {
    emit('delete', props.documentId)
  }
}

function handleVersionFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files?.[0]) {
    versionFile.value = input.files[0]
  }
}

async function handleUploadVersion() {
  if (!versionFile.value) return

  try {
    await uploadNewVersion(versionFile.value, versionDescription.value || undefined)
    versionFile.value = null
    versionDescription.value = ''
  } catch (error) {
    console.error("Erreur lors de l'upload de la version:", error)
  }
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
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open && documentId"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-black/50"
        @click="emit('close')"
      />

      <!-- Modal -->
      <div
        class="relative w-full max-w-2xl max-h-[90vh] overflow-hidden rounded-xl bg-background shadow-xl flex flex-col"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
              <FileText class="h-5 w-5 text-primary" />
            </div>
            <div v-if="document">
              <h2 class="text-lg font-semibold">{{ document.title }}</h2>
              <p class="text-sm text-muted-foreground">{{ document.fileName }}</p>
            </div>
            <div
              v-else
              class="animate-pulse"
            >
              <div class="h-5 w-48 bg-muted rounded" />
              <div class="h-4 w-32 bg-muted rounded mt-1" />
            </div>
          </div>
          <Button
            variant="ghost"
            size="icon"
            @click="emit('close')"
          >
            <X class="h-4 w-4" />
          </Button>
        </div>

        <!-- Loading -->
        <div
          v-if="isLoading"
          class="flex items-center justify-center py-12"
        >
          <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>

        <!-- Content -->
        <div
          v-else-if="document"
          class="flex-1 overflow-auto"
        >
          <!-- Tabs -->
          <div class="flex border-b px-6">
            <button
              class="px-4 py-3 text-sm font-medium border-b-2 transition-colors"
              :class="
                activeTab === 'details'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              "
              @click="activeTab = 'details'"
            >
              Details
            </button>
            <button
              class="px-4 py-3 text-sm font-medium border-b-2 transition-colors"
              :class="
                activeTab === 'versions'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              "
              @click="activeTab = 'versions'"
            >
              Versions ({{ versions?.length ?? 0 }})
            </button>
            <button
              class="px-4 py-3 text-sm font-medium border-b-2 transition-colors"
              :class="
                activeTab === 'shares'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              "
              @click="activeTab = 'shares'"
            >
              Partages ({{ shares?.length ?? 0 }})
            </button>
          </div>

          <!-- Tab: Details -->
          <div
            v-if="activeTab === 'details'"
            class="p-6 space-y-4"
          >
            <!-- Description -->
            <div v-if="document.description">
              <h3 class="text-sm font-medium mb-1">Description</h3>
              <p class="text-muted-foreground">{{ document.description }}</p>
            </div>

            <!-- Metadata grid -->
            <div class="grid grid-cols-2 gap-4">
              <div class="flex items-center gap-2">
                <Folder class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-xs text-muted-foreground">Categorie</p>
                  <p class="font-medium">{{ document.categoryName || 'Non classe' }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <component
                  :is="document.isPublic ? Globe : Lock"
                  class="h-4 w-4 text-muted-foreground"
                />
                <div>
                  <p class="text-xs text-muted-foreground">Visibilite</p>
                  <p class="font-medium">{{ document.isPublic ? 'Public' : 'Prive' }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <FileText class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-xs text-muted-foreground">Taille</p>
                  <p class="font-medium">{{ formatFileSize(document.fileSize) }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <Clock class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-xs text-muted-foreground">Version</p>
                  <p class="font-medium">v{{ document.version ?? 1 }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <Calendar class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-xs text-muted-foreground">Cree le</p>
                  <p class="font-medium">{{ formatDateTime(document.createdAt) }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <Calendar class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-xs text-muted-foreground">Modifie le</p>
                  <p class="font-medium">{{ formatDateTime(document.updatedAt) }}</p>
                </div>
              </div>
            </div>

            <!-- Tags -->
            <div v-if="document.tags && document.tags.length > 0">
              <h3 class="text-sm font-medium mb-2 flex items-center gap-1">
                <Tag class="h-4 w-4" />
                Tags
              </h3>
              <div class="flex flex-wrap gap-1">
                <span
                  v-for="tag in document.tags"
                  :key="tag.id"
                  class="px-2 py-1 rounded-full bg-muted text-sm"
                >
                  {{ tag.name }}
                </span>
              </div>
            </div>
          </div>

          <!-- Tab: Versions -->
          <div
            v-else-if="activeTab === 'versions'"
            class="p-6"
          >
            <!-- Upload new version -->
            <div class="mb-6 p-4 border rounded-lg bg-muted/30">
              <h3 class="text-sm font-medium mb-3">Nouvelle version</h3>
              <input
                ref="versionInputRef"
                type="file"
                class="hidden"
                @change="handleVersionFileSelect"
              />
              <div class="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  @click="versionInputRef?.click()"
                >
                  <Upload class="h-4 w-4 mr-2" />
                  {{ versionFile ? versionFile.name : 'Choisir un fichier' }}
                </Button>
              </div>
              <div
                v-if="versionFile"
                class="mt-3 space-y-2"
              >
                <input
                  v-model="versionDescription"
                  type="text"
                  class="w-full px-3 py-2 rounded-lg border bg-background text-sm"
                  placeholder="Description des changements (optionnel)"
                />
                <Button
                  size="sm"
                  :disabled="isUploadingVersion"
                  @click="handleUploadVersion"
                >
                  <Loader2
                    v-if="isUploadingVersion"
                    class="h-4 w-4 mr-2 animate-spin"
                  />
                  Uploader la version
                </Button>
              </div>
            </div>

            <!-- Version list -->
            <div class="space-y-3">
              <div
                v-for="version in versions as DocumentVersionResponse[] | undefined"
                :key="version.id"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div class="flex items-center gap-3">
                  <div
                    class="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-sm font-medium"
                  >
                    v{{ version.versionNumber }}
                  </div>
                  <div>
                    <p class="font-medium text-sm">{{ version.fileName }}</p>
                    <p class="text-xs text-muted-foreground">
                      {{ formatFileSize(version.fileSize) }} -
                      {{ formatDateTime(version.createdAt) }}
                    </p>
                    <p
                      v-if="version.changeDescription"
                      class="text-xs text-muted-foreground mt-1"
                    >
                      {{ version.changeDescription }}
                    </p>
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Telecharger cette version"
                >
                  <Download class="h-4 w-4" />
                </Button>
              </div>

              <p
                v-if="!versions || versions.length === 0"
                class="text-center text-muted-foreground py-4"
              >
                Aucune version disponible
              </p>
            </div>
          </div>

          <!-- Tab: Shares -->
          <div
            v-else-if="activeTab === 'shares'"
            class="p-6"
          >
            <div class="space-y-3">
              <div
                v-for="share in shares"
                :key="share.id"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
                    <User class="h-4 w-4" />
                  </div>
                  <div>
                    <p class="font-medium text-sm">{{ share.sharedWith }}</p>
                    <p class="text-xs text-muted-foreground">
                      {{ getPermissionLabel(share.permission) }}
                      <span v-if="share.expiresAt">
                        - Expire le {{ formatDate(share.expiresAt) }}</span
                      >
                    </p>
                  </div>
                </div>
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
              </div>

              <p
                v-if="!shares || shares.length === 0"
                class="text-center text-muted-foreground py-4"
              >
                Ce document n'est partage avec personne
              </p>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between p-6 border-t bg-muted/30">
          <Button
            variant="destructive"
            size="sm"
            @click="handleDelete"
          >
            <Trash2 class="h-4 w-4 mr-2" />
            Supprimer
          </Button>
          <div class="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              @click="handleShare"
            >
              <Share2 class="h-4 w-4 mr-2" />
              Partager
            </Button>
            <Button
              size="sm"
              @click="handleDownload"
            >
              <Download class="h-4 w-4 mr-2" />
              Telecharger
            </Button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
