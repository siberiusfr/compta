/**
 * User-facing messages and error messages
 */

export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Erreur de connexion au serveur',
  UNAUTHORIZED: 'Vous devez être connecté pour accéder à cette page',
  FORBIDDEN: "Vous n'avez pas les permissions nécessaires",
  NOT_FOUND: 'La ressource demandée est introuvable',
  SERVER_ERROR: 'Une erreur serveur est survenue',
  VALIDATION_ERROR: 'Veuillez vérifier les informations saisies',
  TIMEOUT: 'La requête a expiré',
  UNKNOWN: 'Une erreur inconnue est survenue',
} as const

export const SUCCESS_MESSAGES = {
  LOGIN: 'Connexion réussie',
  LOGOUT: 'Déconnexion réussie',
  REGISTER: 'Inscription réussie',
  CREATED: 'Élément créé avec succès',
  UPDATED: 'Élément mis à jour avec succès',
  DELETED: 'Élément supprimé avec succès',
  UPLOADED: 'Fichier téléversé avec succès',
  SAVED: 'Sauvegarde réussie',
} as const

export const CONFIRMATION_MESSAGES = {
  DELETE: 'Êtes-vous sûr de vouloir supprimer cet élément ?',
  LOGOUT: 'Voulez-vous vraiment vous déconnecter ?',
  CANCEL: 'Voulez-vous annuler les modifications en cours ?',
  DISCARD: 'Les modifications non sauvegardées seront perdues. Continuer ?',
} as const

export const INFO_MESSAGES = {
  LOADING: 'Chargement en cours...',
  NO_DATA: 'Aucune donnée disponible',
  EMPTY_LIST: 'La liste est vide',
  SEARCH_NO_RESULTS: 'Aucun résultat trouvé',
} as const
