import type { Company, CompanyContact } from '../types/companies.types'

export const mockCompanies: Company[] = [
  {
    id: 'company-1',
    name: 'Tech Solutions',
    legalName: 'Tech Solutions SARL',
    type: 'sarl',
    status: 'active',
    siret: '12345678901234',
    siren: '123456789',
    vatNumber: 'FR12345678901',
    capital: 50000,
    address: {
      street: '15 rue de la Technologie',
      city: 'Paris',
      postalCode: '75001',
      country: 'France'
    },
    phone: '+33 1 23 45 67 89',
    email: 'contact@techsolutions.fr',
    website: 'https://techsolutions.fr',
    employeeCount: 45,
    fiscalYearEnd: '12-31',
    createdAt: new Date('2020-03-15'),
    updatedAt: new Date('2024-06-01')
  },
  {
    id: 'company-2',
    name: 'Digital Factory',
    legalName: 'Digital Factory SAS',
    type: 'sas',
    status: 'active',
    siret: '98765432109876',
    siren: '987654321',
    vatNumber: 'FR98765432109',
    capital: 100000,
    address: {
      street: '42 avenue de l\'Innovation',
      city: 'Lyon',
      postalCode: '69001',
      country: 'France'
    },
    phone: '+33 4 56 78 90 12',
    email: 'hello@digitalfactory.fr',
    website: 'https://digitalfactory.fr',
    employeeCount: 78,
    fiscalYearEnd: '06-30',
    createdAt: new Date('2018-09-01'),
    updatedAt: new Date('2024-05-15')
  },
  {
    id: 'company-3',
    name: 'Green Energy',
    legalName: 'Green Energy SA',
    type: 'sa',
    status: 'active',
    siret: '45678901234567',
    siren: '456789012',
    capital: 500000,
    address: {
      street: '8 place de l\'Ecologie',
      city: 'Bordeaux',
      postalCode: '33000',
      country: 'France'
    },
    phone: '+33 5 67 89 01 23',
    email: 'info@greenenergy.fr',
    employeeCount: 120,
    fiscalYearEnd: '12-31',
    createdAt: new Date('2015-01-10'),
    updatedAt: new Date('2024-04-20')
  },
  {
    id: 'company-4',
    name: 'Consulting Plus',
    legalName: 'Consulting Plus EURL',
    type: 'eurl',
    status: 'pending',
    siret: '11223344556677',
    siren: '112233445',
    address: {
      street: '3 rue du Conseil',
      city: 'Toulouse',
      postalCode: '31000',
      country: 'France'
    },
    email: 'contact@consultingplus.fr',
    employeeCount: 8,
    fiscalYearEnd: '12-31',
    createdAt: new Date('2024-01-05'),
    updatedAt: new Date('2024-01-05')
  }
]

export const mockContacts: CompanyContact[] = [
  {
    id: 'contact-1',
    companyId: 'company-1',
    firstName: 'Jean',
    lastName: 'Dupont',
    fullName: 'Jean Dupont',
    role: 'Directeur General',
    email: 'jean.dupont@techsolutions.fr',
    phone: '+33 6 12 34 56 78',
    isPrimary: true
  },
  {
    id: 'contact-2',
    companyId: 'company-1',
    firstName: 'Marie',
    lastName: 'Martin',
    fullName: 'Marie Martin',
    role: 'Directrice Financiere',
    email: 'marie.martin@techsolutions.fr',
    phone: '+33 6 98 76 54 32',
    isPrimary: false
  },
  {
    id: 'contact-3',
    companyId: 'company-2',
    firstName: 'Pierre',
    lastName: 'Bernard',
    fullName: 'Pierre Bernard',
    role: 'PDG',
    email: 'pierre.bernard@digitalfactory.fr',
    phone: '+33 6 11 22 33 44',
    isPrimary: true
  }
]
