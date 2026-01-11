<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { Button } from '@/components/ui/button'
import {
  Users,
  Plus,
  Search,
  Filter,
  Mail,
  Phone,
  Building2,
  Briefcase,
  MoreHorizontal,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  employees,
  isLoading,
  formatDate,
  getStatusColor,
  getStatusLabel,
  getContractTypeLabel,
  getInitials,
} = useHr()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Users class="h-6 w-6" />
          Employes
        </h1>
        <p class="text-muted-foreground">{{ employees.length }} employes</p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouvel employe
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher un employe..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button
        variant="outline"
        size="icon"
      >
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Employees Grid -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <div
      v-else
      class="grid gap-4 md:grid-cols-2 lg:grid-cols-3"
    >
      <div
        v-for="employee in employees"
        :key="employee.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <!-- Header -->
        <div class="flex items-start gap-4 mb-4">
          <div
            class="flex h-14 w-14 items-center justify-center rounded-full bg-primary text-primary-foreground font-semibold text-lg"
          >
            {{ getInitials(employee.fullName) }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold truncate">{{ employee.fullName }}</h3>
              <span
                :class="cn('text-xs px-2 py-0.5 rounded-full', getStatusColor(employee.status))"
              >
                {{ getStatusLabel(employee.status) }}
              </span>
            </div>
            <p class="text-sm text-muted-foreground">{{ employee.position }}</p>
            <span class="text-xs px-2 py-0.5 rounded bg-muted">
              {{ getContractTypeLabel(employee.contractType) }}
            </span>
          </div>
          <Button
            variant="ghost"
            size="icon-sm"
          >
            <MoreHorizontal class="h-4 w-4" />
          </Button>
        </div>

        <!-- Info -->
        <div class="space-y-2 text-sm">
          <div class="flex items-center gap-2 text-muted-foreground">
            <Building2 class="h-4 w-4 shrink-0" />
            <span>{{ employee.department }}</span>
          </div>
          <div class="flex items-center gap-2 text-muted-foreground">
            <Mail class="h-4 w-4 shrink-0" />
            <span class="truncate">{{ employee.email }}</span>
          </div>
          <div
            v-if="employee.phone"
            class="flex items-center gap-2 text-muted-foreground"
          >
            <Phone class="h-4 w-4 shrink-0" />
            <span>{{ employee.phone }}</span>
          </div>
          <div class="flex items-center gap-2 text-muted-foreground">
            <Briefcase class="h-4 w-4 shrink-0" />
            <span>Depuis le {{ formatDate(employee.hireDate) }}</span>
          </div>
        </div>

        <!-- Manager -->
        <div
          v-if="employee.managerName"
          class="mt-4 pt-4 border-t"
        >
          <p class="text-xs text-muted-foreground">
            Manager: <span class="font-medium text-foreground">{{ employee.managerName }}</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
