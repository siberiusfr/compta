import { ref, computed, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

export interface UseSearchOptions {
  debounceMs?: number
  minLength?: number
}

export function useSearch<T>(
  items: () => T[],
  searchFn: (item: T, query: string) => boolean,
  options: UseSearchOptions = {}
) {
  const { debounceMs = 300, minLength = 0 } = options

  const query = ref('')
  const debouncedQuery = ref('')

  const updateDebouncedQuery = useDebounceFn((value: string) => {
    debouncedQuery.value = value
  }, debounceMs)

  watch(query, (value) => {
    updateDebouncedQuery(value)
  })

  const filteredItems = computed(() => {
    const searchQuery = debouncedQuery.value.toLowerCase().trim()

    if (searchQuery.length < minLength) {
      return items()
    }

    return items().filter(item => searchFn(item, searchQuery))
  })

  const isSearching = computed(() => query.value !== debouncedQuery.value)

  const hasResults = computed(() => filteredItems.value.length > 0)

  const clearSearch = () => {
    query.value = ''
    debouncedQuery.value = ''
  }

  return {
    query,
    filteredItems,
    isSearching,
    hasResults,
    clearSearch
  }
}
