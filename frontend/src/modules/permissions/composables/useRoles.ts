import { computed } from "vue";
import { useQueryClient } from "@tanstack/vue-query";
import {
  useGetAllRoles,
  useUpdateRole,
  useDeleteRole,
  getGetAllRolesQueryKey,
  type RoleResponse,
  type RoleWithUserCountDto,
  type UpdateRoleRequest,
} from "@/modules/oauth/api";
import { useToast } from "@/shared/composables";
import type { Role } from "../types/permissions.types";

function mapRoleResponse(apiRole: RoleWithUserCountDto): Role {
  return {
    id: apiRole.id ?? "",
    name: apiRole.name ?? "",
    description: apiRole.description ?? "",
    permissions: [],
    userCount: apiRole.userCount ?? 0,
    isSystem: false,
    createdAt: apiRole.createdAt ? new Date(apiRole.createdAt) : new Date(),
    updatedAt: new Date(),
  };
}

export function useRoles() {
  const queryClient = useQueryClient();
  const toast = useToast();

  const {
    data: rolesData,
    isLoading,
    isError,
    error,
    refetch,
  } = useGetAllRoles({
    query: {
      staleTime: 1000 * 60 * 5,
      refetchOnWindowFocus: false,
    },
  });

  const roles = computed<Role[]>(() => {
    if (!rolesData.value) return [];

    const data = rolesData.value as unknown;

    if (Array.isArray(data)) {
      const apiRoles = data as RoleResponse[];
      return apiRoles.map(mapRoleResponse);
    } else if (typeof data === "object" && data !== null) {
      const pageData = data as { content?: RoleResponse[] };
      const apiRoles = pageData.content ?? [];
      return apiRoles.map(mapRoleResponse);
    } else {
      console.warn("[useRoles] Unexpected API response format:", data);
      return [];
    }
  });

  const roleCount = computed(() => roles.value.length);
  const systemRoles = computed(() => roles.value.filter((r) => r.isSystem));
  const customRoles = computed(() => roles.value.filter((r) => !r.isSystem));

  const updateRoleMutation = useUpdateRole({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllRolesQueryKey() });
        toast.success("Role modifie", "Les modifications ont ete enregistrees");
      },
      onError: (error: unknown) => {
        toast.error("Erreur", `Impossible de modifier le role: ${error}`);
      },
    },
  });

  const deleteRoleMutation = useDeleteRole({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAllRolesQueryKey() });
        toast.success("Role supprime", "Le role a ete supprime");
      },
      onError: (error: unknown) => {
        toast.error("Erreur", `Impossible de supprimer le role: ${error}`);
      },
    },
  });

  const updateRole = async (id: string, data: UpdateRoleRequest) => {
    return updateRoleMutation.mutateAsync({ id, data });
  };

  const deleteRole = async (id: string) => {
    return deleteRoleMutation.mutateAsync({ id });
  };

  return {
    roles,
    roleCount,
    systemRoles,
    customRoles,
    isLoading,
    isError,
    error,
    updateRole,
    deleteRole,
    refetch,
    isUpdating: updateRoleMutation.isPending,
    isDeleting: deleteRoleMutation.isPending,
  };
}
