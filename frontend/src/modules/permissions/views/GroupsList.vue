<script setup lang="ts">
import { usePermissions } from '../composables/usePermissions'
import { Button } from '@/components/ui/button'
import {
  UsersRound,
  Plus,
  Edit,
  Trash2,
  Users,
  Shield
} from 'lucide-vue-next'

const { groups, users, isLoading, formatDate } = usePermissions()

const getMemberNames = (memberIds: string[]) => {
  return memberIds
    .map(id => users.value.find(u => u.id === id)?.fullName)
    .filter(Boolean)
    .slice(0, 3)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <UsersRound class="h-6 w-6" />
          Groupes
        </h1>
        <p class="text-muted-foreground">
          Organisez les utilisateurs en groupes
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau groupe
      </Button>
    </div>

    <!-- Groups Grid -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="group in groups"
        :key="group.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-900/30">
              <UsersRound class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <h3 class="font-semibold">{{ group.name }}</h3>
              <span class="text-xs text-muted-foreground">
                Cree le {{ formatDate(group.createdAt) }}
              </span>
            </div>
          </div>
          <div class="flex items-center gap-1">
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
          {{ group.description }}
        </p>

        <!-- Stats -->
        <div class="flex items-center gap-4 text-sm mb-4">
          <span class="flex items-center gap-1 text-muted-foreground">
            <Users class="h-4 w-4" />
            {{ group.members.length }} membre{{ group.members.length > 1 ? 's' : '' }}
          </span>
          <span class="flex items-center gap-1 text-muted-foreground">
            <Shield class="h-4 w-4" />
            {{ group.roles.length }} role{{ group.roles.length > 1 ? 's' : '' }}
          </span>
        </div>

        <!-- Members preview -->
        <div class="pt-4 border-t">
          <p class="text-xs font-medium text-muted-foreground mb-2">Membres</p>
          <div class="flex items-center gap-2">
            <div class="flex -space-x-2">
              <div
                v-for="name in getMemberNames(group.members)"
                :key="name"
                class="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground text-xs font-medium border-2 border-background"
                :title="name"
              >
                {{ name?.slice(0, 2).toUpperCase() }}
              </div>
            </div>
            <span
              v-if="group.members.length > 3"
              class="text-xs text-muted-foreground"
            >
              +{{ group.members.length - 3 }} autre{{ group.members.length - 3 > 1 ? 's' : '' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
