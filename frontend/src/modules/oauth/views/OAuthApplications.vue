<script setup lang="ts">
import { ref, computed } from 'vue'
import { useOAuth } from '../composables/useOAuth'
import { Plus, Key, Globe, CheckCircle, XCircle, Edit, Trash2, Copy } from 'lucide-vue-next'

const { applications, createApplication, deleteApplication, updateApplication } = useOAuth()

const safeApplications = computed(() => {
  if (!applications.value || !Array.isArray(applications.value)) {
    return []
  }
  return applications.value
})

const showCreateModal = ref(false)
const newApp = ref({
  name: '',
  redirectUris: '',
  scopes: [] as string[]
})

const availableScopes = [
  'read:profile',
  'read:companies',
  'write:companies',
  'read:invoices',
  'write:invoices',
  'read:accounting',
  'write:accounting',
  'read:employees',
  'write:employees'
]

function handleCreate() {
  const redirectUris = newApp.value.redirectUris.split(',').map((uri) => uri.trim()).filter((uri) => uri)

  createApplication.value({
    name: newApp.value.name,
    clientId: `com.compta.${Date.now()}`,
    clientSecret: `sk_live_${Math.random().toString(36).substring(2, 15)}`,
    redirectUris,
    scopes: newApp.value.scopes,
    active: true
  })

  newApp.value = { name: '', redirectUris: '', scopes: [] }
  showCreateModal.value = false
}

function toggleScope(scope: string) {
  const index = newApp.value.scopes.indexOf(scope)
  if (index === -1) {
    newApp.value.scopes.push(scope)
  } else {
    newApp.value.scopes.splice(index, 1)
  }
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
}

function toggleActive(id: string) {
  const app = safeApplications.value.find((a) => a.id === id)
  if (app) {
    updateApplication.value(id, { active: !app.active })
  }
}
</script>

<template>
  <div v-if="!applications" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des applications...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Applications OAuth</h1>
        <p class="text-gray-600 mt-1">Gérez vos applications OAuth</p>
      </div>
      <button
        @click="showCreateModal = true"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Plus :size="18" />
        Nouvelle application
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="app in safeApplications"
        :key="app.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 p-6"
      >
        <div class="flex items-start justify-between mb-4">
          <div>
            <div class="flex items-center gap-2">
              <h3 class="font-semibold text-gray-900">{{ app.name }}</h3>
              <CheckCircle v-if="app.active" :size="16" class="text-green-500" />
              <XCircle v-else :size="16" class="text-gray-400" />
            </div>
            <p class="text-xs text-gray-500 mt-1">ID: {{ app.clientId }}</p>
          </div>
          <div class="flex gap-2">
            <button
              @click="toggleActive(app.id)"
              :class="[
                'p-2 rounded-lg transition-colors',
                app.active ? 'hover:bg-green-100' : 'hover:bg-gray-100'
              ]"
              :title="app.active ? 'Désactiver' : 'Activer'"
            >
              <CheckCircle v-if="app.active" :size="16" class="text-green-500" />
              <XCircle v-else :size="16" class="text-gray-400" />
            </button>
            <button class="p-2 rounded-lg hover:bg-gray-100 transition-colors" title="Modifier">
              <Edit :size="16" class="text-gray-500" />
            </button>
            <button
              @click="deleteApplication(app.id)"
              class="p-2 rounded-lg hover:bg-red-100 transition-colors"
              title="Supprimer"
            >
              <Trash2 :size="16" class="text-gray-500 hover:text-red-600" />
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <p class="text-xs text-gray-500 font-medium uppercase flex items-center gap-1">
              <Key :size="12" />
              Client Secret
            </p>
            <div class="flex items-center gap-2 mt-1">
              <code class="text-sm text-gray-600 bg-gray-100 px-2 py-1 rounded">{{ app.clientSecret.substring(0, 20) }}...</code>
              <button
                @click="copyToClipboard(app.clientSecret)"
                class="p-1 hover:bg-gray-200 rounded transition-colors"
              >
                <Copy :size="14" class="text-gray-500" />
              </button>
            </div>
          </div>

          <div>
            <p class="text-xs text-gray-500 font-medium uppercase flex items-center gap-1">
              <Globe :size="12" />
              URLs de redirection
            </p>
            <div class="mt-1 space-y-1">
              <p
                v-for="uri in app.redirectUris"
                :key="uri"
                class="text-sm text-gray-600 truncate"
              >
                {{ uri }}
              </p>
            </div>
          </div>

          <div>
            <p class="text-xs text-gray-500 font-medium uppercase">Scopes</p>
            <div class="flex flex-wrap gap-1 mt-1">
              <span
                v-for="scope in app.scopes"
                :key="scope"
                class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded text-xs"
              >
                {{ scope }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
    >
      <div class="bg-white rounded-xl shadow-lg max-w-lg w-full p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-6">Nouvelle application OAuth</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nom de l'application</label>
            <input
              v-model="newApp.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              placeholder="ex: Mobile App"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">URLs de redirection</label>
            <input
              v-model="newApp.redirectUris"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              placeholder="ex: com.app://oauth/callback, http://localhost:3000/callback"
            />
            <p class="text-xs text-gray-500 mt-1">Séparez les URLs par des virgules</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Scopes</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="scope in availableScopes"
                :key="scope"
                @click="toggleScope(scope)"
                :class="[
                  'px-3 py-1.5 rounded-lg text-sm font-medium transition-colors',
                  newApp.scopes.includes(scope)
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                ]"
              >
                {{ scope }}
              </button>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            @click="showCreateModal = false"
            class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            Annuler
          </button>
          <button
            @click="handleCreate"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Créer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
