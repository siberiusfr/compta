<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useNotifications } from '../composables/useNotifications'
import { Button } from '@/components/ui/button'
import {
  Bell,
  Check,
  CheckCheck,
  Archive,
  Trash2,
  ExternalLink,
  Info,
  AlertTriangle,
  AlertCircle,
  CheckCircle
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const {
  notifications,
  isLoading,
  unreadCount,
  formatRelativeTime,
  getTypeColor,
  getPriorityColor,
  markAsRead,
  markAllAsRead,
  archiveNotification,
  deleteNotification
} = useNotifications()

const activeNotifications = computed(() =>
  notifications.value.filter(n => !n.archived)
)

const typeIcons: Record<string, any> = {
  info: Info,
  warning: AlertTriangle,
  error: AlertCircle,
  success: CheckCircle
}

const handleNotificationClick = (notification: typeof notifications.value[0]) => {
  markAsRead(notification.id)
  if (notification.link) {
    router.push(notification.link)
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Bell class="h-6 w-6" />
          Boite de reception
        </h1>
        <p class="text-muted-foreground">
          {{ unreadCount }} notification{{ unreadCount > 1 ? 's' : '' }} non lue{{ unreadCount > 1 ? 's' : '' }}
        </p>
      </div>
      <Button
        v-if="unreadCount > 0"
        variant="outline"
        size="sm"
        @click="markAllAsRead"
      >
        <CheckCheck class="h-4 w-4 mr-2" />
        Tout marquer comme lu
      </Button>
    </div>

    <!-- Notifications List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else-if="activeNotifications.length === 0" class="text-center py-12">
      <Bell class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucune notification</p>
      <p class="text-muted-foreground">Vous etes a jour !</p>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="notification in activeNotifications"
        :key="notification.id"
        :class="cn(
          'rounded-xl border bg-card p-4 transition-all hover:shadow-md cursor-pointer',
          !notification.read && 'border-l-4 border-l-primary bg-primary/5'
        )"
        @click="handleNotificationClick(notification)"
      >
        <div class="flex items-start gap-4">
          <!-- Type Icon -->
          <div :class="cn('flex h-10 w-10 items-center justify-center rounded-full shrink-0', getTypeColor(notification.type))">
            <component :is="typeIcons[notification.type]" class="h-5 w-5" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-2">
              <div>
                <h3 :class="cn('font-medium', !notification.read && 'font-semibold')">
                  {{ notification.title }}
                </h3>
                <p class="text-sm text-muted-foreground mt-1 line-clamp-2">
                  {{ notification.message }}
                </p>
              </div>
              <span class="text-xs text-muted-foreground whitespace-nowrap">
                {{ formatRelativeTime(notification.createdAt) }}
              </span>
            </div>

            <!-- Meta -->
            <div class="flex items-center gap-3 mt-3">
              <span :class="cn('text-xs px-2 py-1 rounded-full', getPriorityColor(notification.priority))">
                {{ notification.priority }}
              </span>
              <span class="text-xs text-muted-foreground">
                {{ notification.sender }}
              </span>
              <span class="text-xs text-muted-foreground">
                {{ notification.category }}
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1 shrink-0" @click.stop>
            <Button
              v-if="!notification.read"
              variant="ghost"
              size="icon-sm"
              title="Marquer comme lu"
              @click="markAsRead(notification.id)"
            >
              <Check class="h-4 w-4" />
            </Button>
            <Button
              v-if="notification.link"
              variant="ghost"
              size="icon-sm"
              title="Voir les details"
              @click="router.push(notification.link)"
            >
              <ExternalLink class="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
              title="Archiver"
              @click="archiveNotification(notification.id)"
            >
              <Archive class="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
              title="Supprimer"
              class="text-destructive hover:text-destructive"
              @click="deleteNotification(notification.id)"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
