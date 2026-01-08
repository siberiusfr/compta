export interface Company {
  id: string
  name: string
  siret: string
  siren: string
  address: string
  city: string
  postalCode: string
  country: string
  phone: string
  email: string
  vatNumber: string
  logo?: string
  createdAt: Date
  active: boolean
}

export interface CompanySettings {
  fiscalYearStart: string
  currency: string
  language: string
  invoicePrefix: string
  quotePrefix: string
  paymentTerms: number
  vatRate: number
}
