<template>
  <n-layout style="height: 100vh">
    <n-flex justify="center" align="center" style="height: 100%">
      <n-card class="register-card" style="max-width: 450px;">
        <n-h1>Créer un compte</n-h1>
        <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleRegister">
          <n-form-item path="username" label="Nom d'utilisateur">
            <n-input v-model:value="model.username" placeholder="Entrez votre nom d'utilisateur" />
          </n-form-item>
          <n-form-item path="firstName" label="Prénom">
            <n-input v-model:value="model.firstName" placeholder="Entrez votre prénom" />
          </n-form-item>
          <n-form-item path="lastName" label="Nom de famille">
            <n-input v-model:value="model.lastName" placeholder="Entrez votre nom de famille" />
          </n-form-item>
          <n-form-item path="email" label="Email">
            <n-input v-model:value="model.email" placeholder="Entrez votre adresse email" />
          </n-form-item>
          <n-form-item path="password" label="Mot de passe">
            <n-input
              v-model:value="model.password"
              type="password"
              show-password-on="mousedown"
              placeholder="Entrez votre mot de passe"
            />
          </n-form-item>
          <n-form-item path="reenteredPassword" label="Confirmer le mot de passe">
            <n-input
              v-model:value="model.reenteredPassword"
              type="password"
              show-password-on="mousedown"
              placeholder="Confirmez votre mot de passe"
              :disabled="!model.password"
            />
          </n-form-item>
          <n-button type="primary" attr-type="submit" block :loading="loading">
            S'inscrire
          </n-button>
        </n-form>
      </n-card>
    </n-flex>
  </n-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  useMessage,
  type FormInst,
  type FormRules,
  NLayout,
  NFlex,
  NCard,
  NH1,
  NForm,
  NFormItem,
  NInput,
  NButton,
} from 'naive-ui'
import { useAuthStore } from '@/modules/auth/stores/authStore'

const formRef = ref<FormInst | null>(null)
const model = ref({
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  reenteredPassword: '',
})
const loading = ref(false)

const authStore = useAuthStore()
const router = useRouter()
const message = useMessage()

// Fonction de validation pour la confirmation du mot de passe
function validatePasswordSame(rule: any, value: string): boolean {
  return value === model.value.password
}

const rules: FormRules = {
  username: [
    { required: true, message: "Le nom d'utilisateur est requis", trigger: 'blur' },
    { min: 3, message: "Le nom d'utilisateur doit contenir au moins 3 caractères", trigger: 'blur' },
  ],
  firstName: [{ required: true, message: 'Le prénom est requis', trigger: 'blur' }],
  lastName: [{ required: true, message: 'Le nom de famille est requis', trigger: 'blur' }],
  email: [{ required: true, message: "L'email est requis", trigger: 'blur' }],
  password: [
    { required: true, message: 'Le mot de passe est requis', trigger: 'blur' },
    { min: 8, message: 'Le mot de passe doit contenir au moins 8 caractères', trigger: 'blur' },
  ],
  reenteredPassword: [
    { required: true, message: 'Veuillez confirmer votre mot de passe', trigger: 'blur' },
    { validator: validatePasswordSame, message: 'Les mots de passe ne correspondent pas', trigger: 'blur' }
  ],
}

const handleRegister = async () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      const { reenteredPassword, ...registrationData } = model.value
      const { success } = await authStore.register(registrationData)
      loading.value = false

      if (success) {
        message.success('Inscription réussie ! Vous pouvez maintenant vous connecter.')
        router.push({ name: 'login' })
      } else {
        message.error("Une erreur est survenue lors de l'inscription.")
      }
    }
  })
}
</script>