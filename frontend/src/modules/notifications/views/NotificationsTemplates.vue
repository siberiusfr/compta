<script setup lang="ts">
import { useNotifications } from '../composables/useNotifications'
import { Button } from '@/components/ui/button'
import { FileText, Plus, Edit, Trash2, Copy, Tag } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { templates, formatDate, getTypeColor } = useNotifications()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileText class="h-6 w-6" />
          Modeles de notification
        </h1>
        <p class="text-muted-foreground">
          Gerez vos modeles de notification reutilisables
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau modele
      </Button>
    </div>

    <!-- Templates Grid -->
    <div v-if="templates.length === 0" class="text-center py-12">
      <FileText class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun modele</p>
      <p class="text-muted-foreground">Creez votre premier modele de notification</p>
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="template in templates"
        :key="template.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-3">
          <div :class="cn('flex h-10 w-10 items-center justify-center rounded-lg', getTypeColor(template.type))">
            <FileText class="h-5 w-5" />
          </div>
          <div class="flex items-center gap-1">
            <Button variant="ghost" size="icon-sm" title="Dupliquer">
              <Copy class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Modifier">
              <Edit class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Supprimer" class="text-destructive hover:text-destructive">
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>

        <!-- Content -->
        <h3 class="font-semibold mb-1">{{ template.name }}</h3>
        <p class="text-sm text-muted-foreground mb-3">{{ template.subject }}</p>

        <!-- Variables -->
        <div class="flex flex-wrap gap-1 mb-4">
          <span
            v-for="variable in template.variables.slice(0, 3)"
            :key="variable"
            class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground"
          >
            <Tag class="h-3 w-3" />
            {{ variable }}
          </span>
          <span
            v-if="template.variables.length > 3"
            class="text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground"
          >
            +{{ template.variables.length - 3 }}
          </span>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between text-xs text-muted-foreground pt-3 border-t">
          <span>Cree le {{ formatDate(template.createdAt) }}</span>
          <span :class="cn('px-2 py-1 rounded-full', getTypeColor(template.type))">
            {{ template.type }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
