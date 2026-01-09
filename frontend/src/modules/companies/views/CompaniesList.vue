<script setup lang="ts">
import { useCompanies } from '../composables/useCompanies'
import { Button } from '@/components/ui/button'
import {
  Building2,
  Plus,
  Search,
  Filter,
  MapPin,
  Users,
  Mail
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  filteredCompanies,
  isLoading,
  totalEmployees,
  formatCurrency,
  getStatusColor,
  getStatusLabel,
  getTypeLabel,
  getInitials
} = useCompanies()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Building2 class="h-6 w-6" />
          Entreprises
        </h1>
        <p class="text-muted-foreground">
          {{ filteredCompanies.length }} entreprises - {{ totalEmployees }} employes
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle entreprise
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher une entreprise..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button variant="outline" size="icon">
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Companies Grid -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <RouterLink
        v-for="company in filteredCompanies"
        :key="company.id"
        :to="`/companies/${company.id}`"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow block"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-primary text-primary-foreground font-semibold">
              {{ getInitials(company.name) }}
            </div>
            <div>
              <h3 class="font-semibold">{{ company.name }}</h3>
              <span class="text-xs text-muted-foreground">
                {{ getTypeLabel(company.type) }}
              </span>
            </div>
          </div>
          <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(company.status))">
            {{ getStatusLabel(company.status) }}
          </span>
        </div>

        <!-- Info -->
        <div class="space-y-2 text-sm">
          <div class="flex items-center gap-2 text-muted-foreground">
            <MapPin class="h-4 w-4 shrink-0" />
            <span class="truncate">{{ company.address.city }}, {{ company.address.country }}</span>
          </div>
          <div class="flex items-center gap-2 text-muted-foreground">
            <Users class="h-4 w-4 shrink-0" />
            <span>{{ company.employeeCount }} employes</span>
          </div>
          <div v-if="company.email" class="flex items-center gap-2 text-muted-foreground">
            <Mail class="h-4 w-4 shrink-0" />
            <span class="truncate">{{ company.email }}</span>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between mt-4 pt-4 border-t text-xs text-muted-foreground">
          <span>SIRET: {{ company.siret }}</span>
          <span v-if="company.capital">Capital: {{ formatCurrency(company.capital) }}</span>
        </div>
      </RouterLink>
    </div>
  </div>
</template>
