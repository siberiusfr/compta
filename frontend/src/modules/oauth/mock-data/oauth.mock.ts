import type { OAuthApplication, OAuthToken, OAuthConsent } from '../types/oauth.types'

export const mockOAuthApplications: OAuthApplication[] = [
  {
    id: '1',
    name: 'Mobile App',
    clientId: 'com.compta.mobile',
    clientSecret: 'sk_live_51M...',
    redirectUris: ['com.compta.mobile://oauth/callback'],
    scopes: ['read:profile', 'read:companies', 'write:invoices'],
    createdAt: new Date('2024-01-01T00:00:00'),
    active: true
  },
  {
    id: '2',
    name: 'Desktop App',
    clientId: 'com.compta.desktop',
    clientSecret: 'sk_live_52N...',
    redirectUris: ['http://localhost:3000/oauth/callback'],
    scopes: ['read:profile', 'read:companies', 'write:invoices', 'read:accounting'],
    createdAt: new Date('2024-01-05T00:00:00'),
    active: true
  },
  {
    id: '3',
    name: 'Integration API',
    clientId: 'com.compta.api',
    clientSecret: 'sk_live_53P...',
    redirectUris: ['https://api.partner.com/callback'],
    scopes: ['read:profile', 'read:companies', 'write:invoices', 'read:accounting', 'write:accounting'],
    createdAt: new Date('2024-01-08T00:00:00'),
    active: false
  }
]

export const mockOAuthTokens: OAuthToken[] = [
  {
    id: 't1',
    applicationId: '1',
    applicationName: 'Mobile App',
    accessToken: 'ya29.a0AfH6SMB...',
    expiresIn: 3600,
    scopes: ['read:profile', 'read:companies'],
    createdAt: new Date('2024-01-09T08:00:00'),
    expiresAt: new Date('2024-01-09T09:00:00'),
    lastUsedAt: new Date('2024-01-09T08:30:00')
  },
  {
    id: 't2',
    applicationId: '2',
    applicationName: 'Desktop App',
    accessToken: 'ya29.a0AfH6SMB...',
    expiresIn: 7200,
    scopes: ['read:profile', 'read:companies', 'write:invoices'],
    createdAt: new Date('2024-01-09T07:00:00'),
    expiresAt: new Date('2024-01-09T09:00:00'),
    lastUsedAt: new Date('2024-01-09T07:30:00')
  },
  {
    id: 't3',
    applicationId: '1',
    applicationName: 'Mobile App',
    accessToken: 'ya29.a0AfH6SMB...',
    expiresIn: 3600,
    scopes: ['read:profile', 'read:companies'],
    createdAt: new Date('2024-01-08T10:00:00'),
    expiresAt: new Date('2024-01-08T11:00:00')
  }
]

export const mockOAuthConsents: OAuthConsent[] = [
  {
    id: 'c1',
    applicationId: '1',
    applicationName: 'Mobile App',
    scopes: ['read:profile', 'read:companies', 'write:invoices'],
    grantedAt: new Date('2024-01-01T00:00:00'),
    expiresAt: new Date('2024-12-31T23:59:59')
  },
  {
    id: 'c2',
    applicationId: '2',
    applicationName: 'Desktop App',
    scopes: ['read:profile', 'read:companies', 'write:invoices', 'read:accounting'],
    grantedAt: new Date('2024-01-05T00:00:00'),
    expiresAt: new Date('2024-12-31T23:59:59')
  }
]
