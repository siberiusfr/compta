<script setup lang="ts">
import { BarChart3 } from 'lucide-vue-next'
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Rapports</h1>
      <p class="text-gray-600 mt-1">Rapports et analyses financières</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-blue-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Rapport mensuel</h3>
        <p class="text-sm text-gray-600">Vue d'ensemble des activités du mois</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-green-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Analyse des revenus</h3>
        <p class="text-sm text-gray-600">Détail des sources de revenus</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-red-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Analyse des dépenses</h3>
        <p class="text-sm text-gray-600">Répartition des dépenses par catégorie</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-purple-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Analyse de trésorerie</h3>
        <p class="text-sm text-gray-600">Évolution de la trésorerie</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-yellow-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Analyse TVA</h3>
        <p class="text-sm text-gray-600">Rapport de TVA collectée et déductible</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
        <div class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center mb-4">
          <BarChart3 :size="24" class="text-indigo-600" />
        </div>
        <h3 class="font-semibold text-gray-900 mb-2">Bilan annuel</h3>
        <p class="text-sm text-gray-600">Bilan financier de l'exercice</p>
      </div>
    </div>
  </div>
</template>
