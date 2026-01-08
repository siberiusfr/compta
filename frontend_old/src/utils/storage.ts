/**
 * Safe localStorage wrapper with JSON serialization
 */
export const storage = {
  get<T>(key: string, defaultValue?: T): T | null {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue ?? null
    } catch (error) {
      console.error(`Error getting item ${key} from localStorage:`, error)
      return defaultValue ?? null
    }
  },

  set<T>(key: string, value: T): void {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`Error setting item ${key} in localStorage:`, error)
    }
  },

  remove(key: string): void {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error(`Error removing item ${key} from localStorage:`, error)
    }
  },

  clear(): void {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('Error clearing localStorage:', error)
    }
  },

  has(key: string): boolean {
    return localStorage.getItem(key) !== null
  },
}

/**
 * Safe sessionStorage wrapper with JSON serialization
 */
export const sessionStorage = {
  get<T>(key: string, defaultValue?: T): T | null {
    try {
      const item = window.sessionStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue ?? null
    } catch (error) {
      console.error(`Error getting item ${key} from sessionStorage:`, error)
      return defaultValue ?? null
    }
  },

  set<T>(key: string, value: T): void {
    try {
      window.sessionStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`Error setting item ${key} in sessionStorage:`, error)
    }
  },

  remove(key: string): void {
    try {
      window.sessionStorage.removeItem(key)
    } catch (error) {
      console.error(`Error removing item ${key} from sessionStorage:`, error)
    }
  },

  clear(): void {
    try {
      window.sessionStorage.clear()
    } catch (error) {
      console.error('Error clearing sessionStorage:', error)
    }
  },

  has(key: string): boolean {
    return window.sessionStorage.getItem(key) !== null
  },
}
