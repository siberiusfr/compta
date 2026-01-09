import { computed } from "vue";
import { useQuery } from "@tanstack/vue-query";
import { customInstance } from "@/api/axios-instance";
import type { Role, Permission } from "../types/permissions.types";

interface RoleResponse {
  id?: string;
  name?: string;
  description?: string;
  permissions?: Permission[];
  userCount?: number;
  isSystem?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

function mapRoleResponse(apiRole: RoleResponse): Role {
  return {
    id: apiRole.id ?? "",
    name: apiRole.name ?? "",
    description: apiRole.description ?? "",
    permissions: apiRole.permissions ?? [],
    userCount: apiRole.userCount ?? 0,
    isSystem: apiRole.isSystem ?? false,
    createdAt: apiRole.createdAt ? new Date(apiRole.createdAt) : new Date(),
    updatedAt: apiRole.updatedAt ? new Date(apiRole.updatedAt) : new Date(),
  };
}

async function getAllRoles(signal?: AbortSignal): Promise<RoleResponse[]> {
  return customInstance<RoleResponse[]>({
    url: "/api/roles",
    method: "GET",
    signal,
  });
}

export function useRoles() {
  const {
    data: rolesData,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ["api", "roles"],
    queryFn: ({ signal }) => getAllRoles(signal),
    staleTime: 1000 * 60 * 5,
  });

  const roles = computed<Role[]>(() => {
    if (!rolesData.value) return [];

    const data = rolesData.value as unknown;

    if (Array.isArray(data)) {
      const apiRoles = data as RoleResponse[];
      return apiRoles.map(mapRoleResponse);
    }

    if (typeof data === "object" && data !== null) {
      const pageData = data as { content?: RoleResponse[] };
      const apiRoles = pageData.content ?? [];
      return apiRoles.map(mapRoleResponse);
    }

    console.warn("[useRoles] Unexpected API response format:", data);
    return [];
  });

  const roleCount = computed(() => roles.value.length);
  const systemRoles = computed(() => roles.value.filter((r) => r.isSystem));
  const customRoles = computed(() => roles.value.filter((r) => !r.isSystem));

  return {
    roles,
    roleCount,
    systemRoles,
    customRoles,
    isLoading,
    isError,
    error,
    refetch,
  };
}
