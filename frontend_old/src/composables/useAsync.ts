import { ref } from 'vue'

interface UseAsyncOptions {
  immediate?: boolean
  onSuccess?: (data: any) => void
  onError?: (error: any) => void
}

/**
 * Composable for handling async operations with loading and error states
 */
export function useAsync<T>(
  asyncFunction: (...args: any[]) => Promise<T>,
  options: UseAsyncOptions = {}
) {
  const { immediate = false, onSuccess, onError } = options

  const data = ref<T | null>(null)
  const error = ref<Error | null>(null)
  const loading = ref(false)

  async function execute(...args: any[]): Promise<T | null> {
    loading.value = true
    error.value = null

    try {
      const result = await asyncFunction(...args)
      data.value = result
      onSuccess?.(result)
      return result
    } catch (err) {
      error.value = err as Error
      onError?.(err)
      return null
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute()
  }

  return {
    data,
    error,
    loading,
    execute,
  }
}
