<script setup lang="ts">
import { useNotifications } from '../composables/useNotifications'
import { Button } from '@/components/ui/button'
import { Send, Eye, Users, Clock, CheckCircle, XCircle } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { sentNotifications, formatDate } = useNotifications()

const statusConfig: Record<string, { icon: any; class: string; label: string }> = {
  sent: {
    icon: Clock,
    class: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
    label: 'Envoye'
  },
  delivered: {
    icon: CheckCircle,
    class: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
    label: 'Delivre'
  },
  failed: {
    icon: XCircle,
    class: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
    label: 'Echoue'
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Send class="h-6 w-6" />
          Notifications envoyees
        </h1>
        <p class="text-muted-foreground">
          Historique des notifications envoyees
        </p>
      </div>
      <Button>
        <Send class="h-4 w-4 mr-2" />
        Nouvelle notification
      </Button>
    </div>

    <!-- Sent Notifications List -->
    <div v-if="sentNotifications.length === 0" class="text-center py-12">
      <Send class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucune notification envoyee</p>
      <p class="text-muted-foreground">Envoyez votre premiere notification</p>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="notification in sentNotifications"
        :key="notification.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start gap-4">
          <!-- Status Icon -->
          <div :class="cn('flex h-10 w-10 items-center justify-center rounded-full shrink-0', statusConfig[notification.status]?.class)">
            <component :is="statusConfig[notification.status]?.icon || Clock" class="h-5 w-5" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-2">
              <h3 class="font-medium">{{ notification.subject }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', statusConfig[notification.status]?.class)">
                {{ statusConfig[notification.status]?.label }}
              </span>
            </div>

            <p class="text-sm text-muted-foreground mt-1 line-clamp-2">
              {{ notification.message }}
            </p>

            <!-- Meta -->
            <div class="flex items-center gap-4 mt-3 text-sm text-muted-foreground">
              <span class="flex items-center gap-1">
                <Users class="h-4 w-4" />
                {{ notification.recipients.length }} destinataire{{ notification.recipients.length > 1 ? 's' : '' }}
              </span>
              <span class="flex items-center gap-1">
                <Eye class="h-4 w-4" />
                {{ notification.readCount }} lecture{{ notification.readCount > 1 ? 's' : '' }}
              </span>
              <span class="flex items-center gap-1">
                <Clock class="h-4 w-4" />
                {{ formatDate(notification.sentAt) }}
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1 shrink-0">
            <Button variant="ghost" size="sm">
              Voir details
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
