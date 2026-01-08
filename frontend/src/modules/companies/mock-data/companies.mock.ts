import type { Company, CompanySettings } from '../types/companies.types'

export const mockCompanies: Company[] = [
  {
    id: '1',
    name: 'TechCorp SAS',
    siret: '123 456 789 00012',
    siren: '123 456 789',
    address: '15 Rue de la Paix',
    city: 'Paris',
    postalCode: '75002',
    country: 'France',
    phone: '+33 1 23 45 67 89',
    email: 'contact@techcorp.com',
    vatNumber: 'FR12345678901',
    createdAt: new Date('2024-01-01T00:00:00'),
    active: true
  },
  {
    id: '2',
    name: 'StartupXYZ',
    siret: '987 654 321 00034',
    siren: '987 654 321',
    address: '42 Avenue des Champs-Élysées',
    city: 'Paris',
    postalCode: '75008',
    country: 'France',
    phone: '+33 1 98 76 54 32',
    email: 'info@startupxyz.com',
    vatNumber: 'FR98765432101',
    createdAt: new Date('2024-01-05T00:00:00'),
    active: true
  },
  {
    id: '3',
    name: 'InnovateLab SARL',
    siret: '456 789 123 00056',
    siren: '456 789 123',
    address: '8 Boulevard Haussmann',
    city: 'Paris',
    postalCode: '75009',
    country: 'France',
    phone: '+33 1 45 67 89 12',
    email: 'hello@innovatelab.com',
    vatNumber: 'FR45678912301',
    createdAt: new Date('2024-01-08T00:00:00'),
    active: false
  }
]

export const mockCompanySettings: CompanySettings = {
  fiscalYearStart: '01-01',
  currency: 'EUR',
  language: 'fr',
  invoicePrefix: 'FAC-',
  quotePrefix: 'DEV-',
  paymentTerms: 30,
  vatRate: 20
}
