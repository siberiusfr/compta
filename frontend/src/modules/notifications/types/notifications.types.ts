export type NotificationType = 'info' | 'warning' | 'error' | 'success'
export type NotificationPriority = 'low' | 'medium' | 'high' | 'urgent'

export interface Notification {
  id: string
  title: string
  message: string
  type: NotificationType
  priority: NotificationPriority
  read: boolean
  archived: boolean
  createdAt: Date
  sender?: string
  link?: string
  category: string
}

export interface NotificationTemplate {
  id: string
  name: string
  subject: string
  body: string
  type: NotificationType
  variables: string[]
  createdAt: Date
  updatedAt: Date
}

export interface NotificationSettings {
  emailEnabled: boolean
  pushEnabled: boolean
  smsEnabled: boolean
  categories: {
    [key: string]: {
      email: boolean
      push: boolean
      sms: boolean
    }
  }
}

export interface SentNotification {
  id: string
  templateId?: string
  recipients: string[]
  subject: string
  message: string
  sentAt: Date
  status: 'sent' | 'delivered' | 'failed'
  readCount: number
}
