<script setup lang="ts">
interface Props {
  show?: boolean
  title?: string
  persistent?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  persistent: false
})

const emit = defineEmits<{
  close: []
}>()

function onBackdropClick() {
  if (!props.persistent) {
    emit('close')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @click="onBackdropClick"
      >
        <div class="fixed inset-0 bg-black/50" />
        
        <div
          class="relative bg-white rounded-xl shadow-lg max-w-lg w-full max-h-[90vh] overflow-auto"
          @click.stop
        >
          <div v-if="title" class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-lg font-semibold text-gray-900">{{ title }}</h2>
          </div>

          <div class="px-6 py-4">
            <slot />
          </div>

          <slot name="footer">
            <div class="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
              <slot name="actions">
                <Button variant="secondary" @click="emit('close')">Fermer</Button>
              </slot>
            </div>
          </slot>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active > div:last-child,
.modal-leave-active > div:last-child {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from > div:last-child,
.modal-leave-to > div:last-child {
  transform: scale(0.95);
  opacity: 0;
}
</style>
