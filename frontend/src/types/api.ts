export type ApiPage<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
};

export type Severity = 'ERROR' | 'WARNING' | 'INFO';

export type ProjectStatus = 0 | 1;

export type Project = {
  id: number;
  name: string;
  description?: string;
  gitUrl?: string;
  branchName?: string;
  creatorId?: number;
  status?: ProjectStatus;
  createTime?: string;
};

export type ProjectForm = {
  name: string;
  description?: string;
  gitUrl?: string;
  branchName?: string;
};

export type GitStatus = {
  projectId: number;
  projectName: string;
  gitUrl?: string;
  localPath?: string;
  branch?: string;
  latestCommit?: string;
  commitMessage?: string;
  exists: boolean;
};

export type ScanContext = {
  projectId: number;
  rootPath: string;
  controllers: ScannedClass[];
  services: ScannedClass[];
  mappers: ScannedClass[];
  entities: ScannedClass[];
  others: ScannedClass[];
  totalFiles: number;
  elapsedMs: number;
};

export type ScannedClass = {
  packageName?: string;
  className?: string;
  filePath?: string;
  methods?: unknown[];
};

export type RuleResult = {
  ruleId: string;
  ruleName: string;
  description: string;
  severity: Severity;
  passed: boolean;
  className: string;
  methodName: string;
  filePath: string;
  lineNumber: number;
  message: string;
  suggestion: string;
};

export type AiIssueAnalysis = {
  ruleId: string;
  ruleName: string;
  className: string;
  methodName: string;
  riskLevel: string;
  reason: string;
  impact: string;
  suggestion: string;
  exampleFix: string;
};

export type IssueSummary = {
  ruleId: string;
  ruleName: string;
  severity: Severity;
  className: string;
  methodName: string;
  filePath: string;
  lineNumber: number;
  reason: string;
  impact: string;
  suggestion: string;
};

export type ReviewReport = {
  projectName: string;
  scanTime: string;
  totalRules: number;
  passedRules: number;
  failedRules: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  issues: IssueSummary[];
  overallSummary?: string;
};

export type ReportResponse = {
  report: ReviewReport;
  aiAnalyses: AiIssueAnalysis[];
};

export type ReportTaskStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'NOT_FOUND';

export type ReportTaskProgress = {
  taskId: string;
  status: ReportTaskStatus;
  stage: string;
  percent: number;
  report: ReviewReport | null;
};

export type ReportTaskStartResult = string | { taskId: string };

export type CachedReport = {
  id: string;
  projectId: number;
  taskId: string;
  projectName: string;
  createdAt: string;
  report: ReviewReport;
  aiAnalyses: AiIssueAnalysis[];
};

// ========== Agent 类型 ==========

export type AgentResult<T = unknown> = {
  agentType: 'REVIEW' | 'FIX' | 'SUMMARY';
  success: boolean;
  message: string;
  startTime: string;
  endTime: string;
  duration: number;
  payload: T;
};

export type ReviewAgentResult = {
  totalRules: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  overallScore: number;
  riskLevel: string;
  summary: string;
  ruleResults: RuleResult[];
  aiAnalysisDuration: number;
  generatedTime: string;
};

export type FixItem = {
  ruleId: string;
  className: string;
  methodName: string;
  severity: Severity;
  issue: string;
  suggestion: string;
  patchContent: string;
  patchGenerated: boolean;
  generateDuration: number;
};

export type FixStatistics = {
  totalIssues: number;
  fixedIssues: number;
  failedIssues: number;
  successRate: number;
  totalDuration: number;
  totalAiDuration: number;
};

export type FixAgentResult = {
  totalIssues: number;
  fixedIssues: number;
  failedIssues: number;
  fixItems: FixItem[];
  statistics: FixStatistics;
  generatedTime: string;
};

export type SummaryStatistics = {
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  failedIssues: number;
  fixSuccessRate: number;
  reviewAiDuration: number;
  fixAiDuration: number;
  totalAiDuration: number;
};

export type ProjectHealthReport = {
  healthLevel: string;
  healthScore: number;
  overallStatus: string;
  summary: string;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  topProblems: string[];
};

export type SummaryAgentResult = {
  statistics: SummaryStatistics;
  healthReport: ProjectHealthReport;
  reviewResult: ReviewAgentResult | null;
  fixResult: FixAgentResult | null;
  generatedTime: string;
};

// ========== Phase-9 企业级功能 ==========

export type AnalysisRecord = {
  id: number;
  projectId: number;
  projectName?: string;
  taskId?: number;
  overallScore?: number;
  riskLevel?: string;
  healthLevel?: string;
  healthScore?: number;
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  fixSuccessRate: number;
  aiDuration: number;
  status: string;
  markdownPath?: string;
  pdfPath?: string;
  createTime: string;
};

export type AnalysisRecordDetail = AnalysisRecord & {
  errorMessage?: string;
  summaryStatistics?: SummaryStatistics;
  healthReport?: ProjectHealthReport;
};

export type TrendData = {
  projectId: number;
  projectName: string;
  points: TrendPoint[];
};

export type TrendPoint = {
  date: string;
  overallScore?: number;
  healthScore?: number;
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  fixSuccessRate: number;
};

export type AnalysisRecordQuery = {
  projectId?: number;
  keyword?: string;
  riskLevel?: string;
  healthLevel?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
};

export type ProjectTrendSummary = {
  projectId: number;
  projectName: string;
  latestScore?: number;
  latestHealthScore?: number;
  scoreChange?: number;
  totalRecords: number;
};

export type StatisticsOverview = {
  totalProjects: number;
  totalRecords: number;
  totalIssues: number;
  totalFixed: number;
  avgHealthScore: number;
  avgFixRate: number;
  projectHealthItems: ProjectHealthItem[];
};

export type ProjectHealthItem = {
  projectId: number;
  projectName: string;
  healthScore?: number;
  healthLevel?: string;
  totalRecords: number;
  totalIssues: number;
  fixedIssues: number;
  fixRate?: number;
};