<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  show?: boolean
  position?: 'top' | 'bottom' | 'top-right' | 'bottom-right'
  duration?: number
  type?: 'success' | 'error' | 'warning' | 'info'
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  position: 'top-right',
  duration: 5000,
  type: 'info'
})

const emit = defineEmits<{
  close: []
}>()

const remaining = ref(props.duration)

let intervalId: number

onMounted(() => {
  if (props.duration > 0) {
    intervalId = window.setInterval(() => {
      remaining.value -= 1000
      if (remaining.value <= 0) {
        emit('close')
      }
    }, 1000)
  }
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})

function close() {
  emit('close')
}

const typeClasses = {
  success: 'bg-green-50 border-green-200 text-green-800',
  error: 'bg-red-50 border-red-200 text-red-800',
  warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800'
}
</script>

<template>
  <Teleport to="body">
    <Transition name="toast">
      <div
        v-if="show"
        :class="[
          'fixed z-50 p-4 rounded-lg border shadow-lg',
          typeClasses[type],
          position === 'top' ? 'top-4 left-1/2 -translate-x-1/2' :
          position === 'bottom' ? 'bottom-4 left-1/2 -translate-x-1/2' :
          position === 'top-right' ? 'top-4 right-4' :
          'bottom-4 right-4'
        ]"
      >
        <div class="flex items-start gap-3">
          <div class="flex-1">
            <slot />
          </div>
          <button
            @click="close"
            class="p-1 hover:bg-black/5 rounded transition-colors"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
