<script setup lang="ts">
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import { BookMarked, Plus, Search, Filter, Check, Eye, Calendar } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  journalEntries,
  draftEntries,
  isLoading,
  formatCurrency,
  formatDate,
  getStatusColor,
  getStatusLabel,
  postEntry,
} = useAccounting()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <BookMarked class="h-6 w-6" />
          Ecritures comptables
        </h1>
        <p class="text-muted-foreground">{{ draftEntries.length }} ecriture(s) en brouillon</p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle ecriture
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher..."
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

    <!-- Journal Entries List -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <div
      v-else
      class="space-y-4"
    >
      <div
        v-for="entry in journalEntries"
        :key="entry.id"
        :class="
          cn(
            'rounded-xl border bg-card overflow-hidden',
            entry.status === 'draft' && 'border-l-4 border-l-yellow-500'
          )
        "
      >
        <!-- Entry Header -->
        <div class="p-4 bg-muted/30 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar class="h-4 w-4" />
              {{ formatDate(entry.date) }}
            </div>
            <span class="font-mono text-sm font-medium bg-muted px-2 py-1 rounded">
              {{ entry.reference }}
            </span>
            <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(entry.status))">
              {{ getStatusLabel(entry.status) }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <Button
              v-if="entry.status === 'draft'"
              variant="outline"
              size="sm"
              @click="postEntry(entry.id)"
            >
              <Check class="h-4 w-4 mr-1" />
              Comptabiliser
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
            >
              <Eye class="h-4 w-4" />
            </Button>
          </div>
        </div>

        <!-- Entry Description -->
        <div class="px-4 py-2 border-b">
          <p class="font-medium">{{ entry.description }}</p>
          <p class="text-xs text-muted-foreground">Cree par {{ entry.createdBy }}</p>
        </div>

        <!-- Entry Lines -->
        <table class="w-full text-sm">
          <thead class="bg-muted/20">
            <tr>
              <th class="text-left p-2 font-medium">Compte</th>
              <th class="text-left p-2 font-medium">Libelle</th>
              <th class="text-right p-2 font-medium">Debit</th>
              <th class="text-right p-2 font-medium">Credit</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr
              v-for="line in entry.lines"
              :key="line.id"
              class="hover:bg-muted/20"
            >
              <td class="p-2">
                <span class="font-mono">{{ line.accountCode }}</span>
                <span class="ml-2 text-muted-foreground">{{ line.accountName }}</span>
              </td>
              <td class="p-2 text-muted-foreground">{{ line.description || '-' }}</td>
              <td class="p-2 text-right font-medium">
                {{ line.debit > 0 ? formatCurrency(line.debit) : '' }}
              </td>
              <td class="p-2 text-right font-medium">
                {{ line.credit > 0 ? formatCurrency(line.credit) : '' }}
              </td>
            </tr>
          </tbody>
          <tfoot class="bg-muted/30 font-semibold">
            <tr>
              <td
                colspan="2"
                class="p-2 text-right"
              >
                Total
              </td>
              <td class="p-2 text-right">{{ formatCurrency(entry.totalDebit) }}</td>
              <td class="p-2 text-right">{{ formatCurrency(entry.totalCredit) }}</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  </div>
</template>
