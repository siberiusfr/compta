<template>
  <n-layout style="height: 100vh">
    <n-flex justify="center" align="center" style="height: 100%">
      <n-card style="max-width: 450px;">
        <n-h1>Se connecter</n-h1>
        <n-form ref="formRef" :model="formValue" :rules="rules" size="large" @submit.prevent="handleLogin">
          <n-form-item path="username" label="Nom d'utilisateur">
            <n-input v-model:value="formValue.username" placeholder="Entrez votre nom d'utilisateur" />
          </n-form-item>
          <n-form-item path="password" label="Mot de passe">
            <n-input
              v-model:value="formValue.password"
              type="password"
              show-password-on="mousedown"
              placeholder="Entrez votre mot de passe"
              @keyup.enter="handleLogin"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" attr-type="submit" block :loading="loading">
              Se connecter
            </n-button>
          </n-form-item>
          <n-form-item>
            <n-text depth="3">
              Pas de compte ?
              <n-button text type="primary" @click="router.push({ name: 'register' })">
                S'inscrire
              </n-button>
            </n-text>
          </n-form-item>
        </n-form>
      </n-card>
    </n-flex>
  </n-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, type FormInst, type FormRules } from 'naive-ui'
import { useAuthStore } from '@/modules/auth/stores/authStore'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formValue = ref({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: "Nom d'utilisateur requis", trigger: 'blur' }],
  password: [{ required: true, message: 'Mot de passe requis', trigger: 'blur' }],
}

async function handleLogin() {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      const { success } = await authStore.login({
        username: formValue.value.username,
        password: formValue.value.password,
      })
      loading.value = false

      if (success) {
        message.success('Connexion réussie')
        // Rediriger vers la page d'accueil ou le tableau de bord après la connexion
        router.push({ name: 'accounting' })
      } else {
        message.error("Nom d'utilisateur ou mot de passe incorrect")
      }
    }
  })
}
</script>
