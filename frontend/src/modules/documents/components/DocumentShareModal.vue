<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { X, Share2, Loader2, User, Trash2, Calendar } from 'lucide-vue-next'
import { useDocument } from '../composables/useDocumentsApi'
import { useDocuments } from '../composables/useDocuments'
import type { DocumentShareRequest, DocumentShareResponse } from '@/api/documents'

const props = defineProps<{
  open: boolean
  documentId: number | null
}>()

const emit = defineEmits<{
  close: []
}>()

const { shareDocument, revokeShare, formatDate } = useDocuments()

const documentIdRef = computed(() => props.documentId ?? 0)
const { document, shares, isLoading } = useDocument(() => documentIdRef.value)

const userId = ref('')
const permission = ref<'READ' | 'WRITE'>('READ')
const expiresAt = ref('')
const isSharing = ref(false)
const isRevoking = ref<number | null>(null)

watch(
  () => props.open,
  (open) => {
    if (!open) {
      resetForm()
    }
  }
)

function resetForm() {
  userId.value = ''
  permission.value = 'READ'
  expiresAt.value = ''
}

const canSubmit = computed(() => userId.value.trim())

async function handleShare() {
  if (!props.documentId || !userId.value.trim()) return

  const shareData: DocumentShareRequest = {
    sharedWith: userId.value.trim(),
    permission: permission.value,
    ...(expiresAt.value ? { expiresAt: expiresAt.value } : {}),
  }

  isSharing.value = true
  try {
    await shareDocument(props.documentId, shareData)
    resetForm()
  } catch (error) {
    console.error('Erreur lors du partage:', error)
  } finally {
    isSharing.value = false
  }
}

async function handleRevoke(shareId: number) {
  isRevoking.value = shareId
  try {
    await revokeShare(shareId)
  } catch (error) {
    console.error('Erreur lors de la revocation:', error)
  } finally {
    isRevoking.value = null
  }
}

function getPermissionLabel(perm?: string): string {
  switch (perm) {
    case 'READ':
      return 'Lecture'
    case 'WRITE':
      return 'Ecriture'
    default:
      return perm || '-'
  }
}

function getMinDate(): string {
  return new Date().toISOString().split('T')[0] ?? ''
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
      <div class="relative w-full max-w-md rounded-xl bg-background shadow-xl">
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <h2 class="text-lg font-semibold flex items-center gap-2">
            <Share2 class="h-5 w-5" />
            Partager le document
          </h2>
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
          v-else
          class="p-6"
        >
          <!-- Document info -->
          <div
            v-if="document"
            class="mb-6 p-3 bg-muted/50 rounded-lg"
          >
            <p class="font-medium">{{ document.title }}</p>
            <p class="text-sm text-muted-foreground">{{ document.fileName }}</p>
          </div>

          <!-- Share form -->
          <form
            class="space-y-4"
            @submit.prevent="handleShare"
          >
            <div>
              <label class="block text-sm font-medium mb-1">
                <User class="h-4 w-4 inline mr-1" />
                Partager avec *
              </label>
              <input
                v-model="userId"
                type="text"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="ID utilisateur ou email"
                required
              />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">Permission</label>
              <select
                v-model="permission"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="READ">Lecture seule</option>
                <option value="WRITE">Lecture et ecriture</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">
                <Calendar class="h-4 w-4 inline mr-1" />
                Date d'expiration (optionnel)
              </label>
              <input
                v-model="expiresAt"
                type="date"
                :min="getMinDate()"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>

            <Button
              type="submit"
              class="w-full"
              :disabled="!canSubmit || isSharing"
            >
              <Loader2
                v-if="isSharing"
                class="h-4 w-4 mr-2 animate-spin"
              />
              <Share2
                v-else
                class="h-4 w-4 mr-2"
              />
              Partager
            </Button>
          </form>

          <!-- Existing shares -->
          <div
            v-if="shares && shares.length > 0"
            class="mt-6"
          >
            <h3 class="text-sm font-medium mb-3">Partages existants</h3>
            <div class="space-y-2 max-h-48 overflow-auto">
              <div
                v-for="share in shares as DocumentShareResponse[]"
                :key="share.id"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div class="flex items-center gap-2">
                  <div class="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
                    <User class="h-4 w-4" />
                  </div>
                  <div>
                    <p class="text-sm font-medium">{{ share.sharedWith }}</p>
                    <p class="text-xs text-muted-foreground">
                      {{ getPermissionLabel(share.permission) }}
                      <span v-if="share.expiresAt"> - {{ formatDate(share.expiresAt) }}</span>
                    </p>
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  class="text-destructive hover:text-destructive"
                  :disabled="isRevoking === share.id"
                  @click="handleRevoke(share.id!)"
                >
                  <Loader2
                    v-if="isRevoking === share.id"
                    class="h-4 w-4 animate-spin"
                  />
                  <Trash2
                    v-else
                    class="h-4 w-4"
                  />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
