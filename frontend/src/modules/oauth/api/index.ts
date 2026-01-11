export * from './generated.schemas'

export {
  links,
  getLinksQueryKey,
  getLinksQueryOptions,
  useLinks,
  health,
  getHealthQueryKey,
  getHealthQueryOptions,
  useHealth,
} from './actuator/actuator'

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
} from './client-management/client-management'

export {
  initiateEmailVerification,
  getInitiateEmailVerificationMutationOptions,
  useInitiateEmailVerification,
  confirmEmailVerification,
  getConfirmEmailVerificationMutationOptions,
  useConfirmEmailVerification,
} from './email-verification-controller/email-verification-controller'

export {
  initiatePasswordReset,
  getInitiatePasswordResetMutationOptions,
  useInitiatePasswordReset,
  confirmPasswordReset,
  getConfirmPasswordResetMutationOptions,
  useConfirmPasswordReset,
} from './password-reset-controller/password-reset-controller'

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
} from './role-management/role-management'

export {
  introspect,
  getIntrospectMutationOptions,
  useIntrospect,
} from './token-introspection/token-introspection'

export {
  revoke,
  getRevokeMutationOptions,
  useRevoke,
} from './token-revocation/token-revocation'

export {
  getUserInfo,
  getGetUserInfoQueryKey,
  getGetUserInfoQueryOptions,
  useGetUserInfo,
} from './user-info/user-info'

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
} from './user-management/user-management'