<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { Calendar, Clock, Check, X } from 'lucide-vue-next'

const { leaveRequests, pendingLeaves, updateLeaveRequest } = useHr()

function getStatusColor(status: string) {
  switch (status) {
    case 'pending': return 'bg-yellow-100 text-yellow-600'
    case 'approved': return 'bg-green-100 text-green-600'
    case 'rejected': return 'bg-red-100 text-red-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'pending': return 'En attente'
    case 'approved': return 'Approuvé'
    case 'rejected': return 'Refusé'
    default: return status
  }
}

function getTypeLabel(type: string) {
  switch (type) {
    case 'paid': return 'Congé payé'
    case 'unpaid': return 'Sans solde'
    case 'sick': return 'Maladie'
    case 'personal': return 'Personnel'
    default: return type
  }
}

function handleApprove(id: string) {
  updateLeaveRequest.value(id, { status: 'approved', reviewedBy: 'Vous', reviewedAt: new Date() })
}

function handleReject(id: string) {
  updateLeaveRequest.value(id, { status: 'rejected', reviewedBy: 'Vous', reviewedAt: new Date() })
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Congés</h1>
        <p class="text-gray-600 mt-1">{{ pendingLeaves.length }} demande(s) en attente</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Calendar :size="18" />
        Nouvelle demande
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="request in leaveRequests"
          :key="request.id"
          class="p-6 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h3 class="font-semibold text-gray-900">{{ request.employeeName }}</h3>
                <span
                  :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    getStatusColor(request.status)
                  ]"
                >
                  {{ getStatusLabel(request.status) }}
                </span>
              </div>

              <div class="flex items-center gap-2 text-sm text-gray-600 mb-2">
                <Calendar :size="16" class="text-gray-400" />
                <span>{{ request.startDate.toLocaleDateString('fr-FR') }} - {{ request.endDate.toLocaleDateString('fr-FR') }}</span>
                <span class="text-gray-400">|</span>
                <Clock :size="16" class="text-gray-400" />
                <span>{{ request.days }} jour(s)</span>
              </div>

              <p class="text-sm text-gray-600">{{ getTypeLabel(request.type) }}</p>
              <p class="text-sm text-gray-500 mt-1">{{ request.reason }}</p>

              <p v-if="request.status !== 'pending'" class="text-xs text-gray-400 mt-2">
                {{ request.status === 'approved' ? 'Approuvé' : 'Refusé' }} par {{ request.reviewedBy }} le {{ request.reviewedAt?.toLocaleDateString('fr-FR') }}
              </p>
            </div>

            <div v-if="request.status === 'pending'" class="flex gap-2">
              <button
                @click="handleApprove(request.id)"
                class="p-2 bg-green-100 hover:bg-green-200 rounded-lg transition-colors"
                title="Approuver"
              >
                <Check :size="18" class="text-green-600" />
              </button>
              <button
                @click="handleReject(request.id)"
                class="p-2 bg-red-100 hover:bg-red-200 rounded-lg transition-colors"
                title="Refuser"
              >
                <X :size="18" class="text-red-600" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
