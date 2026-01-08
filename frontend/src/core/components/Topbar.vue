<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Bell, Search, User, LogOut } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const searchQuery = ref('')

function handleSearch() {
  if (searchQuery.value.trim()) {
    console.log('Searching for:', searchQuery.value)
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="bg-white border-b border-gray-200 px-6 py-4">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4 flex-1 max-w-xl">
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" />
          <input
            v-model="searchQuery"
            @keyup.enter="handleSearch"
            type="text"
            placeholder="Rechercher..."
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>
      </div>

      <div class="flex items-center gap-4">
        <button class="p-2 rounded-lg hover:bg-gray-100 relative">
          <Bell :size="20" class="text-gray-600" />
          <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
        </button>

        <div v-if="authStore.userProfile" class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 cursor-pointer">
          <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
            <User :size="18" class="text-white" />
          </div>
          <div class="text-sm">
            <p class="font-medium text-gray-900">{{ authStore.userProfile.name || authStore.userProfile.email || authStore.userProfile.sub }}</p>
            <p class="text-gray-500 text-xs">{{ authStore.userProfile.email || authStore.userProfile.sub }}</p>
          </div>
        </div>

        <button
          @click="handleLogout"
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
          title="Déconnexion"
        >
          <LogOut :size="20" class="text-gray-600 hover:text-red-600" />
        </button>
      </div>
    </div>
  </header>
</template>
