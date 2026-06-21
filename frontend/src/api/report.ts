import request from '@/utils/request';
import type { ReportResponse } from '@/types/api';

export const generateReportApi = (projectId: number) =>
  request.post(`/report/generate/${projectId}`) as Promise<ReportResponse>;

export const downloadMarkdownApi = (taskId: string) =>
  request.get(`/report/${taskId}/markdown`, { responseType: 'blob' }) as Promise<Blob>;

export const downloadPdfApi = (taskId: string) =>
  request.get(`/report/${taskId}/pdf`, { responseType: 'blob' }) as Promise<Blob>;