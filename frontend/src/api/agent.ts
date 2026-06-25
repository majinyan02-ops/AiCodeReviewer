import request from '@/utils/request';
import type { AgentResult, FixAgentResult, ReviewAgentResult, SummaryAgentResult } from '@/types/api';

export const executeReviewAgentApi = (projectId: string) =>
  request.post('/agent/review', { projectId }) as Promise<AgentResult<ReviewAgentResult>>;

export const executeFixAgentApi = (projectId: string) =>
  request.post('/agent/fix', { projectId }) as Promise<AgentResult<FixAgentResult>>;

export const executeSummaryAgentApi = (projectId: string) =>
  request.post('/agent/summary', { projectId }) as Promise<AgentResult<SummaryAgentResult>>;

export function downloadMarkdownReport(projectId: string, projectName?: string) {
  const params = projectName ? { projectName } : {};
  return request.get(`/agent/report/markdown/${projectId}`, {
    params,
    responseType: 'blob',
  }) as Promise<Blob>;
}

export function downloadPdfReport(projectId: string, projectName?: string) {
  const params = projectName ? { projectName } : {};
  return request.get(`/agent/report/pdf/${projectId}`, {
    params,
    responseType: 'blob',
  }) as Promise<Blob>;
}
