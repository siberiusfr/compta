import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import router from '@/core/router'
import { useAuthStore } from '@/stores/authStore'
import { useThemeStore } from '@/core/stores/themeStore'
import './style.css'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(VueQueryPlugin, {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        staleTime: 1000 * 60 * 5, // 5 minutes
        refetchOnWindowFocus: false,
      },
    },
  },
})

// Initialiser les stores avant de monter l'app
const authStore = useAuthStore()
const themeStore = useThemeStore()

// Initialize theme
themeStore.$subscribe(() => {})

// Initialize auth and mount app
authStore.initialize().then(() => {
  app.mount('#app')
})
