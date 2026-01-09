import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, Role, Group, Permission } from '../types/permissions.types'
import { mockUsers, mockRoles, mockGroups, mockPermissions } from '../mock-data/permissions.mock'

export const usePermissionsStore = defineStore('permissions', () => {
  const users = ref<User[]>(mockUsers)
  const roles = ref<Role[]>(mockRoles)
  const groups = ref<Group[]>(mockGroups)
  const permissions = ref<Permission[]>(mockPermissions)
  const isLoading = ref(false)

  const activeUsers = computed(() =>
    users.value.filter(u => u.status === 'active')
  )

  const pendingUsers = computed(() =>
    users.value.filter(u => u.status === 'pending')
  )

  const userCount = computed(() => users.value.length)
  const roleCount = computed(() => roles.value.length)
  const groupCount = computed(() => groups.value.length)

  async function fetchUsers() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      users.value = mockUsers
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRoles() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      roles.value = mockRoles
    } finally {
      isLoading.value = false
    }
  }

  async function fetchGroups() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      groups.value = mockGroups
    } finally {
      isLoading.value = false
    }
  }

  function updateUserStatus(userId: string, status: User['status']) {
    const user = users.value.find(u => u.id === userId)
    if (user) {
      user.status = status
      user.updatedAt = new Date()
    }
  }

  function addUserToGroup(userId: string, groupId: string) {
    const group = groups.value.find(g => g.id === groupId)
    if (group && !group.members.includes(userId)) {
      group.members.push(userId)
    }
  }

  function removeUserFromGroup(userId: string, groupId: string) {
    const group = groups.value.find(g => g.id === groupId)
    if (group) {
      const index = group.members.indexOf(userId)
      if (index > -1) {
        group.members.splice(index, 1)
      }
    }
  }

  return {
    users,
    roles,
    groups,
    permissions,
    isLoading,
    activeUsers,
    pendingUsers,
    userCount,
    roleCount,
    groupCount,
    fetchUsers,
    fetchRoles,
    fetchGroups,
    updateUserStatus,
    addUserToGroup,
    removeUserFromGroup
  }
})
