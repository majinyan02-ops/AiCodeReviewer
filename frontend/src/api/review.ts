import request from '@/utils/request';
import type { ReportTaskProgress, ReportTaskStartResult, ScanContext } from '@/types/api';

export const scanProjectApi = (projectId: number) => request.get<unknown, ScanContext>(`/scan/${projectId}`);

export const generateReportAsyncApi = async (projectId: number) => {
  const result = await request.post<unknown, ReportTaskStartResult>(`/report/generate/${projectId}/async`);
  return typeof result === 'string' ? result : result.taskId;
};

export const getReportProgressApi = (taskId: string) =>
  request.get<unknown, ReportTaskProgress>(`/report/progress/${taskId}`);

export const getReportProgressByProjectApi = (projectId: number) =>
  request.get<unknown, ReportTaskProgress>(`/report/progress/project/${projectId}`);