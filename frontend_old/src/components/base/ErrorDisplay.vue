<template>
  <n-alert :type="type" :title="title" :closable="closable" @close="$emit('close')">
    <slot>{{ message }}</slot>
  </n-alert>
</template>

<script setup lang="ts">
interface Props {
  type?: 'error' | 'warning' | 'info' | 'success'
  title?: string
  message?: string
  closable?: boolean
}

withDefaults(defineProps<Props>(), {
  type: 'error',
  title: 'Erreur',
  message: 'Une erreur est survenue',
  closable: true,
})

defineEmits<{
  close: []
}>()
</script>
