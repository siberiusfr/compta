<script setup lang="ts">
import { useConfirm } from '@/shared/composables/useConfirm'
import { Button } from '@/components/ui/button'
import { AlertTriangle } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { isOpen, options, handleConfirm, handleCancel } = useConfirm()
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div
        v-if="isOpen && options"
        class="fixed inset-0 z-50 flex items-center justify-center"
      >
        <!-- Overlay -->
        <div
          class="absolute inset-0 bg-black/50"
          @click="handleCancel"
        />

        <!-- Dialog -->
        <div class="relative bg-background rounded-xl border shadow-xl max-w-md w-full mx-4 p-6">
          <!-- Icon -->
          <div
            :class="cn(
              'flex h-12 w-12 items-center justify-center rounded-full mx-auto mb-4',
              options.variant === 'destructive'
                ? 'bg-red-100 dark:bg-red-900/30'
                : 'bg-blue-100 dark:bg-blue-900/30'
            )"
          >
            <AlertTriangle
              :class="cn(
                'h-6 w-6',
                options.variant === 'destructive'
                  ? 'text-red-600 dark:text-red-400'
                  : 'text-blue-600 dark:text-blue-400'
              )"
            />
          </div>

          <!-- Content -->
          <div class="text-center mb-6">
            <h3 class="text-lg font-semibold mb-2">{{ options.title }}</h3>
            <p class="text-muted-foreground">{{ options.message }}</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-center gap-3">
            <Button variant="outline" @click="handleCancel">
              {{ options.cancelText }}
            </Button>
            <Button
              :variant="options.variant === 'destructive' ? 'destructive' : 'default'"
              @click="handleConfirm"
            >
              {{ options.confirmText }}
            </Button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.dialog-enter-active > div:last-child,
.dialog-leave-active > div:last-child {
  transition: transform 0.2s ease;
}

.dialog-enter-from > div:last-child {
  transform: scale(0.95);
}

.dialog-leave-to > div:last-child {
  transform: scale(0.95);
}
</style>
