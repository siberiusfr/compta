<template>
  <div class="page-header">
    <n-page-header :title="title" :subtitle="subtitle" @back="handleBack">
      <template v-if="$slots.avatar" #avatar>
        <slot name="avatar" />
      </template>
      <template v-if="$slots.header" #header>
        <slot name="header" />
      </template>
      <template v-if="$slots.extra" #extra>
        <slot name="extra" />
      </template>
      <template v-if="$slots.footer" #footer>
        <slot name="footer" />
      </template>
    </n-page-header>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  subtitle?: string
  showBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showBack: true,
})

const emit = defineEmits<{
  back: []
}>()

const router = useRouter()

function handleBack() {
  if (props.showBack) {
    emit('back')
    router.back()
  }
}
</script>

