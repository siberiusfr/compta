<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider, NLoadingBarProvider } from 'naive-ui'
import { themeOverrides } from '@/config/theme'
import DefaultLayout from '@layouts/DefaultLayout.vue'
import AuthLayout from '@layouts/AuthLayout.vue'

const route = useRoute()

const layoutComponent = computed(() => {
  const layout = route.meta.layout || 'default'
  return layout === 'auth' ? AuthLayout : DefaultLayout
})
</script>

<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-message-provider>
          <component :is="layoutComponent">
            <router-view />
          </component>
        </n-message-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

