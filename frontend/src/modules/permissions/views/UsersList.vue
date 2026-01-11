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
  UserX,
  Loader2,
  RefreshCw,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  users,
  isLoading,
  isMutating,
  userCount,
  pendingUsers,
  formatRelativeTime,
  getStatusColor,
  getStatusLabel,
  getInitials,
  updateUserStatus,
  searchQuery,
  setSearchQuery,
} = usePermissions()

const handleActivate = async (userId: string) => {
  await updateUserStatus(userId, 'active')
}

const handleDeactivate = async (userId: string) => {
  await updateUserStatus(userId, 'inactive')
}

const onSearchInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  setSearchQuery(target.value)
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
          :value="searchQuery"
          @input="onSearchInput"
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button
        variant="outline"
        size="icon"
      >
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Loading State -->
    <div
      v-if="isLoading"
      class="flex items-center justify-center py-12"
    >
      <Loader2 class="h-8 w-8 animate-spin text-muted-foreground" />
      <span class="ml-3 text-muted-foreground">Chargement des utilisateurs...</span>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="users.length === 0"
      class="text-center py-12"
    >
      <Users class="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
      <h3 class="text-lg font-medium">Aucun utilisateur</h3>
      <p class="text-muted-foreground mt-1">Commencez par inviter votre premier utilisateur.</p>
      <Button class="mt-4">
        <Plus class="h-4 w-4 mr-2" />
        Inviter un utilisateur
      </Button>
    </div>

    <!-- Users List -->
    <div
      v-else
      class="space-y-3"
    >
      <!-- Mutation Overlay -->
      <div
        v-if="isMutating"
        class="fixed inset-0 bg-background/50 backdrop-blur-sm z-50 flex items-center justify-center"
      >
        <div class="flex items-center gap-3 bg-card px-6 py-4 rounded-lg shadow-lg">
          <RefreshCw class="h-5 w-5 animate-spin" />
          <span>Operation en cours...</span>
        </div>
      </div>

      <div
        v-for="user in users"
        :key="user.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-center gap-4">
          <!-- Avatar -->
          <div
            class="flex h-12 w-12 items-center justify-center rounded-full bg-primary text-primary-foreground font-semibold"
          >
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
              v-if="user.status === 'pending' || user.status === 'inactive'"
              variant="ghost"
              size="icon-sm"
              title="Activer"
              :disabled="isMutating"
              @click="handleActivate(user.id)"
            >
              <UserCheck class="h-4 w-4 text-green-600" />
            </Button>
            <Button
              v-else-if="user.status === 'active'"
              variant="ghost"
              size="icon-sm"
              title="Desactiver"
              :disabled="isMutating"
              @click="handleDeactivate(user.id)"
            >
              <UserX class="h-4 w-4 text-yellow-600" />
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
              title="Modifier"
              :disabled="isMutating"
            >
              <Edit class="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
              title="Supprimer"
              class="text-destructive hover:text-destructive"
              :disabled="isMutating"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
