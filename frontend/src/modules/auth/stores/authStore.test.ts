import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './authStore'

describe('AuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Clear localStorage before each test
    localStorage.clear()
  })

  it('should initialize with no user', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('should login successfully', async () => {
    const store = useAuthStore()
    const result = await store.login('test@example.com', 'password')

    expect(result.success).toBe(true)
    expect(store.isAuthenticated).toBe(true)
    expect(store.user).toBeTruthy()
    expect(store.user?.email).toBe('test@example.com')
  })

  it('should store token in localStorage on login', async () => {
    const store = useAuthStore()
    await store.login('test@example.com', 'password')

    const token = localStorage.getItem('auth_token')
    expect(token).toBeTruthy()
  })

  it('should logout successfully', async () => {
    const store = useAuthStore()
    await store.login('test@example.com', 'password')
    await store.logout()

    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('auth_token')).toBeNull()
  })

  it('should check auth from localStorage', async () => {
    const mockUser = {
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
      role: 'admin',
    }

    localStorage.setItem('auth_token', 'test-token')
    localStorage.setItem('user', JSON.stringify(mockUser))

    const store = useAuthStore()
    await store.checkAuth()

    expect(store.isAuthenticated).toBe(true)
    expect(store.user).toEqual(mockUser)
  })
})
