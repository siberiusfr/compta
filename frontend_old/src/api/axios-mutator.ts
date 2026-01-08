import { apiClient } from './client'
import type { AxiosRequestConfig } from 'axios'

export const customAxios = <T>(config: AxiosRequestConfig): Promise<T> => {
  return apiClient(config).then(({ data }) => data)
}

export default customAxios
