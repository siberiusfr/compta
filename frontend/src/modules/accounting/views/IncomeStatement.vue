<script setup lang="ts">
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import { TrendingUp, Download, Calendar, ArrowUp, ArrowDown } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { totalRevenue, totalExpenses, netIncome, isLoading, formatCurrency, currentFiscalYear } =
  useAccounting()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <TrendingUp class="h-6 w-6" />
          Compte de resultat
        </h1>
        <p class="text-muted-foreground flex items-center gap-2">
          <Calendar class="h-4 w-4" />
          {{ currentFiscalYear?.name }}
        </p>
      </div>
      <Button variant="outline">
        <Download class="h-4 w-4 mr-2" />
        Exporter
      </Button>
    </div>

    <!-- Summary Cards -->
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-xl border bg-card p-5">
        <div class="flex items-center gap-3 mb-2">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-100 dark:bg-green-900/30"
          >
            <ArrowUp class="h-5 w-5 text-green-600 dark:text-green-400" />
          </div>
          <span class="text-sm text-muted-foreground">Produits</span>
        </div>
        <p class="text-2xl font-bold text-green-600">{{ formatCurrency(totalRevenue) }}</p>
      </div>

      <div class="rounded-xl border bg-card p-5">
        <div class="flex items-center gap-3 mb-2">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-lg bg-red-100 dark:bg-red-900/30"
          >
            <ArrowDown class="h-5 w-5 text-red-600 dark:text-red-400" />
          </div>
          <span class="text-sm text-muted-foreground">Charges</span>
        </div>
        <p class="text-2xl font-bold text-red-600">{{ formatCurrency(totalExpenses) }}</p>
      </div>

      <div class="rounded-xl border bg-card p-5">
        <div class="flex items-center gap-3 mb-2">
          <div
            :class="
              cn(
                'flex h-10 w-10 items-center justify-center rounded-lg',
                netIncome >= 0 ? 'bg-blue-100 dark:bg-blue-900/30' : 'bg-red-100 dark:bg-red-900/30'
              )
            "
          >
            <TrendingUp
              :class="
                cn(
                  'h-5 w-5',
                  netIncome >= 0
                    ? 'text-blue-600 dark:text-blue-400'
                    : 'text-red-600 dark:text-red-400'
                )
              "
            />
          </div>
          <span class="text-sm text-muted-foreground">Resultat net</span>
        </div>
        <p :class="cn('text-2xl font-bold', netIncome >= 0 ? 'text-blue-600' : 'text-red-600')">
          {{ formatCurrency(netIncome) }}
        </p>
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <!-- Income Statement -->
    <div
      v-else
      class="rounded-xl border bg-card overflow-hidden"
    >
      <table class="w-full">
        <!-- PRODUITS -->
        <thead class="bg-green-100 dark:bg-green-900/30">
          <tr>
            <th
              colspan="2"
              class="text-left p-4 font-bold text-green-800 dark:text-green-200"
            >
              PRODUITS D'EXPLOITATION
            </th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr class="hover:bg-muted/30">
            <td class="p-4">Ventes de produits finis</td>
            <td class="p-4 text-right font-medium">520 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Prestations de services</td>
            <td class="p-4 text-right font-medium">156 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Produits des activites annexes</td>
            <td class="p-4 text-right font-medium">20 000,00 EUR</td>
          </tr>
        </tbody>
        <tfoot class="bg-green-50 dark:bg-green-900/20 font-semibold">
          <tr>
            <td class="p-4">Total des produits</td>
            <td class="p-4 text-right text-green-700 dark:text-green-300">
              {{ formatCurrency(totalRevenue) }}
            </td>
          </tr>
        </tfoot>

        <!-- CHARGES -->
        <thead class="bg-red-100 dark:bg-red-900/30">
          <tr>
            <th
              colspan="2"
              class="text-left p-4 font-bold text-red-800 dark:text-red-200"
            >
              CHARGES D'EXPLOITATION
            </th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr class="hover:bg-muted/30">
            <td class="p-4">Achats de matieres premieres</td>
            <td class="p-4 text-right font-medium">125 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Achats non stockes</td>
            <td class="p-4 text-right font-medium">15 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Locations</td>
            <td class="p-4 text-right font-medium">24 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Remunerations du personnel</td>
            <td class="p-4 text-right font-medium">280 000,00 EUR</td>
          </tr>
          <tr class="hover:bg-muted/30">
            <td class="p-4">Charges de securite sociale</td>
            <td class="p-4 text-right font-medium">112 000,00 EUR</td>
          </tr>
        </tbody>
        <tfoot class="bg-red-50 dark:bg-red-900/20 font-semibold">
          <tr>
            <td class="p-4">Total des charges</td>
            <td class="p-4 text-right text-red-700 dark:text-red-300">
              {{ formatCurrency(totalExpenses) }}
            </td>
          </tr>
        </tfoot>

        <!-- RESULTAT -->
        <tfoot
          :class="
            cn(
              'font-bold text-lg',
              netIncome >= 0 ? 'bg-blue-100 dark:bg-blue-900/30' : 'bg-red-100 dark:bg-red-900/30'
            )
          "
        >
          <tr>
            <td class="p-4">RESULTAT NET</td>
            <td
              :class="
                cn(
                  'p-4 text-right',
                  netIncome >= 0
                    ? 'text-blue-700 dark:text-blue-300'
                    : 'text-red-700 dark:text-red-300'
                )
              "
            >
              {{ formatCurrency(netIncome) }}
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>
