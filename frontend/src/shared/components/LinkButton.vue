<script setup lang="ts">
interface Props {
  to: string
  variant?: 'primary' | 'secondary' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false
})

const variantClasses = {
  primary: 'bg-blue-600 text-white hover:bg-blue-700 disabled:bg-blue-400',
  secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 disabled:bg-gray-100',
  ghost: 'bg-transparent text-gray-700 hover:bg-gray-100 disabled:text-gray-400'
}

const sizeClasses = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2',
  lg: 'px-6 py-3 text-lg'
}
</script>

<template>
  <RouterLink
    :to="to"
    :disabled="disabled"
    :class="[
      'inline-flex items-center justify-center rounded-lg font-medium transition-colors',
      'focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
      variantClasses[variant],
      sizeClasses[size],
      disabled && 'cursor-not-allowed opacity-50'
    ]"
  >
    <slot />
  </RouterLink>
</template>
