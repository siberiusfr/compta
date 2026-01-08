import {
  UserManager,
  WebStorageStateStore,
  User,
  type UserManagerSettings,
} from 'oidc-client-ts'

const settings: UserManagerSettings = {
  authority: 'http://localhost:9000',
  client_id: 'public-client',
  redirect_uri: 'http://localhost:3000/authorized',
  post_logout_redirect_uri: 'http://localhost:3000',
  response_type: 'code',
  scope: 'openid read write',

  // PKCE est active par defaut dans oidc-client-ts
  // Pas de client_secret car c'est un public client

  // Endpoints explicites (optionnel si discovery fonctionne)
  metadata: {
    issuer: 'http://localhost:9000',
    authorization_endpoint: 'http://localhost:9000/oauth2/authorize',
    token_endpoint: 'http://localhost:9000/oauth2/token',
    userinfo_endpoint: 'http://localhost:9000/userinfo',
    jwks_uri: 'http://localhost:9000/.well-known/jwks.json',
    end_session_endpoint: 'http://localhost:9000/logout',
  },

  // Storage
  userStore: new WebStorageStateStore({ store: window.localStorage }),

  // Refresh token
  automaticSilentRenew: true,
  silentRequestTimeoutInSeconds: 10,

  // Comportement
  filterProtocolClaims: true,
  loadUserInfo: true,
}

class AuthService {
  private userManager: UserManager

  constructor() {
    this.userManager = new UserManager(settings)

    // Event listeners pour le refresh automatique
    this.userManager.events.addAccessTokenExpiring(() => {
      console.log('[Auth] Token expiring, refreshing...')
    })

    this.userManager.events.addAccessTokenExpired(() => {
      console.log('[Auth] Token expired')
    })

    this.userManager.events.addSilentRenewError((error) => {
      console.error('[Auth] Silent renew error:', error)
    })

    this.userManager.events.addUserLoaded((user) => {
      console.log('[Auth] User loaded:', user.profile.sub)
    })

    this.userManager.events.addUserUnloaded(() => {
      console.log('[Auth] User unloaded')
    })
  }

  /**
   * Demarre le flux de login OAuth2/OIDC
   * Redirige vers le serveur d'autorisation
   */
  async login(): Promise<void> {
    try {
      await this.userManager.signinRedirect()
    } catch (error) {
      console.error('[Auth] Login error:', error)
      throw error
    }
  }

  /**
   * Gere le callback apres l'authentification
   * Appele sur la page /authorized
   */
  async handleCallback(): Promise<User> {
    try {
      const user = await this.userManager.signinRedirectCallback()
      return user
    } catch (error) {
      console.error('[Auth] Callback error:', error)
      throw error
    }
  }

  /**
   * Deconnecte l'utilisateur
   * Redirige vers le endpoint de logout du serveur
   */
  async logout(): Promise<void> {
    try {
      await this.userManager.signoutRedirect()
    } catch (error) {
      console.error('[Auth] Logout error:', error)
      throw error
    }
  }

  /**
   * Deconnecte localement sans redirection
   */
  async logoutLocal(): Promise<void> {
    await this.userManager.removeUser()
  }

  /**
   * Recupere l'utilisateur courant depuis le storage
   */
  async getUser(): Promise<User | null> {
    try {
      return await this.userManager.getUser()
    } catch (error) {
      console.error('[Auth] Get user error:', error)
      return null
    }
  }

  /**
   * Verifie si l'utilisateur est authentifie
   */
  async isAuthenticated(): Promise<boolean> {
    const user = await this.getUser()
    return !!user && !user.expired
  }

  /**
   * Recupere le token d'acces
   */
  async getAccessToken(): Promise<string | null> {
    const user = await this.getUser()
    return user?.access_token ?? null
  }

  /**
   * Force le refresh du token
   */
  async refreshToken(): Promise<User | null> {
    try {
      return await this.userManager.signinSilent()
    } catch (error) {
      console.error('[Auth] Refresh token error:', error)
      return null
    }
  }

  /**
   * Abonne un callback aux changements d'utilisateur
   */
  onUserLoaded(callback: (user: User) => void): void {
    this.userManager.events.addUserLoaded(callback)
  }

  /**
   * Abonne un callback a la deconnexion
   */
  onUserUnloaded(callback: () => void): void {
    this.userManager.events.addUserUnloaded(callback)
  }

  /**
   * Abonne un callback a l'expiration du token
   */
  onAccessTokenExpired(callback: () => void): void {
    this.userManager.events.addAccessTokenExpired(callback)
  }
}

// Export une instance singleton
export const authService = new AuthService()

// Export aussi le type User pour l'utiliser ailleurs
export type { User }
