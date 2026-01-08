export interface OAuthApplication {
  id: string
  name: string
  clientId: string
  clientSecret: string
  redirectUris: string[]
  scopes: string[]
  createdAt: Date
  active: boolean
}

export interface OAuthToken {
  id: string
  applicationId: string
  applicationName: string
  accessToken: string
  refreshToken?: string
  expiresIn: number
  scopes: string[]
  createdAt: Date
  expiresAt: Date
  lastUsedAt?: Date
}

export interface OAuthConsent {
  id: string
  applicationId: string
  applicationName: string
  scopes: string[]
  grantedAt: Date
  expiresAt: Date
}
