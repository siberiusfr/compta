<template>
  <n-form ref="formRef" :model="formValue" :rules="rules" size="large">
    <n-form-item path="email" label="Email">
      <n-input v-model:value="formValue.email" placeholder="Entrez votre email" />
    </n-form-item>
    <n-form-item path="password" label="Mot de passe">
      <n-input
        v-model:value="formValue.password"
        type="password"
        placeholder="Entrez votre mot de passe"
        @keyup.enter="handleLogin"
      />
    </n-form-item>
    <n-form-item>
      <n-button type="primary" block :loading="loading" @click="handleLogin">
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
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { useAuthStore } from '@stores/index'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formValue = ref({
  email: '',
  password: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: 'Email requis', trigger: 'blur' },
    { type: 'email', message: 'Email invalide', trigger: 'blur' },
  ],
  password: [{ required: true, message: 'Mot de passe requis', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const result = await authStore.login(formValue.value.email, formValue.value.password)

    if (result.success) {
      message.success('Connexion réussie')
      router.push({ name: 'accounting' })
    } else {
      message.error('Email ou mot de passe incorrect')
    }
  } catch (error) {
    console.error('Validation failed:', error)
  } finally {
    loading.value = false
  }
}
</script>
