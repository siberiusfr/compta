<script setup lang="ts">
import { usePermissions } from '../composables/usePermissions'
import { Button } from '@/components/ui/button'
import {
  Shield,
  Plus,
  Edit,
  Trash2,
  Users,
  Lock,
  Key
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { roles, isLoading } = usePermissions()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Shield class="h-6 w-6" />
          Roles
        </h1>
        <p class="text-muted-foreground">
          Gerez les roles et permissions
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau role
      </Button>
    </div>

    <!-- Roles Grid -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="role in roles"
        :key="role.id"
        :class="cn(
          'rounded-xl border bg-card p-5 hover:shadow-md transition-shadow',
          role.isSystem && 'border-primary/50'
        )"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div :class="cn(
              'flex h-10 w-10 items-center justify-center rounded-lg',
              role.isSystem
                ? 'bg-primary text-primary-foreground'
                : 'bg-primary/10 text-primary'
            )">
              <Shield class="h-5 w-5" />
            </div>
            <div>
              <h3 class="font-semibold">{{ role.name }}</h3>
              <span
                v-if="role.isSystem"
                class="text-xs text-muted-foreground flex items-center gap-1"
              >
                <Lock class="h-3 w-3" />
                Role systeme
              </span>
            </div>
          </div>
          <div v-if="!role.isSystem" class="flex items-center gap-1">
            <Button variant="ghost" size="icon-sm" title="Modifier">
              <Edit class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Supprimer" class="text-destructive hover:text-destructive">
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>

        <!-- Description -->
        <p class="text-sm text-muted-foreground mb-4">
          {{ role.description }}
        </p>

        <!-- Stats -->
        <div class="flex items-center gap-4 text-sm">
          <span class="flex items-center gap-1 text-muted-foreground">
            <Users class="h-4 w-4" />
            {{ role.userCount }} utilisateur{{ role.userCount > 1 ? 's' : '' }}
          </span>
          <span class="flex items-center gap-1 text-muted-foreground">
            <Key class="h-4 w-4" />
            {{ role.permissions.length }} permission{{ role.permissions.length > 1 ? 's' : '' }}
          </span>
        </div>

        <!-- Permissions preview -->
        <div class="mt-4 pt-4 border-t">
          <p class="text-xs font-medium text-muted-foreground mb-2">Permissions</p>
          <div class="flex flex-wrap gap-1">
            <span
              v-for="permission in role.permissions.slice(0, 3)"
              :key="permission.id"
              class="text-xs px-2 py-1 rounded bg-muted"
            >
              {{ permission.name }}
            </span>
            <span
              v-if="role.permissions.length > 3"
              class="text-xs px-2 py-1 rounded bg-muted"
            >
              +{{ role.permissions.length - 3 }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
