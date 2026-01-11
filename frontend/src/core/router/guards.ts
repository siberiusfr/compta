import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

/**
 * Mapping des roles OAuth2 vers les roles applicatifs
 * Le serveur OAuth2 retourne des roles comme 'ROLE_ADMIN', 'ADMIN', etc.
 * Les routes definissent des roles comme 'Administrateur', 'Utilisateur', etc.
 */
const ROLE_MAPPINGS: Record<string, string[]> = {
  // Role OAuth2 -> Roles applicatifs correspondants
  ROLE_ADMIN: ['Administrateur', 'Admin', 'ADMIN', 'ROLE_ADMIN'],
  ADMIN: ['Administrateur', 'Admin', 'ADMIN', 'ROLE_ADMIN'],
  ROLE_USER: ['Utilisateur', 'User', 'USER', 'ROLE_USER'],
  USER: ['Utilisateur', 'User', 'USER', 'ROLE_USER'],
  ROLE_MANAGER: ['Manager', 'MANAGER', 'ROLE_MANAGER'],
  MANAGER: ['Manager', 'MANAGER', 'ROLE_MANAGER'],
}

/**
 * Verifie si l'utilisateur a l'un des roles requis
 * Supporte plusieurs formats de roles (ROLE_ADMIN, ADMIN, Administrateur, etc.)
 */
function hasRole(userRoles: string[], requiredRoles: string[]): boolean {
  // Normalise les roles utilisateur en ajoutant tous les equivalents
  const expandedUserRoles = new Set<string>()

  for (const role of userRoles) {
    // Ajoute le role original
    expandedUserRoles.add(role)
    expandedUserRoles.add(role.toUpperCase())
    expandedUserRoles.add(role.toLowerCase())

    // Ajoute les equivalents depuis le mapping
    const mappings = ROLE_MAPPINGS[role] ?? ROLE_MAPPINGS[role.toUpperCase()]
    if (mappings) {
      mappings.forEach((r) => expandedUserRoles.add(r))
    }
  }

  // Verifie si au moins un role requis est present
  return requiredRoles.some((role) => expandedUserRoles.has(role))
}

export async function authGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> {
  const authStore = useAuthStore()

  console.log('[AuthGuard] Navigating to:', to.path, 'meta:', to.meta)

  // Wait for auth initialization with timeout
  const maxWait = 5000
  const startTime = Date.now()

  while (authStore.isLoading && Date.now() - startTime < maxWait) {
    await new Promise((resolve) => setTimeout(resolve, 50))
  }

  console.log('[AuthGuard] Auth state:', {
    isLoading: authStore.isLoading,
    isAuthenticated: authStore.isAuthenticated,
    userProfile: authStore.userProfile,
  })

  // Skip auth check for callback routes
  if (to.meta.isCallback) {
    return next()
  }

  // Check if route requires authentication
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('[AuthGuard] Not authenticated, redirecting to login')
    // Store the return URL for post-login redirect
    sessionStorage.setItem('returnUrl', to.fullPath)
    return next({ name: 'login' })
  }

  // Check if route requires specific roles
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const profile = authStore.userProfile as Record<string, unknown> | null

    // Decode access token to get roles (Spring Security stores roles in access_token)
    let tokenClaims: Record<string, unknown> = {}
    const accessToken = authStore.accessToken
    if (accessToken) {
      try {
        const payload = accessToken.split('.')[1]
        if (payload) {
          tokenClaims = JSON.parse(atob(payload))
        }
      } catch (e) {
        console.warn('[AuthGuard] Failed to decode access token:', e)
      }
    }

    // Debug: affiche le profil et les claims du token
    console.log('[AuthGuard] ID Token profile:', JSON.stringify(profile, null, 2))
    console.log('[AuthGuard] Access Token claims:', JSON.stringify(tokenClaims, null, 2))

    // Cherche les rôles dans le token d'accès ou le profil
    const userRoles: string[] =
      (tokenClaims?.roles as string[]) ??
      (tokenClaims?.authorities as string[]) ??
      (profile?.roles as string[]) ??
      (profile?.authorities as string[]) ??
      []

    console.log('[AuthGuard] Detected user roles:', userRoles, 'Required:', to.meta.roles)

    if (!hasRole(userRoles, to.meta.roles as string[])) {
      console.warn('[AuthGuard] Access denied.')
      return next({ name: 'unauthorized' })
    }
  }

  // Redirect authenticated users away from auth pages
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return next({ name: 'dashboard' })
  }

  next()
}
