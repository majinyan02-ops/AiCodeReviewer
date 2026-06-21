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