<script setup lang="ts">
import { ref, computed } from 'vue'
import { usePermissions } from '../composables/usePermissions'
import { Users, Plus, Search, MoreVertical } from 'lucide-vue-next'

const { users, activeUsers } = usePermissions()
const searchQuery = ref('')

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(u =>
    u.firstName.toLowerCase().includes(query) ||
    u.lastName.toLowerCase().includes(query) ||
    u.email.toLowerCase().includes(query)
  )
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Utilisateurs</h1>
        <p class="text-gray-600 mt-1">{{ activeUsers.length }} utilisateur(s) actif(s)</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouvel utilisateur
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un utilisateur..."
          class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
        />
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Utilisateur</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rôle</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Dernière connexion</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
                  <Users :size="20" class="text-gray-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-900">{{ user.firstName }} {{ user.lastName }}</p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ user.email }}</td>
            <td class="px-6 py-4">
              <span class="px-2 py-1 bg-blue-100 text-blue-600 rounded text-xs font-medium">{{ user.roleName }}</span>
            </td>
            <td class="px-6 py-4">
              <span
                :class="[
                  'px-2 py-1 rounded text-xs font-medium',
                  user.active ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
                ]"
              >
                {{ user.active ? 'Actif' : 'Inactif' }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">
              {{ user.lastLogin ? user.lastLogin.toLocaleString('fr-FR') : 'Jamais' }}
            </td>
            <td class="px-6 py-4 text-right">
              <button class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
                <MoreVertical :size="18" class="text-gray-500" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
