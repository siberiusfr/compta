<script setup lang="ts">
import { usePermissions } from '../composables/usePermissions'
import { Shield, Plus } from 'lucide-vue-next'

const { roles } = usePermissions()
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Rôles</h1>
        <p class="text-gray-600 mt-1">Gérez les rôles et permissions</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouveau rôle
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="role in roles"
        :key="role.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 p-6"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <Shield :size="24" class="text-blue-600" />
            </div>
            <div>
              <h3 class="font-semibold text-gray-900">{{ role.name }}</h3>
              <p class="text-xs text-gray-500">{{ role.userCount }} utilisateur(s)</p>
            </div>
          </div>
        </div>

        <p class="text-sm text-gray-600 mb-4">{{ role.description }}</p>

        <div>
          <p class="text-xs text-gray-500 font-medium uppercase mb-2">Permissions</p>
          <div class="flex flex-wrap gap-1">
            <span
              v-for="permission in role.permissions.slice(0, 4)"
              :key="permission"
              class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs"
            >
              {{ permission }}
            </span>
            <span v-if="role.permissions.length > 4" class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs">
              +{{ role.permissions.length - 4 }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
