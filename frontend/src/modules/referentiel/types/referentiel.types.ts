import type {
  ProduitResponse,
  ProduitRequest,
  ClientResponse,
  ClientRequest,
  FournisseurResponse,
  FournisseurRequest,
  FamilleProduitResponse,
  FamilleProduitRequest,
} from '@/api/referentiel/gen/generated.schemas'

export type {
  ProduitResponse,
  ProduitRequest,
  ClientResponse,
  ClientRequest,
  FournisseurResponse,
  FournisseurRequest,
  FamilleProduitResponse,
  FamilleProduitRequest,
}

export interface ReferentielFilter {
  search?: string
  actif?: boolean
}
