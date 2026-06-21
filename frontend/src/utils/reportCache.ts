import type { AiIssueAnalysis, CachedReport, ReviewReport } from '@/types/api';

const REPORT_CACHE_KEY = 'ai-code-reviewer-reports';

const readReports = (): CachedReport[] => {
  const raw = localStorage.getItem(REPORT_CACHE_KEY);

  if (!raw) {
    return [];
  }

  try {
    return JSON.parse(raw) as CachedReport[];
  } catch {
    localStorage.removeItem(REPORT_CACHE_KEY);
    return [];
  }
};

const writeReports = (reports: CachedReport[]) => {
  localStorage.setItem(REPORT_CACHE_KEY, JSON.stringify(reports));
};

export const getCachedReports = () => readReports();

export const saveCachedReport = (projectId: number, taskId: string, report: ReviewReport, aiAnalyses?: AiIssueAnalysis[]) => {
  const cachedReport: CachedReport = {
    id: `${projectId}-${Date.now()}`,
    projectId,
    taskId,
    projectName: report.projectName,
    createdAt: new Date().toISOString(),
    report,
    aiAnalyses: aiAnalyses ?? [],
  };

  const nextReports = [cachedReport, ...readReports()].slice(0, 20);
  writeReports(nextReports);
  return cachedReport;
};

export const removeCachedReport = (id: string) => {
  writeReports(readReports().filter((report) => report.id !== id));
};

export const clearCachedReports = () => writeReports([]);