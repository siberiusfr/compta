<script setup lang="ts">
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import {
  PieChart,
  Download,
  Calendar
} from 'lucide-vue-next'

const {
  totalAssets,
  totalLiabilities,
  totalEquity,
  isLoading,
  formatCurrency,
  currentFiscalYear
} = useAccounting()
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <PieChart class="h-6 w-6" />
          Bilan
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

    <!-- Loading -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <!-- Balance Sheet -->
    <div v-else class="grid gap-6 lg:grid-cols-2">
      <!-- ACTIF -->
      <div class="rounded-xl border bg-card overflow-hidden">
        <div class="p-4 bg-blue-100 dark:bg-blue-900/30 border-b">
          <h2 class="text-lg font-bold text-blue-800 dark:text-blue-200">ACTIF</h2>
        </div>
        <div class="p-4 space-y-4">
          <!-- Actif immobilise -->
          <div>
            <h3 class="font-semibold text-sm text-muted-foreground mb-2">Actif immobilise</h3>
            <div class="space-y-2">
              <div class="flex justify-between py-1">
                <span>Immobilisations corporelles</span>
                <span class="font-medium">205 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Immobilisations incorporelles</span>
                <span class="font-medium">15 000,00 EUR</span>
              </div>
            </div>
            <div class="flex justify-between py-2 border-t mt-2 font-semibold">
              <span>Total actif immobilise</span>
              <span>220 000,00 EUR</span>
            </div>
          </div>

          <!-- Actif circulant -->
          <div>
            <h3 class="font-semibold text-sm text-muted-foreground mb-2">Actif circulant</h3>
            <div class="space-y-2">
              <div class="flex justify-between py-1">
                <span>Stocks</span>
                <span class="font-medium">45 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Creances clients</span>
                <span class="font-medium">78 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Disponibilites</span>
                <span class="font-medium">87 500,00 EUR</span>
              </div>
            </div>
            <div class="flex justify-between py-2 border-t mt-2 font-semibold">
              <span>Total actif circulant</span>
              <span>210 500,00 EUR</span>
            </div>
          </div>
        </div>
        <div class="p-4 bg-blue-50 dark:bg-blue-900/20 border-t">
          <div class="flex justify-between text-lg font-bold text-blue-800 dark:text-blue-200">
            <span>TOTAL ACTIF</span>
            <span>{{ formatCurrency(totalAssets) }}</span>
          </div>
        </div>
      </div>

      <!-- PASSIF -->
      <div class="rounded-xl border bg-card overflow-hidden">
        <div class="p-4 bg-green-100 dark:bg-green-900/30 border-b">
          <h2 class="text-lg font-bold text-green-800 dark:text-green-200">PASSIF</h2>
        </div>
        <div class="p-4 space-y-4">
          <!-- Capitaux propres -->
          <div>
            <h3 class="font-semibold text-sm text-muted-foreground mb-2">Capitaux propres</h3>
            <div class="space-y-2">
              <div class="flex justify-between py-1">
                <span>Capital social</span>
                <span class="font-medium">100 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Reserves</span>
                <span class="font-medium">25 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Resultat de l'exercice</span>
                <span class="font-medium">45 000,00 EUR</span>
              </div>
            </div>
            <div class="flex justify-between py-2 border-t mt-2 font-semibold">
              <span>Total capitaux propres</span>
              <span>{{ formatCurrency(totalEquity) }}</span>
            </div>
          </div>

          <!-- Dettes -->
          <div>
            <h3 class="font-semibold text-sm text-muted-foreground mb-2">Dettes</h3>
            <div class="space-y-2">
              <div class="flex justify-between py-1">
                <span>Dettes fournisseurs</span>
                <span class="font-medium">45 000,00 EUR</span>
              </div>
              <div class="flex justify-between py-1">
                <span>Dettes fiscales et sociales</span>
                <span class="font-medium">35 500,00 EUR</span>
              </div>
            </div>
            <div class="flex justify-between py-2 border-t mt-2 font-semibold">
              <span>Total dettes</span>
              <span>{{ formatCurrency(totalLiabilities) }}</span>
            </div>
          </div>
        </div>
        <div class="p-4 bg-green-50 dark:bg-green-900/20 border-t">
          <div class="flex justify-between text-lg font-bold text-green-800 dark:text-green-200">
            <span>TOTAL PASSIF</span>
            <span>{{ formatCurrency(totalEquity + totalLiabilities) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
