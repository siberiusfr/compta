<script setup lang="ts">
import { ref } from 'vue'
import { useNotifications } from '../composables/useNotifications'
import { Button } from '@/components/ui/button'
import { Settings, Mail, Bell, Smartphone, Save } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { settings, updateSettings } = useNotifications()

const localSettings = ref({ ...settings.value })

const categories = [
  { key: 'invoices', label: 'Factures', description: 'Notifications liees aux factures' },
  { key: 'payments', label: 'Paiements', description: 'Notifications de paiements recus' },
  { key: 'hr', label: 'Ressources Humaines', description: 'Notifications RH' },
  { key: 'system', label: 'Systeme', description: 'Alertes systeme et maintenance' }
]

const saveSettings = () => {
  updateSettings(localSettings.value)
}

const toggleChannel = (channel: 'emailEnabled' | 'pushEnabled' | 'smsEnabled') => {
  localSettings.value[channel] = !localSettings.value[channel]
}

const toggleCategoryChannel = (category: string, channel: 'email' | 'push' | 'sms') => {
  if (!localSettings.value.categories[category]) {
    localSettings.value.categories[category] = { email: false, push: false, sms: false }
  }
  localSettings.value.categories[category][channel] = !localSettings.value.categories[category][channel]
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Settings class="h-6 w-6" />
          Parametres de notification
        </h1>
        <p class="text-muted-foreground">
          Configurez vos preferences de notification
        </p>
      </div>
      <Button @click="saveSettings">
        <Save class="h-4 w-4 mr-2" />
        Enregistrer
      </Button>
    </div>

    <!-- Global Settings -->
    <div class="rounded-xl border bg-card p-6">
      <h2 class="text-lg font-semibold mb-4">Canaux de notification</h2>
      <p class="text-sm text-muted-foreground mb-6">
        Activez ou desactivez les canaux de notification globaux
      </p>

      <div class="grid gap-4 md:grid-cols-3">
        <button
          :class="cn(
            'flex items-center gap-4 p-4 rounded-lg border transition-colors',
            localSettings.emailEnabled ? 'border-primary bg-primary/5' : 'border-border'
          )"
          @click="toggleChannel('emailEnabled')"
        >
          <div :class="cn(
            'flex h-12 w-12 items-center justify-center rounded-lg',
            localSettings.emailEnabled ? 'bg-primary text-primary-foreground' : 'bg-muted'
          )">
            <Mail class="h-6 w-6" />
          </div>
          <div class="text-left">
            <p class="font-medium">Email</p>
            <p class="text-sm text-muted-foreground">
              {{ localSettings.emailEnabled ? 'Active' : 'Desactive' }}
            </p>
          </div>
        </button>

        <button
          :class="cn(
            'flex items-center gap-4 p-4 rounded-lg border transition-colors',
            localSettings.pushEnabled ? 'border-primary bg-primary/5' : 'border-border'
          )"
          @click="toggleChannel('pushEnabled')"
        >
          <div :class="cn(
            'flex h-12 w-12 items-center justify-center rounded-lg',
            localSettings.pushEnabled ? 'bg-primary text-primary-foreground' : 'bg-muted'
          )">
            <Bell class="h-6 w-6" />
          </div>
          <div class="text-left">
            <p class="font-medium">Push</p>
            <p class="text-sm text-muted-foreground">
              {{ localSettings.pushEnabled ? 'Active' : 'Desactive' }}
            </p>
          </div>
        </button>

        <button
          :class="cn(
            'flex items-center gap-4 p-4 rounded-lg border transition-colors',
            localSettings.smsEnabled ? 'border-primary bg-primary/5' : 'border-border'
          )"
          @click="toggleChannel('smsEnabled')"
        >
          <div :class="cn(
            'flex h-12 w-12 items-center justify-center rounded-lg',
            localSettings.smsEnabled ? 'bg-primary text-primary-foreground' : 'bg-muted'
          )">
            <Smartphone class="h-6 w-6" />
          </div>
          <div class="text-left">
            <p class="font-medium">SMS</p>
            <p class="text-sm text-muted-foreground">
              {{ localSettings.smsEnabled ? 'Active' : 'Desactive' }}
            </p>
          </div>
        </button>
      </div>
    </div>

    <!-- Category Settings -->
    <div class="rounded-xl border bg-card p-6">
      <h2 class="text-lg font-semibold mb-4">Preferences par categorie</h2>
      <p class="text-sm text-muted-foreground mb-6">
        Personnalisez les notifications pour chaque categorie
      </p>

      <div class="space-y-4">
        <div
          v-for="category in categories"
          :key="category.key"
          class="flex items-center justify-between p-4 rounded-lg border"
        >
          <div>
            <p class="font-medium">{{ category.label }}</p>
            <p class="text-sm text-muted-foreground">{{ category.description }}</p>
          </div>

          <div class="flex items-center gap-2">
            <button
              :class="cn(
                'flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm transition-colors',
                localSettings.categories[category.key]?.email ? 'border-primary bg-primary/5 text-primary' : 'border-border text-muted-foreground'
              )"
              :disabled="!localSettings.emailEnabled"
              @click="toggleCategoryChannel(category.key, 'email')"
            >
              <Mail class="h-4 w-4" />
              Email
            </button>

            <button
              :class="cn(
                'flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm transition-colors',
                localSettings.categories[category.key]?.push ? 'border-primary bg-primary/5 text-primary' : 'border-border text-muted-foreground'
              )"
              :disabled="!localSettings.pushEnabled"
              @click="toggleCategoryChannel(category.key, 'push')"
            >
              <Bell class="h-4 w-4" />
              Push
            </button>

            <button
              :class="cn(
                'flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm transition-colors',
                localSettings.categories[category.key]?.sms ? 'border-primary bg-primary/5 text-primary' : 'border-border text-muted-foreground'
              )"
              :disabled="!localSettings.smsEnabled"
              @click="toggleCategoryChannel(category.key, 'sms')"
            >
              <Smartphone class="h-4 w-4" />
              SMS
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
