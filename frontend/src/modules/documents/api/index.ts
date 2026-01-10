/**
 * Re-exports from generated API files (tags-split mode)
 * This barrel file allows importing from '@/modules/documents/api'
 */

// Types from schemas
export * from './generated.schemas'

// Documents hooks
export {
  getById,
  getGetByIdQueryKey,
  useGetById,
  update,
  useUpdate,
  _delete,
  useDelete,
  getMetadata,
  useGetMetadata,
  setMetadata,
  useSetMetadata,
  getAll1,
  getGetAll1QueryKey,
  useGetAll1,
  upload,
  useUpload,
  searchGet,
  getSearchGetQueryKey,
  useSearchGet,
  search,
  useSearch,
  download,
  useDownload,
  getDownloadUrl,
  useGetDownloadUrl,
  getPublic,
  useGetPublic,
  getMyDocuments,
  getGetMyDocumentsQueryKey,
  useGetMyDocuments,
  getByCategory,
  useGetByCategory,
  deleteMetadataKey,
  useDeleteMetadataKey,
} from './documents/documents'

// Categories hooks
export {
  getById1,
  useGetById1,
  update2,
  useUpdate2,
  delete1,
  useDelete1,
  getAll2,
  getGetAll2QueryKey,
  useGetAll2,
  create1,
  useCreate1,
  getTree,
  useGetTree,
  getRootCategories,
  useGetRootCategories,
} from './categories/categories'

// Document sharing hooks
export {
  update1,
  useUpdate1,
  revoke,
  useRevoke,
  getSharesByDocument,
  useGetSharesByDocument,
  share,
  useShare,
  cleanupExpiredShares,
  useCleanupExpiredShares,
  getSharesWithUser,
  useGetSharesWithUser,
  getSharedWithMe,
  useGetSharedWithMe,
} from './document-sharing/document-sharing'

// Document versions hooks
export {
  getVersions,
  useGetVersions,
  uploadVersion,
  useUploadVersion,
  getVersion,
  useGetVersion,
  downloadVersion,
  useDownloadVersion,
  getVersionDownloadUrl,
  useGetVersionDownloadUrl,
} from './document-versions/document-versions'

// Tags hooks
export {
  getAll,
  useGetAll,
  create,
  useCreate,
  getById2,
  useGetById2,
  delete2,
  useDelete2,
} from './tags/tags'
