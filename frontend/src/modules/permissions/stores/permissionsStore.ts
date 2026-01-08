import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockRoles, mockUsers, mockAuditLogs } from '../mock-data/permissions.mock'
import type { Role, User, AuditLog } from '../types/permissions.types'

export const usePermissionsStore = defineStore('permissions', () => {
  const roles = ref<Role[]>(mockRoles)
  const users = ref<User[]>(mockUsers)
  const auditLogs = ref<AuditLog[]>(mockAuditLogs)

  const activeUsers = computed(() => users.value.filter(u => u.active))

  function createRole(role: Omit<Role, 'id' | 'userCount' | 'createdAt'>) {
    const newRole: Role = {
      ...role,
      id: Date.now().toString(),
      userCount: 0,
      createdAt: new Date()
    }
    roles.value.push(newRole)
    return newRole
  }

  function createUser(user: Omit<User, 'id' | 'createdAt'>) {
    const newUser: User = {
      ...user,
      id: Date.now().toString(),
      createdAt: new Date()
    }
    users.value.push(newUser)
    return newUser
  }

  function updateUser(id: string, updates: Partial<User>) {
    const index = users.value.findIndex(u => u.id === id)
    if (index !== -1) {
      users.value[index] = { ...users.value[index], ...updates } as User
    }
  }

  function deleteUser(id: string) {
    const index = users.value.findIndex(u => u.id === id)
    if (index !== -1) {
      users.value.splice(index, 1)
    }
  }

  return {
    roles,
    users,
    auditLogs,
    activeUsers,
    createRole,
    createUser,
    updateUser,
    deleteUser
  }
})
