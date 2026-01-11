import { computed, ref } from "vue";
import { useQueryClient } from "@tanstack/vue-query";
import {
  useGetAllUsers,
  useCreateUser,
  useUpdateUser,
  useDeleteUser,
  useEnableUser,
  useDisableUser,
  getGetAllUsersQueryKey,
  type UserResponse,
  type CreateUserRequest,
  type UpdateUserRequest,
} from "@/api/oauth";
import { useToast } from "@/shared/composables";
import type { User, UserStatus } from "../types/permissions.types";

/**
 * Transforme une reponse API UserResponse en type User local
 */
function mapUserResponse(apiUser: UserResponse): User {
  return {
    id: apiUser.id ?? "",
    email: apiUser.email ?? "",
    firstName: apiUser.firstName ?? "",
    lastName: apiUser.lastName ?? "",
    fullName:
      `${apiUser.firstName ?? ""} ${apiUser.lastName ?? ""}`.trim() ||
      (apiUser.username ?? ""),
    status: mapUserStatus(apiUser.enabled, apiUser.accountNonLocked),
    roles: apiUser.roles ?? [],
    groups: [],
    lastLogin: undefined,
    createdAt: apiUser.createdAt ? new Date(apiUser.createdAt) : new Date(),
    updatedAt: apiUser.updatedAt ? new Date(apiUser.updatedAt) : new Date(),
  };
}

/**
 * Determine le statut utilisateur selon les flags API
 */
function mapUserStatus(
  enabled?: boolean,
  accountNonLocked?: boolean,
): UserStatus {
  if (!enabled) return "inactive";
  if (!accountNonLocked) return "suspended";
  return "active";
}

export function useUsers() {
  const queryClient = useQueryClient();
  const toast = useToast();

  // Etat local pour les filtres
  const searchQuery = ref("");
  const statusFilter = ref<UserStatus | "">("");

  // ========================================
  // QUERIES - Lecture des donnees
  // ========================================

  // Recuperation de tous les utilisateurs
  const {
    data: usersData,
    isLoading,
    isError,
    error,
    refetch,
  } = useGetAllUsers({
    query: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      refetchOnWindowFocus: false,
    },
  });

  // Transformation des donnees API en format local
  const users = computed<User[]>(() => {
    if (!usersData.value) return [];

    console.log("[useUsers] Raw API response:", usersData.value);

    // Gere les deux cas: tableau direct ou objet pagine (Spring Data)
    let apiUsers: UserResponse[];
    const data = usersData.value as unknown;

    if (Array.isArray(data)) {
      apiUsers = data as UserResponse[];
      console.log("[useUsers] Parsed as array:", apiUsers.length, "users");
    } else if (typeof data === "object" && data !== null) {
      // Spring Data Page: { content: [...], totalElements, ... }
      const pageData = data as { content?: UserResponse[] };
      apiUsers = pageData.content ?? [];
      console.log("[useUsers] Parsed as page:", apiUsers.length, "users");
    } else {
      console.warn("[useUsers] Unexpected API response format:", data);
      apiUsers = [];
    }

    const mappedUsers = apiUsers.map(mapUserResponse);
    console.log("[useUsers] Mapped users:", mappedUsers.length);

    return mappedUsers;
  });

  // Utilisateurs filtres
  const filteredUsers = computed(() => {
    let result = users.value;

    // Filtre par recherche
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase();
      result = result.filter(
        (u) =>
          u.fullName.toLowerCase().includes(query) ||
          u.email.toLowerCase().includes(query),
      );
    }

    // Filtre par statut
    if (statusFilter.value) {
      result = result.filter((u) => u.status === statusFilter.value);
    }

    return result;
  });

  // Statistiques calculees
  const userCount = computed(() => users.value.length);
  const activeUsers = computed(() =>
    users.value.filter((u) => u.status === "active"),
  );
  const pendingUsers = computed(() =>
    users.value.filter((u) => u.status === "pending"),
  );
  const inactiveUsers = computed(() =>
    users.value.filter((u) => u.status === "inactive"),
  );

  // ========================================
  // MUTATIONS - Modification des donnees
  // ========================================

  // Creation d'utilisateur
  const createUserMutation = useCreateUser({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllUsersQueryKey() });
        toast.success(
          "Utilisateur cree",
          "L'utilisateur a ete cree avec succes",
        );
      },
      onError: (error: unknown) => {
        toast.error("Erreur", `Impossible de creer l'utilisateur: ${error}`);
      },
    },
  });

  // Mise a jour d'utilisateur
  const updateUserMutation = useUpdateUser({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllUsersQueryKey() });
        toast.success(
          "Utilisateur modifie",
          "Les modifications ont ete enregistrees",
        );
      },
      onError: (error: unknown) => {
        toast.error("Erreur", `Impossible de modifier l'utilisateur: ${error}`);
      },
    },
  });

  // Suppression d'utilisateur
  const deleteUserMutation = useDeleteUser({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllUsersQueryKey() });
        toast.success("Utilisateur supprime", "L'utilisateur a ete supprime");
      },
      onError: (error: unknown) => {
        toast.error(
          "Erreur",
          `Impossible de supprimer l'utilisateur: ${error}`,
        );
      },
    },
  });

  // Activation d'utilisateur
  const enableUserMutation = useEnableUser({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllUsersQueryKey() });
        toast.success("Utilisateur active", "Le compte a ete active");
      },
      onError: (error: unknown) => {
        toast.error("Erreur", `Impossible d'activer l'utilisateur: ${error}`);
      },
    },
  });

  // Desactivation d'utilisateur
  const disableUserMutation = useDisableUser({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllUsersQueryKey() });
        toast.success("Utilisateur desactive", "Le compte a ete desactive");
      },
      onError: (error: unknown) => {
        toast.error(
          "Erreur",
          `Impossible de desactiver l'utilisateur: ${error}`,
        );
      },
    },
  });

  // ========================================
  // ACTIONS - Fonctions exposees
  // ========================================

  const createUser = async (data: CreateUserRequest) => {
    return createUserMutation.mutateAsync({ data });
  };

  const updateUser = async (id: string, data: UpdateUserRequest) => {
    return updateUserMutation.mutateAsync({ id, data });
  };

  const deleteUser = async (id: string) => {
    return deleteUserMutation.mutateAsync({ id });
  };

  const enableUser = async (id: string) => {
    return enableUserMutation.mutateAsync({ id });
  };

  const disableUser = async (id: string) => {
    return disableUserMutation.mutateAsync({ id });
  };

  const updateUserStatus = async (userId: string, status: UserStatus) => {
    if (status === "active") {
      return enableUser(userId);
    } else if (status === "inactive") {
      return disableUser(userId);
    }
  };

  const setSearchQuery = (query: string) => {
    searchQuery.value = query;
  };

  const setStatusFilter = (status: UserStatus | "") => {
    statusFilter.value = status;
  };

  return {
    // Donnees
    users,
    filteredUsers,

    // Etat de chargement
    isLoading,
    isError,
    error,

    // Statistiques
    userCount,
    activeUsers,
    pendingUsers,
    inactiveUsers,

    // Filtres
    searchQuery,
    statusFilter,
    setSearchQuery,
    setStatusFilter,

    // Actions
    createUser,
    updateUser,
    deleteUser,
    enableUser,
    disableUser,
    updateUserStatus,
    refetch,

    // Etats des mutations
    isCreating: createUserMutation.isPending,
    isUpdating: updateUserMutation.isPending,
    isDeleting: deleteUserMutation.isPending,
    isEnabling: enableUserMutation.isPending,
    isDisabling: disableUserMutation.isPending,
  };
}

// Re-export des types utiles
export type { UserResponse, CreateUserRequest, UpdateUserRequest };
