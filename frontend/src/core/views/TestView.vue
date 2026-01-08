<template>
  <div class="p-6">
    <h1 class="text-3xl font-bold mb-4">Page de Test</h1>
    
    <div class="space-y-4">
      <div class="bg-green-50 border border-green-200 rounded-lg p-4">
        <h2 class="font-bold text-green-800">✅ Vue fonctionne</h2>
        <p class="text-green-700">Le composant Vue est rendu correctement</p>
      </div>
      
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h2 class="font-bold text-blue-800">🔌 Pinia fonctionne</h2>
        <p class="text-blue-700">Les stores Pinia sont accessibles</p>
      </div>
      
      <div class="bg-purple-50 border border-purple-200 rounded-lg p-4">
        <h2 class="font-bold text-purple-800">🚀 Vue Router fonctionne</h2>
        <p class="text-purple-700">Les routes sont configurées correctement</p>
      </div>
      
      <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <h2 class="font-bold text-yellow-800">🎨 Tailwind CSS fonctionne</h2>
        <p class="text-yellow-700">Les classes utilitaires sont appliquées</p>
      </div>
    </div>
    
    <div class="mt-6">
      <button
        @click="$router.push('/dashboard')"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Aller au Dashboard
      </button>
    </div>
  </div>
</template>
