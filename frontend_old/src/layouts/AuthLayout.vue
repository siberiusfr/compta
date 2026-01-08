<template>
  <n-layout class="auth-layout">
    <n-layout-content>
      <div class="auth-container">
        <div class="auth-card">
          <div class="auth-header">
            <h1>{{ appName }}</h1>
          </div>
          <router-view />
        </div>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
const appName = import.meta.env.VITE_APP_NAME || 'Compta'
</script>

