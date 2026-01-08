<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <div class="text-center space-y-4">
      <h1 class="text-6xl font-bold text-gray-900">404</h1>
      <p class="text-xl text-gray-600">Page non trouvée</p>
      <div class="flex gap-4 justify-center">
        <RouterLink
          to="/login"
          class="inline-block px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Connexion
        </RouterLink>
        <RouterLink
          to="/test"
          class="inline-block px-6 py-3 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
        >
          Page de test
        </RouterLink>
      </div>
    </div>
  </div>
</template>
