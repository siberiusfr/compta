export interface Notification {
  id: string
  title: string
  message: string
  type: 'info' | 'warning' | 'error' | 'success'
  read: boolean
  createdAt: Date
  sender?: string
}

export interface NotificationTemplate {
  id: string
  name: string
  subject: string
  body: string
  type: 'email' | 'sms' | 'push'
  variables: string[]
}

export interface NotificationSettings {
  emailEnabled: boolean
  smsEnabled: boolean
  pushEnabled: boolean
  frequency: 'immediate' | 'daily' | 'weekly'
}
