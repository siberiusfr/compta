<script setup lang="ts">
import Sidebar from '@/core/components/Sidebar.vue'
import Topbar from '@/core/components/Topbar.vue'
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <Sidebar />
    
    <div class="flex-1 flex flex-col overflow-hidden">
      <Topbar />
      
      <main class="flex-1 overflow-auto p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
