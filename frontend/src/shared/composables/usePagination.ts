import { ref, computed } from 'vue'

export interface UsePaginationOptions {
  initialPage?: number
  initialPageSize?: number
  total?: number
}

export function usePagination(options: UsePaginationOptions = {}) {
  const page = ref(options.initialPage ?? 1)
  const pageSize = ref(options.initialPageSize ?? 10)
  const total = ref(options.total ?? 0)

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  const hasNextPage = computed(() => page.value < totalPages.value)
  const hasPrevPage = computed(() => page.value > 1)

  const offset = computed(() => (page.value - 1) * pageSize.value)

  const goToPage = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages.value) {
      page.value = newPage
    }
  }

  const nextPage = () => {
    if (hasNextPage.value) {
      page.value++
    }
  }

  const prevPage = () => {
    if (hasPrevPage.value) {
      page.value--
    }
  }

  const setPageSize = (size: number) => {
    pageSize.value = size
    page.value = 1
  }

  const setTotal = (newTotal: number) => {
    total.value = newTotal
  }

  const reset = () => {
    page.value = options.initialPage ?? 1
  }

  return {
    page,
    pageSize,
    total,
    totalPages,
    hasNextPage,
    hasPrevPage,
    offset,
    goToPage,
    nextPage,
    prevPage,
    setPageSize,
    setTotal,
    reset,
  }
}
