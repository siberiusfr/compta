import Axios from 'axios';
import type { AxiosRequestConfig } from 'axios';

export const AXIOS_INSTANCE = Axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

export const customInstance = <T>(config: AxiosRequestConfig): Promise<T> => {
  const promise = AXIOS_INSTANCE.request<T>(config).then(({ data }) => data);
  return promise;
};