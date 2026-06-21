import request from '@/utils/request';
import type { ApiPage, Project, ProjectForm } from '@/types/api';

export type ProjectPageParams = {
  current: number;
  size: number;
};

export const getProjectPageApi = (params: ProjectPageParams) =>
  request.get('/project/page', { params }) as Promise<ApiPage<Project>>;

export const getProjectDetailApi = (id: number) => request.get(`/project/${id}`) as Promise<Project>;

export const createProjectApi = (data: ProjectForm) => request.post('/project', data) as Promise<Project>;

export const updateProjectApi = (id: number, data: ProjectForm) =>
  request.put(`/project/${id}`, data) as Promise<Project>;

export const deleteProjectApi = (id: number) => request.delete(`/project/${id}`) as Promise<void>;