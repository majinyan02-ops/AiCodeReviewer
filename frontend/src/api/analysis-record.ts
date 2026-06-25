import request from '@/utils/request';
import type {
  ApiPage,
  AnalysisRecord,
  AnalysisRecordDetail,
  AnalysisRecordQuery,
  TrendData,
  ProjectTrendSummary,
  StatisticsOverview,
} from '@/types/api';

export function getRecordPage(params: AnalysisRecordQuery) {
  return request.get('/analysis-record/page', { params }) as Promise<ApiPage<AnalysisRecord>>;
}

export function getRecordDetail(id: number) {
  return request.get(`/analysis-record/${id}`) as Promise<AnalysisRecordDetail>;
}

export function deleteRecord(id: number) {
  return request.delete(`/analysis-record/${id}`);
}

export function batchDeleteRecords(ids: number[]) {
  return request.delete('/analysis-record/batch', { data: ids });
}

export function getProjectTrend(projectId: number, limit = 30) {
  return request.get(`/analysis-record/trend/${projectId}`, { params: { limit } }) as Promise<TrendData>;
}

export function getProjectTrendOverview() {
  return request.get('/analysis-record/trend/overview') as Promise<ProjectTrendSummary[]>;
}

export function getStatisticsOverview() {
  return request.get('/analysis-record/statistics/overview') as Promise<StatisticsOverview>;
}
