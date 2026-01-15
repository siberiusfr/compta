<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import {
  Package,
  Plus,
  Search,
  Edit,
  Trash2,
  CheckCircle2,
  XCircle,
  Tag,
  Briefcase,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const {
  companyId,
  useGetAllProduits,
  useGetAllFamilles,
  useDeleteProduit,
  invalidateProduits,
  getStatusColor,
  getStatusLabel,
  getTypeArticleLabel,
  formatCurrency,
} = useReferentiel()

const searchQuery = ref('')
const deleteConfirmId = ref<number | null>(null)

const { data: produits, isLoading } = useGetAllProduits(companyId)
const { data: familles } = useGetAllFamilles(companyId)

const deleteMutation = useDeleteProduit({
  mutation: {
    onSuccess: () => {
      invalidateProduits()
      deleteConfirmId.value = null
    },
  },
})

const familleMap = computed(() => {
  const map = new Map<number, string>()
  familles.value?.forEach((f) => {
    if (f.id) map.set(f.id, f.libelle ?? f.code ?? '')
  })
  return map
})

const filteredProduits = computed(() => {
  if (!produits.value) return []
  if (!searchQuery.value) return produits.value
  const query = searchQuery.value.toLowerCase()
  return produits.value.filter(
    (p) =>
      p.reference?.toLowerCase().includes(query) ||
      p.designation?.toLowerCase().includes(query) ||
      p.description?.toLowerCase().includes(query)
  )
})

function handleCreate() {
  router.push({ name: 'referentiel-produits-create' })
}

function handleEdit(id: number) {
  router.push({ name: 'referentiel-produits-edit', params: { id } })
}

function handleDelete(id: number) {
  deleteMutation.mutate({ companyId: companyId.value, id })
}

function getFamilleName(familleId?: number): string {
  if (!familleId) return '-'
  return familleMap.value.get(familleId) ?? '-'
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Package class="h-6 w-6" />
          Produits et Services
        </h1>
        <p class="text-muted-foreground">Gérez votre catalogue de produits et services</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="h-4 w-4 mr-2" />
        Nouveau produit
      </Button>
    </div>

    <!-- Search -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un produit..."
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
        v-for="produit in filteredProduits"
        :key="produit.id"
        class="rounded-xl border bg-card p-4 hover:bg-accent/50 transition-colors"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 flex-1">
            <div
              :class="
                cn(
                  'p-2 rounded-lg',
                  produit.typeArticle === 'SERVICE'
                    ? 'bg-blue-100 dark:bg-blue-900/30'
                    : 'bg-primary/10'
                )
              "
            >
              <component
                :is="produit.typeArticle === 'SERVICE' ? Briefcase : Package"
                :class="
                  cn(
                    'h-5 w-5',
                    produit.typeArticle === 'SERVICE'
                      ? 'text-blue-600 dark:text-blue-400'
                      : 'text-primary'
                  )
                "
              />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <span class="font-mono font-medium text-sm">{{ produit.reference }}</span>
                <span class="font-medium truncate">{{ produit.designation }}</span>
                <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(produit.actif))">
                  {{ getStatusLabel(produit.actif) }}
                </span>
              </div>
              <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground">
                <span class="flex items-center gap-1">
                  <Tag class="h-3 w-3" />
                  {{ getTypeArticleLabel(produit.typeArticle) }}
                </span>
                <span v-if="produit.familleId">
                  {{ getFamilleName(produit.familleId) }}
                </span>
                <span v-if="produit.unite">
                  {{ produit.unite }}
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-6">
            <div class="text-right">
              <div class="font-semibold">{{ formatCurrency(produit.prixVente) }}</div>
              <div
                v-if="produit.prixAchat"
                class="text-xs text-muted-foreground"
              >
                Achat: {{ formatCurrency(produit.prixAchat) }}
              </div>
            </div>
            <div class="flex items-center gap-1">
              <Button
                variant="ghost"
                size="icon-sm"
                @click="handleEdit(produit.id!)"
              >
                <Edit class="h-4 w-4" />
              </Button>
              <Button
                v-if="deleteConfirmId !== produit.id"
                variant="ghost"
                size="icon-sm"
                class="text-destructive hover:text-destructive"
                @click="deleteConfirmId = produit.id!"
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
                  @click="handleDelete(produit.id!)"
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
    </div>

    <!-- Empty state -->
    <div
      v-if="!isLoading && filteredProduits.length === 0"
      class="text-center py-12"
    >
      <Package class="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
      <p class="text-muted-foreground">Aucun produit trouvé</p>
      <Button
        variant="outline"
        class="mt-4"
        @click="handleCreate"
      >
        <Plus class="h-4 w-4 mr-2" />
        Créer un produit
      </Button>
    </div>
  </div>
</template>
