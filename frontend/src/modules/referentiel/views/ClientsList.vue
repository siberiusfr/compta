<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import {
  Users,
  Plus,
  Search,
  Edit,
  Trash2,
  CheckCircle2,
  XCircle,
  Building2,
  User,
  Mail,
  Phone,
  MapPin,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const {
  companyId,
  useGetAllClients,
  useDeleteClient,
  invalidateClients,
  getStatusColor,
  getStatusLabel,
  getTypeClientLabel,
} = useReferentiel()

const searchQuery = ref('')
const deleteConfirmId = ref<number | null>(null)

const { data: clients, isLoading } = useGetAllClients(companyId)

const deleteMutation = useDeleteClient({
  mutation: {
    onSuccess: () => {
      invalidateClients()
      deleteConfirmId.value = null
    },
  },
})

const filteredClients = computed(() => {
  if (!clients.value) return []
  if (!searchQuery.value) return clients.value
  const query = searchQuery.value.toLowerCase()
  return clients.value.filter(
    (c) =>
      c.code?.toLowerCase().includes(query) ||
      c.raisonSociale?.toLowerCase().includes(query) ||
      c.email?.toLowerCase().includes(query) ||
      c.ville?.toLowerCase().includes(query)
  )
})

function handleCreate() {
  router.push({ name: 'referentiel-clients-create' })
}

function handleEdit(id: number) {
  router.push({ name: 'referentiel-clients-edit', params: { id } })
}

function handleDelete(id: number) {
  deleteMutation.mutate({ companyId: companyId.value, id })
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Users class="h-6 w-6" />
          Clients
        </h1>
        <p class="text-muted-foreground">Gérez votre portefeuille clients</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="h-4 w-4 mr-2" />
        Nouveau client
      </Button>
    </div>

    <!-- Search -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un client..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <!-- List -->
    <div
      v-else
      class="space-y-3"
    >
      <div
        v-for="client in filteredClients"
        :key="client.id"
        class="rounded-xl border bg-card p-4 hover:bg-accent/50 transition-colors"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 flex-1">
            <div
              :class="
                cn(
                  'p-2 rounded-lg',
                  client.typeClient === 'ENTREPRISE'
                    ? 'bg-blue-100 dark:bg-blue-900/30'
                    : 'bg-primary/10'
                )
              "
            >
              <component
                :is="client.typeClient === 'ENTREPRISE' ? Building2 : User"
                :class="
                  cn(
                    'h-5 w-5',
                    client.typeClient === 'ENTREPRISE'
                      ? 'text-blue-600 dark:text-blue-400'
                      : 'text-primary'
                  )
                "
              />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <span class="font-mono font-medium text-sm">{{ client.code }}</span>
                <span class="font-medium truncate">{{ client.raisonSociale }}</span>
                <span class="text-xs px-2 py-1 rounded-full bg-secondary text-secondary-foreground">
                  {{ getTypeClientLabel(client.typeClient) }}
                </span>
                <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(client.actif))">
                  {{ getStatusLabel(client.actif) }}
                </span>
              </div>
              <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground flex-wrap">
                <span
                  v-if="client.email"
                  class="flex items-center gap-1"
                >
                  <Mail class="h-3 w-3" />
                  {{ client.email }}
                </span>
                <span
                  v-if="client.telephone"
                  class="flex items-center gap-1"
                >
                  <Phone class="h-3 w-3" />
                  {{ client.telephone }}
                </span>
                <span
                  v-if="client.ville"
                  class="flex items-center gap-1"
                >
                  <MapPin class="h-3 w-3" />
                  {{ client.ville }}
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon-sm"
              @click="handleEdit(client.id!)"
            >
              <Edit class="h-4 w-4" />
            </Button>
            <Button
              v-if="deleteConfirmId !== client.id"
              variant="ghost"
              size="icon-sm"
              class="text-destructive hover:text-destructive"
              @click="deleteConfirmId = client.id!"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
            <div
              v-else
              class="flex items-center gap-1"
            >
              <Button
                variant="ghost"
                size="icon-sm"
                class="text-destructive hover:text-destructive"
                @click="handleDelete(client.id!)"
                :disabled="deleteMutation.isPending.value"
              >
                <CheckCircle2 class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon-sm"
                @click="deleteConfirmId = null"
              >
                <XCircle class="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-if="!isLoading && filteredClients.length === 0"
      class="text-center py-12"
    >
      <Users class="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
      <p class="text-muted-foreground">Aucun client trouvé</p>
      <Button
        variant="outline"
        class="mt-4"
        @click="handleCreate"
      >
        <Plus class="h-4 w-4 mr-2" />
        Créer un client
      </Button>
    </div>
  </div>
</template>
