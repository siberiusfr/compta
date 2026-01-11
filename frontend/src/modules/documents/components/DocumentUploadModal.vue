<script setup lang="ts">
import { ref, computed } from 'vue'
import { Button } from '@/components/ui/button'
import { X, Upload, Loader2, FileText, Folder } from 'lucide-vue-next'
import { useDocuments } from '../composables/useDocuments'
import type { DocumentUploadRequest } from '@/api/documents'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const { categories, uploadDocument, isUploading } = useDocuments()

const file = ref<File | null>(null)
const title = ref('')
const description = ref('')
const categoryId = ref<number | undefined>()
const isPublic = ref(false)
const tags = ref<string[]>([])
const tagInput = ref('')
const dragOver = ref(false)

const fileInputRef = ref<HTMLInputElement | null>(null)

const canSubmit = computed(() => file.value && title.value.trim())

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files?.[0]) {
    file.value = input.files[0]
    if (!title.value) {
      title.value = input.files[0].name.replace(/\.[^/.]+$/, '')
    }
  }
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  dragOver.value = false
  const droppedFile = event.dataTransfer?.files?.[0]
  if (droppedFile) {
    file.value = droppedFile
    if (!title.value) {
      title.value = droppedFile.name.replace(/\.[^/.]+$/, '')
    }
  }
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
  dragOver.value = true
}

function handleDragLeave() {
  dragOver.value = false
}

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !tags.value.includes(tag)) {
    tags.value.push(tag)
    tagInput.value = ''
  }
}

function removeTag(tag: string) {
  tags.value = tags.value.filter((t) => t !== tag)
}

async function handleSubmit() {
  if (!file.value || !title.value.trim()) return

  const metadata: DocumentUploadRequest = {
    title: title.value.trim(),
    description: description.value.trim() || undefined,
    categoryId: categoryId.value,
    isPublic: isPublic.value,
    tags: tags.value.length > 0 ? tags.value : undefined,
  }

  try {
    await uploadDocument(file.value, metadata)
    resetForm()
    emit('success')
    emit('close')
  } catch (error) {
    console.error("Erreur lors de l'upload:", error)
  }
}

function resetForm() {
  file.value = null
  title.value = ''
  description.value = ''
  categoryId.value = undefined
  isPublic.value = false
  tags.value = []
  tagInput.value = ''
}

function handleClose() {
  resetForm()
  emit('close')
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-black/50"
        @click="handleClose"
      />

      <!-- Modal -->
      <div class="relative w-full max-w-lg rounded-xl bg-background p-6 shadow-xl">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold flex items-center gap-2">
            <Upload class="h-5 w-5" />
            Nouveau document
          </h2>
          <Button
            variant="ghost"
            size="icon"
            @click="handleClose"
          >
            <X class="h-4 w-4" />
          </Button>
        </div>

        <form
          class="space-y-4"
          @submit.prevent="handleSubmit"
        >
          <!-- Drop zone -->
          <div
            class="border-2 border-dashed rounded-lg p-6 text-center transition-colors"
            :class="[
              dragOver
                ? 'border-primary bg-primary/5'
                : 'border-muted-foreground/25 hover:border-primary/50',
            ]"
            @drop="handleDrop"
            @dragover="handleDragOver"
            @dragleave="handleDragLeave"
          >
            <input
              ref="fileInputRef"
              type="file"
              class="hidden"
              @change="handleFileSelect"
            />

            <div v-if="file">
              <FileText class="h-10 w-10 mx-auto text-primary mb-2" />
              <p class="font-medium">{{ file.name }}</p>
              <p class="text-sm text-muted-foreground">{{ formatFileSize(file.size) }}</p>
              <Button
                type="button"
                variant="link"
                size="sm"
                class="mt-2"
                @click="file = null"
              >
                Changer de fichier
              </Button>
            </div>
            <div v-else>
              <Upload class="h-10 w-10 mx-auto text-muted-foreground mb-2" />
              <p class="text-muted-foreground mb-2">
                Glissez un fichier ici ou
                <button
                  type="button"
                  class="text-primary underline"
                  @click="fileInputRef?.click()"
                >
                  parcourez
                </button>
              </p>
              <p class="text-xs text-muted-foreground">PDF, Word, Excel, Images (max 50MB)</p>
            </div>
          </div>

          <!-- Title -->
          <div>
            <label class="block text-sm font-medium mb-1">Titre *</label>
            <input
              v-model="title"
              type="text"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              placeholder="Titre du document"
              required
            />
          </div>

          <!-- Description -->
          <div>
            <label class="block text-sm font-medium mb-1">Description</label>
            <textarea
              v-model="description"
              rows="2"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring resize-none"
              placeholder="Description optionnelle"
            />
          </div>

          <!-- Category -->
          <div>
            <label class="block text-sm font-medium mb-1">
              <Folder class="h-4 w-4 inline mr-1" />
              Categorie
            </label>
            <select
              v-model="categoryId"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option :value="undefined">Aucune categorie</option>
              <option
                v-for="category in categories"
                :key="category.id"
                :value="category.id"
              >
                {{ category.name }}
              </option>
            </select>
          </div>

          <!-- Tags -->
          <div>
            <label class="block text-sm font-medium mb-1">Tags</label>
            <div class="flex gap-2">
              <input
                v-model="tagInput"
                type="text"
                class="flex-1 px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="Ajouter un tag"
                @keydown.enter.prevent="addTag"
              />
              <Button
                type="button"
                variant="outline"
                @click="addTag"
              >
                Ajouter
              </Button>
            </div>
            <div
              v-if="tags.length > 0"
              class="flex flex-wrap gap-1 mt-2"
            >
              <span
                v-for="tag in tags"
                :key="tag"
                class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-muted text-sm"
              >
                {{ tag }}
                <button
                  type="button"
                  class="hover:text-destructive"
                  @click="removeTag(tag)"
                >
                  <X class="h-3 w-3" />
                </button>
              </span>
            </div>
          </div>

          <!-- Public -->
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              v-model="isPublic"
              type="checkbox"
              class="rounded"
            />
            <span class="text-sm">Document public</span>
          </label>

          <!-- Actions -->
          <div class="flex justify-end gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              @click="handleClose"
            >
              Annuler
            </Button>
            <Button
              type="submit"
              :disabled="!canSubmit || isUploading"
            >
              <Loader2
                v-if="isUploading"
                class="h-4 w-4 mr-2 animate-spin"
              />
              <Upload
                v-else
                class="h-4 w-4 mr-2"
              />
              Telecharger
            </Button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
