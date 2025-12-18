<template>
  <n-form ref="formRef" :model="formValue" :rules="rules" size="large">
    <n-form-item path="name" label="Nom complet">
      <n-input v-model:value="formValue.name" placeholder="Entrez votre nom" />
    </n-form-item>
    <n-form-item path="email" label="Email">
      <n-input v-model:value="formValue.email" placeholder="Entrez votre email" />
    </n-form-item>
    <n-form-item path="password" label="Mot de passe">
      <n-input v-model:value="formValue.password" type="password" placeholder="Mot de passe" />
    </n-form-item>
    <n-form-item path="confirmPassword" label="Confirmer le mot de passe">
      <n-input
        v-model:value="formValue.confirmPassword"
        type="password"
        placeholder="Confirmez le mot de passe"
        @keyup.enter="handleRegister"
      />
    </n-form-item>
    <n-form-item>
      <n-button type="primary" block :loading="loading" @click="handleRegister">
        S'inscrire
      </n-button>
    </n-form-item>
    <n-form-item>
      <n-text depth="3">
        Déjà un compte ?
        <n-button text type="primary" @click="router.push({ name: 'login' })">
          Se connecter
        </n-button>
      </n-text>
    </n-form-item>
  </n-form>
</template>

<script setup lang="ts">
import type { FormInst, FormRules, FormItemRule } from 'naive-ui'
import { useAuthStore } from '@stores/index'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formValue = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const validatePasswordSame = (rule: FormItemRule, value: string): boolean => {
  return value === formValue.value.password
}

const rules: FormRules = {
  name: [{ required: true, message: 'Nom requis', trigger: 'blur' }],
  email: [
    { required: true, message: 'Email requis', trigger: 'blur' },
    { type: 'email', message: 'Email invalide', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Mot de passe requis', trigger: 'blur' },
    { min: 6, message: 'Le mot de passe doit contenir au moins 6 caractères', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'Confirmation requise', trigger: 'blur' },
    {
      validator: validatePasswordSame,
      message: 'Les mots de passe ne correspondent pas',
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const result = await authStore.register(
      formValue.value.email,
      formValue.value.password,
      formValue.value.name
    )

    if (result.success) {
      message.success('Inscription réussie')
      router.push({ name: 'login' })
    } else {
      message.error("Erreur lors de l'inscription")
    }
  } catch (error) {
    console.error('Validation failed:', error)
  } finally {
    loading.value = false
  }
}
</script>
