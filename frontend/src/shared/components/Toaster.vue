<script setup lang="ts">
import { useToast } from '@/shared/composables/useToast'
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { toasts, removeToast } = useToast()

const icons = {
  default: Info,
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info
}

const variantClasses = {
  default: 'bg-background border-border',
  success: 'bg-green-50 border-green-200 dark:bg-green-900/20 dark:border-green-800',
  error: 'bg-red-50 border-red-200 dark:bg-red-900/20 dark:border-red-800',
  warning: 'bg-yellow-50 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-800',
  info: 'bg-blue-50 border-blue-200 dark:bg-blue-900/20 dark:border-blue-800'
}

const iconClasses = {
  default: 'text-foreground',
  success: 'text-green-600 dark:text-green-400',
  error: 'text-red-600 dark:text-red-400',
  warning: 'text-yellow-600 dark:text-yellow-400',
  info: 'text-blue-600 dark:text-blue-400'
}
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="cn(
          'flex items-start gap-3 p-4 rounded-lg border shadow-lg',
          variantClasses[toast.variant]
        )"
      >
        <component
          :is="icons[toast.variant]"
          :class="cn('h-5 w-5 shrink-0 mt-0.5', iconClasses[toast.variant])"
        />
        <div class="flex-1 min-w-0">
          <p class="font-medium text-sm">{{ toast.title }}</p>
          <p v-if="toast.description" class="text-sm text-muted-foreground mt-1">
            {{ toast.description }}
          </p>
        </div>
        <button
          class="shrink-0 p-1 rounded hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          @click="removeToast(toast.id)"
        >
          <X class="h-4 w-4 text-muted-foreground" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
