import { createApp } from 'vue'
import { createPinia } from 'pinia'
import vueQueryPlugin from '@/plugins/vue-query'
import router from './router'
import App from './App.vue'
import './style.css'

// Naive UI setup
const meta = document.createElement('meta')
meta.name = 'naive-ui-style'
document.head.appendChild(meta)

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(vueQueryPlugin)

app.mount('#app')
