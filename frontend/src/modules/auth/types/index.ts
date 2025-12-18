import type { User } from '@app-types/index'

export interface LoginCredentials {
  email: string
  password: string
}

export interface RegisterData {
  name: string
  email: string
  password: string
  confirmPassword?: string
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export interface AuthResponse {
  token: string
  user: User
  expiresAt?: string
}

export interface TokenPayload {
  userId: string
  email: string
  role: string
  exp: number
  iat: number
}

export type AuthError = {
  code: 'INVALID_CREDENTIALS' | 'USER_NOT_FOUND' | 'ALREADY_EXISTS' | 'TOKEN_EXPIRED'
  message: string
}
