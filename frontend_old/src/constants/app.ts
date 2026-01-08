/**
 * Application-wide constants
 */

export const APP_NAME = import.meta.env.VITE_APP_NAME || 'Compta'
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '0.0.0'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
export const API_TIMEOUT = parseInt(import.meta.env.VITE_API_TIMEOUT || '30000')

export const ENABLE_DEVTOOLS = import.meta.env.VITE_ENABLE_DEVTOOLS === 'true'
