export * from './gen/generated.schemas'

export {
  links,
  getLinksQueryKey,
  getLinksQueryOptions,
  useLinks,
  health,
  getHealthQueryKey,
  getHealthQueryOptions,
  useHealth,
} from './gen/actuator/actuator'

export {
  getClientById,
  getGetClientByIdQueryKey,
  getGetClientByIdQueryOptions,
  useGetClientById,
  getAllClients,
  getGetAllClientsQueryKey,
  getGetAllClientsQueryOptions,
  useGetAllClients,
  createClient,
  getCreateClientMutationOptions,
  useCreateClient,
  updateClient,
  getUpdateClientMutationOptions,
  useUpdateClient,
  deleteClient,
  getDeleteClientMutationOptions,
  useDeleteClient,
  rotateClientSecret,
  getRotateClientSecretMutationOptions,
  useRotateClientSecret,
} from './gen/client-management/client-management'

export {
  initiateEmailVerification,
  getInitiateEmailVerificationMutationOptions,
  useInitiateEmailVerification,
  confirmEmailVerification,
  getConfirmEmailVerificationMutationOptions,
  useConfirmEmailVerification,
} from './gen/email-verification-controller/email-verification-controller'

export {
  initiatePasswordReset,
  getInitiatePasswordResetMutationOptions,
  useInitiatePasswordReset,
  confirmPasswordReset,
  getConfirmPasswordResetMutationOptions,
  useConfirmPasswordReset,
} from './gen/password-reset-controller/password-reset-controller'

export {
  getRoleById,
  getGetRoleByIdQueryKey,
  getGetRoleByIdQueryOptions,
  useGetRoleById,
  getAllRoles,
  getGetAllRolesQueryKey,
  getGetAllRolesQueryOptions,
  useGetAllRoles,
  createRole,
  getCreateRoleMutationOptions,
  useCreateRole,
  updateRole,
  getUpdateRoleMutationOptions,
  useUpdateRole,
  deleteRole,
  getDeleteRoleMutationOptions,
  useDeleteRole,
  getRoleByName,
  getGetRoleByNameQueryKey,
  getGetRoleByNameQueryOptions,
  useGetRoleByName,
} from './gen/role-management/role-management'

export {
  introspect,
  getIntrospectMutationOptions,
  useIntrospect,
} from './gen/token-introspection/token-introspection'

export {
  revoke,
  getRevokeMutationOptions,
  useRevoke,
} from './gen/token-revocation/token-revocation'

export {
  getUserInfo,
  getGetUserInfoQueryKey,
  getGetUserInfoQueryOptions,
  useGetUserInfo,
} from './gen/user-info/user-info'

export {
  getUserById,
  getGetUserByIdQueryKey,
  getGetUserByIdQueryOptions,
  useGetUserById,
  updateUser,
  getUpdateUserMutationOptions,
  useUpdateUser,
  deleteUser,
  getDeleteUserMutationOptions,
  useDeleteUser,
  getAllUsers,
  getGetAllUsersQueryKey,
  getGetAllUsersQueryOptions,
  useGetAllUsers,
  createUser,
  getCreateUserMutationOptions,
  useCreateUser,
  getUserRoles,
  getGetUserRolesQueryKey,
  getGetUserRolesQueryOptions,
  useGetUserRoles,
  assignRoles,
  getAssignRolesMutationOptions,
  useAssignRoles,
  changePassword,
  getChangePasswordMutationOptions,
  useChangePassword,
  enableUser,
  getEnableUserMutationOptions,
  useEnableUser,
  disableUser,
  getDisableUserMutationOptions,
  useDisableUser,
  removeRole,
  getRemoveRoleMutationOptions,
  useRemoveRole,
} from './gen/user-management/user-management'
