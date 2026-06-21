import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { router } from '@/router';
import { useUserStore } from '@/store/user';

type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
};

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
});

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore();

  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`;
  }

  return config;
});

request.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>;

    if (typeof result?.code === 'number') {
      if (result.code === 200) {
        return result.data;
      }

      ElMessage.error(result.message || '请求失败');
      return Promise.reject(result);
    }

    return response.data;
  },
  (error: AxiosError<{ message?: string }>) => {
    const userStore = useUserStore();
    const status = error.response?.status;
    const message = error.response?.data?.message ?? error.message ?? '请求失败';

    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录');
      userStore.clearSession();
      router.replace('/login');
      return Promise.reject(error);
    }

    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default request;