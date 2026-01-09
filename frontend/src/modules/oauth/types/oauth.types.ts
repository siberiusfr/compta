export interface OAuthConfig {
  authority: string
  clientId: string
  redirectUri: string
  postLogoutRedirectUri: string
  scope: string
}

export interface UserProfile {
  sub: string
  name?: string
  email?: string
  email_verified?: boolean
  preferred_username?: string
  picture?: string
  roles?: string[]
  groups?: string[]
}

export interface AuthState {
  isAuthenticated: boolean
  isLoading: boolean
  user: UserProfile | null
  accessToken: string | null
  error: string | null
}

export interface LoginOptions {
  returnUrl?: string
  prompt?: 'login' | 'consent' | 'select_account'
}

export interface LogoutOptions {
  localOnly?: boolean
}
