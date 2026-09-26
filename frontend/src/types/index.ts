export interface AgentTask {
  id: number
  projectId: number
  userId: number
  goal: string
  context?: string | null
  status: AgentTaskStatus
  planJson?: string | null
  resultSummary?: string | null
  currentStepIndex: number
  totalSteps: number
  maxSteps: number
  maxRetry?: number
  currentRetry: number
  totalTokens?: number
  totalLatencyMs?: number
  waitingForUser: boolean
  userConfirmationData?: string | null
  createdAt: string
  updatedAt: string
  completedAt?: string | null
}

export type AgentTaskStatus =
  | 'CREATED'
  | 'ANALYZING'
  | 'PLANNING'
  | 'READY'
  | 'RUNNING'
  | 'WAITING_TOOL'
  | 'WAITING_USER'
  | 'REPLANNING'
  | 'ANALYZING_FAILURE'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'

export interface AgentStep {
  id: number
  taskId: number
  stepIndex: number
  stepName: string
  stepType?: string
  status: StepStatus
  input?: string | null
  output?: string | null
  toolName?: string | null
  toolInput?: string | null
  toolOutput?: string | null
  errorMessage?: string | null
  latencyMs?: number
  tokensUsed?: number
  model?: string | null
  retryCount: number
  startedAt: string | null
  completedAt?: string | null
}

export type StepStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SKIPPED' | 'WAITING_TOOL' | 'WAITING_USER'

export interface AgentTrace {
  id: number
  taskId: number
  stepIndex: number
  eventType: string
  input?: string | null
  output?: string | null
  toolName?: string | null
  status?: string | null
  latencyMs?: number
  tokensUsed?: number
  model?: string | null
  error?: string | null
  metadata?: string | null
  createdAt: string
}

export interface TestProject {
  id: number
  name: string
  description?: string
  userId: number
  baseUrl?: string
  openApiUrl?: string
  openApiSpec?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface TestBug {
  id: number
  projectId: number
  executionId: number
  bugCode: string
  title: string
  description?: string
  severity: string
  status: string
  module?: string
  stepsToReproduce?: string
  expectedResult?: string
  actualResult?: string
  rootCauseHypothesis?: string
  confidence?: number
  suspectedModule?: string
  evidence?: string
  httpRequest?: string
  httpResponse?: string
  suggestedFix?: string
  reproductionRate: number
  aiGenerated: boolean
  confirmed: boolean
  createdAt: string
}

export interface Requirement {
  id: number
  projectId: number
  title: string
  description?: string
  source: string
  status: string
  riskLevel: string
  coverageScore: number
  rules?: RequirementRule[]
}

export interface RequirementRule {
  id: number
  requirementId: number
  ruleCode: string
  ruleDescription: string
  covered: boolean
  coverageCount: number
}

export interface TestCase {
  id: number
  projectId: number
  scenarioId: number
  caseCode: string
  title: string
  description?: string
  precondition?: string
  steps?: string
  expectedResult?: string
  testData?: string
  testType?: string
  strategy?: string
  priority: number
  automated: boolean
  aiGenerated: boolean
}

export interface ApiDefinition {
  id: number
  projectId: number
  method: string
  path: string
  summary?: string
  description?: string
  requestSchema?: string
  responseSchema?: string
  parameters?: string
  tag?: string
}

export interface SseEvent {
  type: string
  data: any
  timestamp: number
}

export interface TestReport {
  id: number
  projectId: number
  agentTaskId: number
  title: string
  summary?: string
  totalTests?: number
  passedTests?: number
  failedTests?: number
  skippedTests?: number
  passRate?: number
  requirementCoverage?: string
  riskAssessment?: string
  bugsSummary?: string
  aiRecommendations?: string
  totalExecutionTimeMs?: number
  totalTokensUsed?: number
  generatedAt: string
}

export interface AgentEvaluation {
  id: number
  taskId: number
  projectId: number
  requirementUnderstandingScore?: number
  testCaseQualityScore?: number
  coverageScore?: number
  bugDetectionScore?: number
  rootCauseAccuracyScore?: number
  toolSelectionScore?: number
  completionRateScore?: number
  evaluationDetails?: string
  promptVersion?: string
  modelUsed?: string
  evaluatedAt: string
}
