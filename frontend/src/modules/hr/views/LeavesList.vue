<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { Button } from '@/components/ui/button'
import { CalendarDays, Plus, Search, Check, X, Calendar } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  leaveRequests,
  pendingLeaves,
  isLoading,
  formatDate,
  getStatusColor,
  getStatusLabel,
  getLeaveTypeLabel,
  approveLeave,
  rejectLeave,
} = useHr()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <CalendarDays class="h-6 w-6" />
          Demandes de conges
        </h1>
        <p class="text-muted-foreground">
          {{ pendingLeaves.length }} demande{{ pendingLeaves.length > 1 ? 's' : '' }} en attente
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle demande
      </Button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        placeholder="Rechercher..."
        class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
      />
    </div>

    <!-- Leave Requests List -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <div
      v-else
      class="space-y-3"
    >
      <div
        v-for="leave in leaveRequests"
        :key="leave.id"
        :class="
          cn(
            'rounded-xl border bg-card p-5 transition-shadow',
            leave.status === 'pending' && 'border-l-4 border-l-yellow-500'
          )
        "
      >
        <div class="flex items-start gap-4">
          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-semibold">{{ leave.employeeName }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(leave.status))">
                {{ getStatusLabel(leave.status) }}
              </span>
              <span class="text-xs px-2 py-1 rounded bg-muted">
                {{ getLeaveTypeLabel(leave.type) }}
              </span>
            </div>

            <div class="flex items-center gap-2 text-sm text-muted-foreground mt-2">
              <Calendar class="h-4 w-4" />
              <span> Du {{ formatDate(leave.startDate) }} au {{ formatDate(leave.endDate) }} </span>
              <span class="font-medium text-foreground">({{ leave.days }} jours)</span>
            </div>

            <p
              v-if="leave.reason"
              class="text-sm text-muted-foreground mt-2"
            >
              {{ leave.reason }}
            </p>

            <p
              v-if="leave.approvedBy"
              class="text-xs text-muted-foreground mt-2"
            >
              Approuve par {{ leave.approvedBy }} le {{ formatDate(leave.approvedAt!) }}
            </p>
          </div>

          <!-- Actions -->
          <div
            v-if="leave.status === 'pending'"
            class="flex items-center gap-2"
          >
            <Button
              variant="outline"
              size="sm"
              class="text-green-600 hover:text-green-600 hover:bg-green-50"
              @click="approveLeave(leave.id)"
            >
              <Check class="h-4 w-4 mr-1" />
              Approuver
            </Button>
            <Button
              variant="outline"
              size="sm"
              class="text-red-600 hover:text-red-600 hover:bg-red-50"
              @click="rejectLeave(leave.id)"
            >
              <X class="h-4 w-4 mr-1" />
              Refuser
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
