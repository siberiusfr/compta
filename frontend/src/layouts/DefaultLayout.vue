<template>
  <n-layout has-sider class="full-height-layout">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="currentRoute"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-content">
          <h2>{{ appName }}</h2>
          <n-space>
            <n-button quaternary circle @click="handleLogout">
              <template #icon>
                <n-icon><LogOutOutline /></n-icon>
              </template>
            </n-button>
          </n-space>
        </div>
      </n-layout-header>

      <n-layout-content content-style="padding: 24px;">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { LogOutOutline } from '@vicons/ionicons5'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const appName = import.meta.env.VITE_APP_NAME || 'Compta'

const currentRoute = computed(() => route.name as string)

const menuOptions: MenuOption[] = [
  {
    label: 'Comptabilité',
    key: 'accounting',
    icon: renderIcon('📊'),
  },
  {
    label: 'Ressources Humaines',
    key: 'hr',
    icon: renderIcon('👥'),
  },
  {
    label: 'Documents',
    key: 'documents',
    icon: renderIcon('📄'),
  },
]

function renderIcon(emoji: string) {
  return () => h('span', { style: { fontSize: '18px' } }, emoji)
}

function handleLogout() {
  router.push({ name: 'login' })
}
</script>

