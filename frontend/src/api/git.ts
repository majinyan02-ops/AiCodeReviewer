import request from '@/utils/request';
import type { GitStatus } from '@/types/api';

export const syncRepositoryApi = (projectId: number) =>
  request.post<unknown, string>(`/git/sync/${projectId}`);

export const getRepositoryStatusApi = (projectId: number) =>
  request.get<unknown, GitStatus>(`/git/status/${projectId}`);