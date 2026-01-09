<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useSidebarStore } from '@/core/stores/sidebarStore'
import { useThemeStore } from '@/core/stores/themeStore'
import { Button } from '@/components/ui/button'
import {
  Menu,
  PanelLeftClose,
  PanelLeftOpen,
  Sun,
  Moon,
  Monitor,
  LogOut,
  User,
  ChevronRight
} from 'lucide-vue-next'
import type { BreadcrumbItem } from '@/shared/types/common.types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const sidebarStore = useSidebarStore()
const themeStore = useThemeStore()

const breadcrumbs = computed((): BreadcrumbItem[] => {
  const items: BreadcrumbItem[] = [{ label: 'Accueil', route: '/dashboard' }]

  const pathParts = route.path.split('/').filter(Boolean)

  let currentPath = ''
  for (const part of pathParts) {
    currentPath += `/${part}`
    const name = part.charAt(0).toUpperCase() + part.slice(1).replace(/-/g, ' ')
    items.push({ label: name, route: currentPath })
  }

  return items
})

const themeIcon = computed(() => {
  switch (themeStore.theme) {
    case 'light': return Sun
    case 'dark': return Moon
    default: return Monitor
  }
})

const handleLogout = async () => {
  await authStore.logout()
}

const goTo = (path?: string) => {
  if (path) {
    router.push(path)
  }
}
</script>

<template>
  <header class="sticky top-0 z-20 flex h-16 items-center gap-4 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 px-4 lg:px-6">
    <!-- Mobile menu button -->
    <Button
      variant="ghost"
      size="icon"
      class="lg:hidden"
      @click="sidebarStore.toggleMobile"
    >
      <Menu class="h-5 w-5" />
    </Button>

    <!-- Sidebar toggle (desktop) -->
    <Button
      variant="ghost"
      size="icon"
      class="hidden lg:flex"
      @click="sidebarStore.toggle"
    >
      <PanelLeftClose v-if="!sidebarStore.isCollapsed" class="h-5 w-5" />
      <PanelLeftOpen v-else class="h-5 w-5" />
    </Button>

    <!-- Breadcrumb -->
    <nav class="flex items-center gap-1 text-sm text-muted-foreground">
      <template v-for="(item, index) in breadcrumbs" :key="item.label">
        <ChevronRight v-if="index > 0" class="h-4 w-4" />
        <button
          :class="[
            'hover:text-foreground transition-colors',
            index === breadcrumbs.length - 1 ? 'text-foreground font-medium' : ''
          ]"
          @click="goTo(item.route)"
        >
          {{ item.label }}
        </button>
      </template>
    </nav>

    <!-- Spacer -->
    <div class="flex-1" />

    <!-- Actions -->
    <div class="flex items-center gap-2">
      <!-- Theme toggle -->
      <Button
        variant="ghost"
        size="icon"
        @click="themeStore.toggleTheme"
        :title="`Theme: ${themeStore.theme}`"
      >
        <component :is="themeIcon" class="h-5 w-5" />
      </Button>

      <!-- User menu -->
      <div class="flex items-center gap-3 pl-2 border-l">
        <div class="hidden sm:block text-right">
          <p class="text-sm font-medium">
            {{ authStore.userProfile?.name || 'Utilisateur' }}
          </p>
          <p class="text-xs text-muted-foreground">
            {{ authStore.userProfile?.email }}
          </p>
        </div>
        <div class="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-primary-foreground">
          <User class="h-5 w-5" />
        </div>
        <Button
          variant="ghost"
          size="icon"
          @click="handleLogout"
          title="Deconnexion"
        >
          <LogOut class="h-5 w-5" />
        </Button>
      </div>
    </div>
  </header>
</template>
