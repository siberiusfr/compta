import { computed, type Ref } from 'vue'

/**
 * Utilitaire pour protéger l'accès aux données des stores
 * Évite les erreurs "Cannot read properties of undefined"
 */

export function useSafeArray<T>(arrayRef: Ref<T[] | undefined>) {
  return computed(() => {
    if (!arrayRef.value || !Array.isArray(arrayRef.value)) {
      return []
    }
    return arrayRef.value
  })
}

export function useSafeObject<T>(objectRef: Ref<T | undefined>) {
  return computed(() => {
    if (!objectRef.value) {
      return null
    }
    return objectRef.value
  })
}

export function useSafeNumber(numberRef: Ref<number | undefined>) {
  return computed(() => {
    if (numberRef.value === undefined || numberRef.value === null) {
      return 0
    }
    return numberRef.value
  })
}

export function isLoading(data: unknown) {
  return data === undefined || data === null
}

export function withLoading<T>(data: Ref<T | undefined>, renderFn: () => unknown) {
  if (isLoading(data.value)) {
    return renderFn()
  }
  return null
}
