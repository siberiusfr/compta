<script setup lang="ts">
import { usePermissions } from '../composables/usePermissions'
import { Button } from '@/components/ui/button'
import {
  Users,
  Plus,
  Search,
  Filter,
  Mail,
  Shield,
  Clock,
  Edit,
  Trash2,
  UserCheck,
  UserX
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  users,
  isLoading,
  userCount,
  pendingUsers,
  formatRelativeTime,
  getStatusColor,
  getStatusLabel,
  getInitials,
  updateUserStatus
} = usePermissions()

const handleActivate = (userId: string) => {
  updateUserStatus(userId, 'active')
}

const handleDeactivate = (userId: string) => {
  updateUserStatus(userId, 'inactive')
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Users class="h-6 w-6" />
          Utilisateurs
        </h1>
        <p class="text-muted-foreground">
          {{ userCount }} utilisateurs - {{ pendingUsers.length }} en attente
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Inviter un utilisateur
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher un utilisateur..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button variant="outline" size="icon">
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Users List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="user in users"
        :key="user.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-center gap-4">
          <!-- Avatar -->
          <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary text-primary-foreground font-semibold">
            {{ getInitials(user.fullName) }}
          </div>

          <!-- Info -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold">{{ user.fullName }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(user.status))">
                {{ getStatusLabel(user.status) }}
              </span>
            </div>
            <div class="flex items-center gap-4 mt-1 text-sm text-muted-foreground">
              <span class="flex items-center gap-1">
                <Mail class="h-4 w-4" />
                {{ user.email }}
              </span>
              <span class="flex items-center gap-1">
                <Clock class="h-4 w-4" />
                {{ formatRelativeTime(user.lastLogin) }}
              </span>
            </div>
          </div>

          <!-- Roles -->
          <div class="flex items-center gap-2">
            <Shield class="h-4 w-4 text-muted-foreground" />
            <div class="flex flex-wrap gap-1">
              <span
                v-for="role in user.roles"
                :key="role"
                class="text-xs px-2 py-1 rounded-full bg-primary/10 text-primary"
              >
                {{ role }}
              </span>
              <span
                v-if="user.roles.length === 0"
                class="text-xs text-muted-foreground"
              >
                Aucun role
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1">
            <Button
              v-if="user.status === 'pending'"
              variant="ghost"
              size="icon-sm"
              title="Activer"
              @click="handleActivate(user.id)"
            >
              <UserCheck class="h-4 w-4 text-green-600" />
            </Button>
            <Button
              v-else-if="user.status === 'active'"
              variant="ghost"
              size="icon-sm"
              title="Desactiver"
              @click="handleDeactivate(user.id)"
            >
              <UserX class="h-4 w-4 text-yellow-600" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Modifier">
              <Edit class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Supprimer" class="text-destructive hover:text-destructive">
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
