<template>
  <n-modal
    v-model:show="isVisible"
    preset="dialog"
    :title="title"
    :positive-text="positiveText"
    :negative-text="negativeText"
    @positive-click="handleConfirm"
    @negative-click="handleCancel"
  >
    <slot>{{ message }}</slot>
  </n-modal>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  message?: string
  positiveText?: string
  negativeText?: string
  show?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Confirmation',
  message: 'Êtes-vous sûr ?',
  positiveText: 'Confirmer',
  negativeText: 'Annuler',
  show: false,
})

const emit = defineEmits<{
  confirm: []
  cancel: []
  'update:show': [value: boolean]
}>()

const isVisible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

function handleConfirm() {
  emit('confirm')
  isVisible.value = false
}

function handleCancel() {
  emit('cancel')
  isVisible.value = false
}
</script>
