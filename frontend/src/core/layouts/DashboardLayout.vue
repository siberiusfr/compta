<script setup lang="ts">
import { computed } from 'vue'
import { useSidebarStore } from '@/core/stores/sidebarStore'
import Sidebar from '@/core/components/Sidebar.vue'
import Header from '@/core/components/Header.vue'
import { cn } from '@/lib/utils'

const sidebarStore = useSidebarStore()

const mainClasses = computed(() => cn(
  'min-h-screen transition-all duration-300',
  sidebarStore.isCollapsed ? 'lg:pl-16' : 'lg:pl-64'
))
</script>

<template>
  <div class="min-h-screen bg-background">
    <Sidebar />
    <div :class="mainClasses">
      <Header />
      <main class="p-4 lg:p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
