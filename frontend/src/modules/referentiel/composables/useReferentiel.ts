import { storeToRefs } from 'pinia'
import { useQueryClient } from '@tanstack/vue-query'
import { useReferentielStore } from '../stores/referentielStore'

// Produits API
import {
  useGetAll as useGetAllProduits,
  useGetById as useGetProduitById,
  useCreate as useCreateProduit,
  useUpdate as useUpdateProduit,
  useDelete as useDeleteProduit,
  getGetAllQueryKey as getProduitsQueryKey,
} from '@/api/referentiel/gen/produits/produits'

// Clients API
import {
  useGetAll3 as useGetAllClients,
  useGetById3 as useGetClientById,
  useCreate3 as useCreateClient,
  useUpdate3 as useUpdateClient,
  useDelete3 as useDeleteClient,
  getGetAll3QueryKey as getClientsQueryKey,
} from '@/api/referentiel/gen/clients/clients'

// Fournisseurs API
import {
  useGetAll1 as useGetAllFournisseurs,
  useGetById1 as useGetFournisseurById,
  useCreate1 as useCreateFournisseur,
  useUpdate1 as useUpdateFournisseur,
  useDelete1 as useDeleteFournisseur,
  getGetAll1QueryKey as getFournisseursQueryKey,
} from '@/api/referentiel/gen/fournisseurs/fournisseurs'

// Familles Produits API
import {
  useGetAll2 as useGetAllFamilles,
  useGetById2 as useGetFamilleById,
  useCreate2 as useCreateFamille,
  useUpdate2 as useUpdateFamille,
  useDelete2 as useDeleteFamille,
  getGetAll2QueryKey as getFamillesQueryKey,
} from '@/api/referentiel/gen/familles-produits/familles-produits'

export function useReferentiel() {
  const store = useReferentielStore()
  const { companyId, filter } = storeToRefs(store)
  const queryClient = useQueryClient()

  const formatCurrency = (value?: number): string => {
    if (value === undefined || value === null) return '-'
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
    }).format(value)
  }

  const formatDate = (date?: string): string => {
    if (!date) return '-'
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(date))
  }

  const formatPercentage = (value?: number): string => {
    if (value === undefined || value === null) return '-'
    return `${value}%`
  }

  const getStatusColor = (actif?: boolean): string => {
    return actif
      ? 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30'
      : 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30'
  }

  const getStatusLabel = (actif?: boolean): string => {
    return actif ? 'Actif' : 'Inactif'
  }

  const getTypeArticleLabel = (type?: string): string => {
    const labels: Record<string, string> = {
      PRODUIT: 'Produit',
      SERVICE: 'Service',
    }
    return labels[type ?? ''] ?? type ?? '-'
  }

  const getTypeStockLabel = (type?: string): string => {
    const labels: Record<string, string> = {
      STOCKABLE: 'Stockable',
      NON_STOCKABLE: 'Non stockable',
    }
    return labels[type ?? ''] ?? type ?? '-'
  }

  const getTypeClientLabel = (type?: string): string => {
    const labels: Record<string, string> = {
      PARTICULIER: 'Particulier',
      ENTREPRISE: 'Entreprise',
    }
    return labels[type ?? ''] ?? type ?? '-'
  }

  const invalidateProduits = () => {
    queryClient.invalidateQueries({ queryKey: getProduitsQueryKey(companyId.value) })
  }

  const invalidateClients = () => {
    queryClient.invalidateQueries({ queryKey: getClientsQueryKey(companyId.value) })
  }

  const invalidateFournisseurs = () => {
    queryClient.invalidateQueries({ queryKey: getFournisseursQueryKey(companyId.value) })
  }

  const invalidateFamilles = () => {
    queryClient.invalidateQueries({ queryKey: getFamillesQueryKey(companyId.value) })
  }

  return {
    companyId,
    filter,
    setFilter: store.setFilter,
    clearFilter: store.clearFilter,
    formatCurrency,
    formatDate,
    formatPercentage,
    getStatusColor,
    getStatusLabel,
    getTypeArticleLabel,
    getTypeStockLabel,
    getTypeClientLabel,
    // Produits
    useGetAllProduits,
    useGetProduitById,
    useCreateProduit,
    useUpdateProduit,
    useDeleteProduit,
    invalidateProduits,
    // Clients
    useGetAllClients,
    useGetClientById,
    useCreateClient,
    useUpdateClient,
    useDeleteClient,
    invalidateClients,
    // Fournisseurs
    useGetAllFournisseurs,
    useGetFournisseurById,
    useCreateFournisseur,
    useUpdateFournisseur,
    useDeleteFournisseur,
    invalidateFournisseurs,
    // Familles
    useGetAllFamilles,
    useGetFamilleById,
    useCreateFamille,
    useUpdateFamille,
    useDeleteFamille,
    invalidateFamilles,
  }
}
