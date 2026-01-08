import type { Notification, NotificationTemplate, NotificationSettings } from '../types/notifications.types'

export const mockNotifications: Notification[] = [
  {
    id: '1',
    title: 'Nouvelle facture reçue',
    message: 'Facture #INV-2024-001 de TechCorp (12 500€)',
    type: 'info',
    read: false,
    createdAt: new Date('2024-01-09T10:30:00'),
    sender: 'Système'
  },
  {
    id: '2',
    title: 'Alerte: Délai dépassé',
    message: 'La facture #INV-2024-023 est en retard de paiement',
    type: 'warning',
    read: false,
    createdAt: new Date('2024-01-09T09:15:00'),
    sender: 'Système'
  },
  {
    id: '3',
    title: 'Erreur de synchronisation',
    message: 'Échec de la synchronisation avec le service bancaire',
    type: 'error',
    read: true,
    createdAt: new Date('2024-01-08T16:45:00'),
    sender: 'Système'
  },
  {
    id: '4',
    title: 'Paiement reçu',
    message: 'Paiement de 5 000€ reçu de StartupXYZ',
    type: 'success',
    read: true,
    createdAt: new Date('2024-01-08T14:20:00'),
    sender: 'Système'
  },
  {
    id: '5',
    title: 'Rappel de congé',
    message: 'Votre demande de congé a été approuvée',
    type: 'info',
    read: true,
    createdAt: new Date('2024-01-08T11:00:00'),
    sender: 'Marie Dupont'
  }
]

export const mockSentNotifications: Notification[] = [
  {
    id: 's1',
    title: 'Rappel de paiement',
    message: 'Rappel pour la facture #INV-2024-015',
    type: 'info',
    read: true,
    createdAt: new Date('2024-01-08T15:30:00'),
    sender: 'Vous'
  },
  {
    id: 's2',
    title: 'Invitation à collaborer',
    message: 'Invitation envoyée à Jean Martin',
    type: 'success',
    read: true,
    createdAt: new Date('2024-01-08T10:00:00'),
    sender: 'Vous'
  }
]

export const mockNotificationTemplates: NotificationTemplate[] = [
  {
    id: 't1',
    name: 'Rappel de facture',
    subject: 'Rappel: Facture {{invoiceNumber}} en attente de paiement',
    body: 'Bonjour {{name}},\n\nNous vous rappelons que votre facture {{invoiceNumber}} de {{amount}}€ est en attente de paiement.\n\nMerci de votre diligence.\n\nCordialement',
    type: 'email',
    variables: ['name', 'invoiceNumber', 'amount']
  },
  {
    id: 't2',
    name: 'Bienvenue',
    subject: 'Bienvenue sur Compta !',
    body: 'Bienvenue {{name}},\n\nVotre compte a été créé avec succès.\n\nConnectez-vous dès maintenant pour commencer.\n\nL\'équipe Compta',
    type: 'email',
    variables: ['name']
  },
  {
    id: 't3',
    name: 'Confirmation de commande',
    subject: 'Confirmation de commande #{{orderNumber}}',
    body: 'Votre commande #{{orderNumber}} a été confirmée.',
    type: 'sms',
    variables: ['orderNumber']
  }
]

export const mockNotificationSettings: NotificationSettings = {
  emailEnabled: true,
  smsEnabled: false,
  pushEnabled: true,
  frequency: 'immediate'
}
