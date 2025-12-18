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

<style scoped>
.auth-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-header h1 {
  margin: 0;
  font-size: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
