<template>
  <div class="empty-state">
    <n-empty :description="description">
      <template v-if="$slots.icon" #icon>
        <slot name="icon" />
      </template>
      <template v-if="actionText" #extra>
        <n-button :type="actionType" @click="$emit('action')">
          {{ actionText }}
        </n-button>
      </template>
    </n-empty>
  </div>
</template>

<script setup lang="ts">
interface Props {
  description?: string
  actionText?: string
  actionType?: 'primary' | 'default' | 'tertiary' | 'success' | 'warning' | 'error'
}

withDefaults(defineProps<Props>(), {
  description: 'Aucune donnée',
  actionType: 'primary',
})

defineEmits<{
  action: []
}>()
</script>

<style scoped>
.empty-state {
  padding: 48px 24px;
  text-align: center;
}
</style>
