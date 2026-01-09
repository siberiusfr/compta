export type CompanyStatus = 'active' | 'inactive' | 'pending'
export type CompanyType = 'sarl' | 'sas' | 'sa' | 'eurl' | 'ei' | 'auto-entrepreneur'

export interface Company {
  id: string
  name: string
  legalName: string
  type: CompanyType
  status: CompanyStatus
  siret: string
  siren: string
  vatNumber?: string
  capital?: number
  address: Address
  phone?: string
  email?: string
  website?: string
  logo?: string
  employeeCount: number
  fiscalYearEnd: string
  createdAt: Date
  updatedAt: Date
}

export interface Address {
  street: string
  city: string
  postalCode: string
  country: string
}

export interface CompanyContact {
  id: string
  companyId: string
  firstName: string
  lastName: string
  fullName: string
  role: string
  email: string
  phone?: string
  isPrimary: boolean
}

export interface CompanyFilter {
  status?: CompanyStatus
  type?: CompanyType
  search?: string
}
