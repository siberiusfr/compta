<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSidebarStore } from '@/core/stores/sidebarStore'
import { menuItems } from '@/core/config/menu'
import {
  LayoutDashboard,
  Bell,
  Building2,
  Users,
  Calculator,
  FileText,
  Shield,
  Settings,
  ChevronDown,
  ChevronRight,
  X
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const route = useRoute()
const sidebarStore = useSidebarStore()

const expandedMenus = ref<Set<string>>(new Set())

const iconMap: Record<string, any> = {
  LayoutDashboard,
  Bell,
  Building2,
  Users,
  Calculator,
  FileText,
  Shield,
  Settings
}

const isActive = (path?: string) => {
  if (!path) return false
  return route.path === path || route.path.startsWith(path + '/')
}

const isMenuExpanded = (label: string) => expandedMenus.value.has(label)

const toggleMenu = (label: string) => {
  if (expandedMenus.value.has(label)) {
    expandedMenus.value.delete(label)
  } else {
    expandedMenus.value.add(label)
  }
}

const navigateTo = (path?: string) => {
  if (path) {
    router.push(path)
    sidebarStore.closeMobile()
  }
}

const sidebarClasses = computed(() => cn(
  'fixed left-0 top-0 z-40 h-screen bg-sidebar border-r border-sidebar-border transition-all duration-300',
  sidebarStore.isCollapsed ? 'w-16' : 'w-64',
  'max-lg:w-64',
  sidebarStore.isMobileOpen ? 'max-lg:translate-x-0' : 'max-lg:-translate-x-full'
))
</script>

<template>
  <aside :class="sidebarClasses">
    <!-- Logo Header -->
    <div class="flex h-16 items-center justify-between border-b border-sidebar-border px-4">
      <div v-if="!sidebarStore.isCollapsed" class="flex items-center gap-2">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
          <Calculator class="h-5 w-5 text-primary-foreground" />
        </div>
        <span class="text-lg font-semibold text-sidebar-foreground">Compta</span>
      </div>
      <div v-else class="mx-auto">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
          <Calculator class="h-5 w-5 text-primary-foreground" />
        </div>
      </div>
      <button
        class="lg:hidden p-1 rounded-md hover:bg-sidebar-accent"
        @click="sidebarStore.closeMobile"
      >
        <X class="h-5 w-5" />
      </button>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto px-3 py-4">
      <ul class="space-y-1">
        <li v-for="item in menuItems" :key="item.label">
          <!-- Single item without children -->
          <template v-if="!item.children">
            <button
              :class="cn(
                'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                isActive(item.route)
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                  : 'text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )"
              @click="navigateTo(item.route)"
            >
              <component
                :is="iconMap[item.icon!]"
                v-if="item.icon"
                class="h-5 w-5 shrink-0"
              />
              <span v-if="!sidebarStore.isCollapsed" class="truncate">{{ item.label }}</span>
            </button>
          </template>

          <!-- Item with children -->
          <template v-else>
            <button
              :class="cn(
                'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                item.children.some(child => isActive(child.route))
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                  : 'text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )"
              @click="toggleMenu(item.label)"
            >
              <component
                :is="iconMap[item.icon!]"
                v-if="item.icon"
                class="h-5 w-5 shrink-0"
              />
              <span v-if="!sidebarStore.isCollapsed" class="flex-1 truncate text-left">
                {{ item.label }}
              </span>
              <ChevronDown
                v-if="!sidebarStore.isCollapsed && isMenuExpanded(item.label)"
                class="h-4 w-4 shrink-0"
              />
              <ChevronRight
                v-else-if="!sidebarStore.isCollapsed"
                class="h-4 w-4 shrink-0"
              />
            </button>

            <!-- Submenu -->
            <ul
              v-if="isMenuExpanded(item.label) && !sidebarStore.isCollapsed"
              class="ml-4 mt-1 space-y-1 border-l border-sidebar-border pl-3"
            >
              <li v-for="child in item.children" :key="child.label">
                <button
                  :class="cn(
                    'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                    isActive(child.route)
                      ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                      : 'text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
                  )"
                  @click="navigateTo(child.route)"
                >
                  <span class="truncate">{{ child.label }}</span>
                </button>
              </li>
            </ul>
          </template>
        </li>
      </ul>
    </nav>
  </aside>

  <!-- Mobile overlay -->
  <div
    v-if="sidebarStore.isMobileOpen"
    class="fixed inset-0 z-30 bg-black/50 lg:hidden"
    @click="sidebarStore.closeMobile"
  />
</template>
