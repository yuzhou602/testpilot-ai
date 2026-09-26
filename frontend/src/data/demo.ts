import type { AgentTask, AgentStep, AgentTrace, ApiDefinition, TestProject, SseEvent, Requirement, TestCase, TestBug, TestReport, AgentEvaluation } from '@/types'

export const demoProject: TestProject = {
  id: 1,
  name: 'ShopSphere',
  description: 'Demo e-commerce system for AI testing',
  userId: 1,
  baseUrl: 'http://localhost:3001',
  active: true,
  createdAt: '2026-09-01T10:00:00Z',
  updatedAt: '2026-09-04T21:00:00Z',
}

export const demoTasks: AgentTask[] = [
  {
    id: 102,
    projectId: 1,
    userId: 1,
    goal: '全面测试用户登录接口，包括正常流程、异常输入、边界值和安全性测试',
    context: 'Test POST /api/auth/login endpoint',
    status: 'ANALYZING_FAILURE',
    planJson: JSON.stringify({
      strategy: 'Comprehensive API testing using equivalence partitioning, boundary value analysis, state transition, and error guessing',
      scenarios: [
        { title: 'Normal Login Flow', testType: 'API', priority: 1, riskLevel: 'HIGH' },
        { title: 'Invalid Login Attempts', testType: 'API', priority: 2, riskLevel: 'HIGH' },
        { title: 'Account Locking', testType: 'API', priority: 1, riskLevel: 'CRITICAL' },
        { title: 'Security Tests', testType: 'API', priority: 2, riskLevel: 'HIGH' },
      ],
      steps: [
        { stepName: 'Parse OpenAPI Specification', stepType: 'analyze', tool: null },
        { stepName: 'Analyze Request Schema', stepType: 'analyze', tool: 'readRequirement' },
        { stepName: 'Generate Test Strategy', stepType: 'plan', tool: null },
        { stepName: 'Generate Test Cases', stepType: 'plan', tool: 'generateTestData' },
        { stepName: 'Execute Happy Path Tests', stepType: 'execute', tool: 'executeHttpRequest' },
        { stepName: 'Execute Boundary Tests', stepType: 'execute', tool: 'executeHttpRequest' },
        { stepName: 'Execute Security Tests', stepType: 'execute', tool: 'executeHttpRequest' },
        { stepName: 'Analyze Failure Results', stepType: 'failure_analysis', tool: 'readApplicationLog' },
        { stepName: 'Generate Test Report', stepType: 'report', tool: null },
      ],
      estimatedDuration: '3 minutes',
      riskAreas: ['SQL Injection', 'Account Locking', 'Token Validation'],
    }),
    resultSummary: '18 tests · 16 passed · 2 failed · analysis in progress',
    currentStepIndex: 7,
    totalSteps: 9,
    maxSteps: 50,
    maxRetry: 3,
    currentRetry: 0,
    totalTokens: 15420,
    totalLatencyMs: 12500,
    waitingForUser: false,
    createdAt: '2026-09-04T21:03:00Z',
    updatedAt: '2026-09-04T21:06:34Z',
  },
  {
    id: 101,
    projectId: 1,
    userId: 1,
    goal: 'API 回归测试 - 订单模块',
    context: 'Regression test Order APIs',
    status: 'COMPLETED',
    planJson: JSON.stringify({
      strategy: 'API regression testing for order module',
      scenarios: [{ title: 'Order CRUD', testType: 'API' }],
      steps: [
        { stepName: 'Analyze Order APIs', stepType: 'analyze' },
        { stepName: 'Execute Tests', stepType: 'execute', tool: 'executeHttpRequest' },
        { stepName: 'Generate Report', stepType: 'report' },
      ],
      estimatedDuration: '2 minutes',
      riskAreas: [],
    }),
    resultSummary: 'Completed 3 steps. Passed: 8, Failed: 0',
    currentStepIndex: 2,
    totalSteps: 3,
    maxSteps: 50,
    maxRetry: 3,
    currentRetry: 0,
    totalTokens: 6200,
    totalLatencyMs: 5400,
    waitingForUser: false,
    createdAt: '2026-09-04T20:15:00Z',
    updatedAt: '2026-09-04T20:17:30Z',
    completedAt: '2026-09-04T20:17:30Z',
  },
  {
    id: 100,
    projectId: 1,
    userId: 1,
    goal: '测试购物车优惠券应用逻辑',
    context: 'Test coupon application in cart',
    status: 'RUNNING',
    planJson: JSON.stringify({
      strategy: 'Testing coupon logic with boundary values',
      scenarios: [
        { title: 'Valid Coupon', testType: 'API' },
        { title: 'Expired Coupon', testType: 'API' },
        { title: 'Usage Limit', testType: 'API' },
      ],
      steps: [
        { stepName: 'Parse Coupon API', stepType: 'analyze' },
        { stepName: 'Generate Test Data', stepType: 'plan', tool: 'generateTestData' },
        { stepName: 'Execute Coupon Tests', stepType: 'execute', tool: 'executeHttpRequest' },
        { stepName: 'Analyze Results', stepType: 'assert' },
      ],
      estimatedDuration: '2 minutes',
      riskAreas: ['Coupon Stacking', 'Race Condition'],
    }),
    resultSummary: null,
    currentStepIndex: 1,
    totalSteps: 4,
    maxSteps: 50,
    maxRetry: 3,
    currentRetry: 0,
    totalTokens: 3100,
    totalLatencyMs: 2800,
    waitingForUser: false,
    createdAt: '2026-09-04T21:10:00Z',
    updatedAt: '2026-09-04T21:12:48Z',
  },
]

export const demoSteps: AgentStep[] = [
  { id: 1, taskId: 102, stepIndex: 0, stepName: 'Parse OpenAPI Specification', stepType: 'analyze', status: 'COMPLETED', input: '{"source":"openapi"}', output: '{"endpoints":5,"schemas":3}', toolName: null, toolInput: null, toolOutput: null, errorMessage: null, latencyMs: 320, tokensUsed: 450, model: 'gpt-4o', retryCount: 0, startedAt: '2026-09-04T21:03:00Z', completedAt: '2026-09-04T21:03:01Z' },
  { id: 2, taskId: 102, stepIndex: 1, stepName: 'Analyze Request Schema', stepType: 'analyze', status: 'COMPLETED', input: '{"method":"POST","path":"/api/auth/login"}', output: '{"fields":["username","password"],"required":["username","password"]}', toolName: 'readRequirement', toolInput: '{"projectId":1}', toolOutput: '{"requirements":4}', errorMessage: null, latencyMs: 180, tokensUsed: 380, model: 'gpt-4o', retryCount: 0, startedAt: '2026-09-04T21:03:01Z', completedAt: '2026-09-04T21:03:02Z' },
  { id: 3, taskId: 102, stepIndex: 2, stepName: 'Generate Test Strategy', stepType: 'plan', status: 'COMPLETED', input: '{"target":"login"}', output: '{"strategies":["equivalence","boundary","state_transition","error_guessing"]}', toolName: null, toolInput: null, toolOutput: null, errorMessage: null, latencyMs: 890, tokensUsed: 1200, model: 'gpt-4o', retryCount: 0, startedAt: '2026-09-04T21:03:02Z', completedAt: '2026-09-04T21:03:03Z' },
  { id: 4, taskId: 102, stepIndex: 3, stepName: 'Generate Test Cases', stepType: 'plan', status: 'COMPLETED', input: '{"strategy":"comprehensive"}', output: '{"cases":18,"scenarios":4}', toolName: 'generateTestData', toolInput: '{"schema":{"username":"string","password":"string"}}', toolOutput: '{"records":18}', errorMessage: null, latencyMs: 450, tokensUsed: 890, model: 'gpt-4o', retryCount: 0, startedAt: '2026-09-04T21:03:03Z', completedAt: '2026-09-04T21:03:04Z' },
  { id: 5, taskId: 102, stepIndex: 4, stepName: 'Execute Happy Path Tests', stepType: 'execute', status: 'COMPLETED', input: '{"method":"POST","url":"/api/auth/login","body":{"username":"test001","password":"Test@123"}}', output: '{"status":200,"latency":142}', toolName: 'executeHttpRequest', toolInput: '{"method":"POST","url":"http://localhost:3001/api/auth/login"}', toolOutput: '{"statusCode":200,"body":"{\\"token\\":\\"eyJhbG..."}', errorMessage: null, latencyMs: 142, tokensUsed: 0, model: null, retryCount: 0, startedAt: '2026-09-04T21:03:04Z', completedAt: '2026-09-04T21:03:04Z' },
  { id: 6, taskId: 102, stepIndex: 5, stepName: 'Execute Boundary Tests', stepType: 'execute', status: 'COMPLETED', input: '{"tests":["empty_username","empty_password","max_length"]}', output: '{"passed":5,"failed":1}', toolName: 'executeHttpRequest', toolInput: '{"method":"POST","url":"http://localhost:3001/api/auth/login"}', toolOutput: '{"statusCode":400,"body":"{\\"error\\":\\"Username required\\"}"}', errorMessage: null, latencyMs: 580, tokensUsed: 0, model: null, retryCount: 0, startedAt: '2026-09-04T21:03:05Z', completedAt: '2026-09-04T21:03:06Z' },
  { id: 7, taskId: 102, stepIndex: 6, stepName: 'Execute Security Tests', stepType: 'execute', status: 'FAILED', input: '{"tests":["sql_injection","xss","brute_force"]}', output: '{"passed":3,"failed":1}', toolName: 'executeHttpRequest', toolInput: '{"method":"POST","url":"http://localhost:3001/api/auth/login"}', toolOutput: '{"statusCode":500,"body":"{\\"error\\":\\"Internal Server Error\\"}"}', errorMessage: 'Expected 400, received 500', latencyMs: 320, tokensUsed: 0, model: null, retryCount: 0, startedAt: '2026-09-04T21:03:06Z', completedAt: '2026-09-04T21:03:07Z' },
  { id: 8, taskId: 102, stepIndex: 7, stepName: 'Analyze Failure Results', stepType: 'failure_analysis', status: 'RUNNING', input: '{"failures":2}', output: null, toolName: 'readApplicationLog', toolInput: '{"level":"ERROR","sinceMinutes":5}', toolOutput: '{"logs":[{"message":"DataIntegrityViolationException"}]}', errorMessage: null, latencyMs: 680, tokensUsed: 1500, model: 'gpt-4o', retryCount: 0, startedAt: '2026-09-04T21:03:07Z' },
  { id: 9, taskId: 102, stepIndex: 8, stepName: 'Generate Test Report', stepType: 'report', status: 'PENDING', input: '{"taskId":102}', output: null, toolName: null, toolInput: null, toolOutput: null, errorMessage: null, retryCount: 0, startedAt: '2026-09-04T21:03:09Z' },
]

const now = Date.now()
const t = (offset: number) => now - (180000 - offset * 1000)

export const demoEvents: SseEvent[] = [
  { type: 'TASK_STARTED', data: { taskId: 102, goal: '全面测试用户登录接口' }, timestamp: t(0) },
  { type: 'PLAN_CREATED', data: { strategy: 'Comprehensive API testing', scenarios: 4, steps: 9 }, timestamp: t(1) },
  { type: 'STEP_STARTED', data: { stepIndex: 0, stepName: 'Parse OpenAPI Specification', tool: null }, timestamp: t(2) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 0, result: { endpoints: 5, schemas: 3 } }, timestamp: t(3) },
  { type: 'STEP_STARTED', data: { stepIndex: 1, stepName: 'Analyze Request Schema', tool: 'readRequirement' }, timestamp: t(4) },
  { type: 'TOOL_CALL', data: { tool: 'readRequirement', input: { projectId: 1 } }, timestamp: t(5) },
  { type: 'TOOL_RESULT', data: { tool: 'readRequirement', result: { success: true, output: 'Found 4 requirements' } }, timestamp: t(5.5) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 1, result: { fields: ['username', 'password'] } }, timestamp: t(6) },
  { type: 'STEP_STARTED', data: { stepIndex: 2, stepName: 'Generate Test Strategy', tool: null }, timestamp: t(7) },
  { type: 'AGENT_THINKING', data: { message: 'Analyzing testing strategies...' }, timestamp: t(8) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 2, result: { strategies: 4 } }, timestamp: t(9) },
  { type: 'STEP_STARTED', data: { stepIndex: 3, stepName: 'Generate Test Cases', tool: 'generateTestData' }, timestamp: t(10) },
  { type: 'TOOL_CALL', data: { tool: 'generateTestData', input: { count: 18 } }, timestamp: t(11) },
  { type: 'TOOL_RESULT', data: { tool: 'generateTestData', result: { success: true } }, timestamp: t(11.5) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 3, result: { cases: 18 } }, timestamp: t(12) },
  { type: 'STEP_STARTED', data: { stepIndex: 4, stepName: 'Execute Happy Path Tests', tool: 'executeHttpRequest' }, timestamp: t(13) },
  { type: 'TOOL_CALL', data: { tool: 'executeHttpRequest', input: { method: 'POST', url: '/api/auth/login' } }, timestamp: t(14) },
  { type: 'TOOL_RESULT', data: { tool: 'executeHttpRequest', result: { success: true, statusCode: 200, latencyMs: 142 } }, timestamp: t(14.5) },
  { type: 'ASSERTION', data: { check: 'status == 200', result: 'PASS' }, timestamp: t(15) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 4, result: { passed: 3 } }, timestamp: t(16) },
  { type: 'STEP_STARTED', data: { stepIndex: 5, stepName: 'Execute Boundary Tests', tool: 'executeHttpRequest' }, timestamp: t(17) },
  { type: 'TOOL_CALL', data: { tool: 'executeHttpRequest', input: { method: 'POST', body: { username: '' } } }, timestamp: t(18) },
  { type: 'TOOL_RESULT', data: { tool: 'executeHttpRequest', result: { success: true, statusCode: 400 } }, timestamp: t(18.5) },
  { type: 'ASSERTION', data: { check: 'status == 400', result: 'PASS' }, timestamp: t(19) },
  { type: 'TOOL_CALL', data: { tool: 'executeHttpRequest', input: { method: 'POST', body: { username: 'X'.repeat(300) } } }, timestamp: t(20) },
  { type: 'TOOL_RESULT', data: { tool: 'executeHttpRequest', result: { success: false, statusCode: 500 } }, timestamp: t(20.5) },
  { type: 'ASSERTION', data: { check: 'status == 400', result: 'FAIL (got 500)' }, timestamp: t(21) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 5, result: { passed: 5, failed: 1 } }, timestamp: t(22) },
  { type: 'STEP_STARTED', data: { stepIndex: 6, stepName: 'Execute Security Tests', tool: 'executeHttpRequest' }, timestamp: t(23) },
  { type: 'TOOL_CALL', data: { tool: 'executeHttpRequest', input: { method: 'POST', body: { username: "' OR 1=1--" } } }, timestamp: t(24) },
  { type: 'TOOL_RESULT', data: { tool: 'executeHttpRequest', result: { success: true, statusCode: 400 } }, timestamp: t(24.5) },
  { type: 'ASSERTION', data: { check: 'SQL injection blocked', result: 'PASS' }, timestamp: t(25) },
  { type: 'STEP_COMPLETED', data: { stepIndex: 6, result: { passed: 3, failed: 1 } }, timestamp: t(26) },
  { type: 'FAILURE_ANALYSIS', data: { failures: 2, action: 'Analyzing root cause...' }, timestamp: t(27) },
  { type: 'STEP_STARTED', data: { stepIndex: 7, stepName: 'Analyze Failure Results', tool: 'readApplicationLog' }, timestamp: t(28) },
  { type: 'TOOL_CALL', data: { tool: 'readApplicationLog', input: { level: 'ERROR' } }, timestamp: t(29) },
  { type: 'TOOL_RESULT', data: { tool: 'readApplicationLog', result: { success: true, logs: ['DataIntegrityViolationException'] } }, timestamp: t(29.5) },
  { type: 'BUG_CREATED', data: { bugId: 1, title: 'Username > 255 chars causes 500', severity: 'MAJOR', confidence: 0.91 }, timestamp: t(30) },
]

export const demoApis: ApiDefinition[] = [
  { id: 1, projectId: 1, method: 'POST', path: '/api/auth/login', summary: 'Authenticate user', description: 'Validates credentials and returns an access token.', tag: 'Authentication', requestSchema: '{"username":"test001","password":"Test@123"}', responseSchema: '{"token":"string","user":{"id":1,"username":"test001"}}' },
  { id: 2, projectId: 1, method: 'POST', path: '/api/auth/logout', summary: 'End session', tag: 'Authentication', requestSchema: '{}', responseSchema: '{"success":true}' },
  { id: 3, projectId: 1, method: 'GET', path: '/api/users', summary: 'List users', tag: 'Users', responseSchema: '{"items":[],"total":0}' },
  { id: 4, projectId: 1, method: 'GET', path: '/api/products/search', summary: 'Search catalog', tag: 'Catalog', parameters: '[{"name":"query","in":"query"}]', responseSchema: '{"items":[],"page":1}' },
]

export const demoRequirements: Requirement[] = [
  { id: 1, projectId: 1, title: 'Login credentials are required', description: 'Users must provide a username and password before authentication.', source: 'Product spec · AUTH-01', status: 'ACTIVE', riskLevel: 'HIGH', coverageScore: 100, rules: [
    { id: 1, requirementId: 1, ruleCode: 'R1', ruleDescription: 'Username is required', covered: true, coverageCount: 3 },
    { id: 2, requirementId: 1, ruleCode: 'R2', ruleDescription: 'Password is required', covered: true, coverageCount: 3 },
  ] },
  { id: 2, projectId: 1, title: 'Account locks after five failures', description: 'Five consecutive invalid passwords lock the account for 30 minutes.', source: 'Security policy · SEC-12', status: 'ACTIVE', riskLevel: 'CRITICAL', coverageScore: 80, rules: [
    { id: 3, requirementId: 2, ruleCode: 'R3', ruleDescription: 'The fifth failure triggers a lock', covered: true, coverageCount: 2 },
    { id: 4, requirementId: 2, ruleCode: 'R4', ruleDescription: 'Account automatically unlocks after 30 minutes', covered: false, coverageCount: 0 },
  ] },
  { id: 3, projectId: 1, title: 'Access token lifecycle', description: 'A valid login returns a signed token with a bounded expiry.', source: 'OpenAPI · /auth/login', status: 'ACTIVE', riskLevel: 'MEDIUM', coverageScore: 75, rules: [
    { id: 5, requirementId: 3, ruleCode: 'R5', ruleDescription: 'Token is returned for valid credentials', covered: true, coverageCount: 2 },
    { id: 6, requirementId: 3, ruleCode: 'R6', ruleDescription: 'Expired tokens are rejected', covered: false, coverageCount: 0 },
  ] },
]

export const demoTestCases: TestCase[] = [
  { id: 1, projectId: 1, scenarioId: 1, caseCode: 'TC-101', title: 'Valid username and password', description: 'Happy-path authentication', precondition: 'Active test user exists', steps: 'POST /api/auth/login with valid credentials', expectedResult: '200 and a signed token', testData: '{"username":"test001","password":"Test@123"}', testType: 'API', strategy: 'EQUIVALENCE_PARTITION', priority: 0, automated: true, aiGenerated: true },
  { id: 2, projectId: 1, scenarioId: 2, caseCode: 'TC-102', title: 'Empty password is rejected', description: 'Required-field boundary', precondition: 'None', steps: 'POST with an empty password', expectedResult: '400 Password required', testData: '{"username":"test001","password":""}', testType: 'Boundary', strategy: 'BOUNDARY_VALUE', priority: 0, automated: true, aiGenerated: true },
  { id: 3, projectId: 1, scenarioId: 3, caseCode: 'TC-103', title: 'Fifth invalid password locks account', description: 'State transition at failure threshold', precondition: 'Failure count is four', steps: 'Submit one additional invalid password', expectedResult: '423 Account locked', testData: '{"attempt":5}', testType: 'State', strategy: 'STATE_TRANSITION', priority: 0, automated: true, aiGenerated: true },
  { id: 4, projectId: 1, scenarioId: 4, caseCode: 'TC-104', title: 'Username length 500', description: 'Maximum-length boundary discovered a server error', precondition: 'None', steps: 'Submit a 500-character username', expectedResult: '400 Validation error', testData: '{"username":"<500 chars>"}', testType: 'Boundary', strategy: 'BOUNDARY_VALUE', priority: 1, automated: true, aiGenerated: true },
  { id: 5, projectId: 1, scenarioId: 4, caseCode: 'TC-105', title: 'SQL injection payload is rejected', description: 'Authentication input security', precondition: 'None', steps: "Submit admin' OR 1=1--", expectedResult: '400 Invalid input', testData: '{"username":"SQL payload"}', testType: 'Security', strategy: 'ERROR_GUESSING', priority: 0, automated: true, aiGenerated: true },
]

export const demoBugs: TestBug[] = [
  { id: 1, projectId: 1, executionId: 102, bugCode: 'BUG-102', title: 'Username over 255 characters returns 500', description: 'The login endpoint fails before request validation.', severity: 'MAJOR', status: 'OPEN', module: 'Authentication', stepsToReproduce: '1. Open POST /api/auth/login\n2. Set username length to 500\n3. Send request', expectedResult: '400 with a validation message', actualResult: '500 Internal Server Error', rootCauseHypothesis: 'Request DTO is missing an upper-bound length constraint.', confidence: 0.91, suspectedModule: 'AuthRequest DTO', evidence: 'DataIntegrityViolationException · username varchar(255) · input length 500', suggestedFix: '@Size(max = 255)', reproductionRate: 1, aiGenerated: true, confirmed: false, httpRequest: '{"username":"XXXXX…","password":"Test@123"}', httpResponse: '{"status":500,"message":"Internal Server Error"}', createdAt: '2026-09-04T21:03:26Z' },
  { id: 2, projectId: 1, executionId: 102, bugCode: 'BUG-103', title: 'Lockout response leaks remaining duration', description: 'Authentication response reveals an exact internal lock timer.', severity: 'MINOR', status: 'TRIAGE', module: 'Authentication', stepsToReproduce: 'Trigger account lock and retry login.', expectedResult: 'Generic account locked response', actualResult: 'Response exposes 1,793 seconds remaining', rootCauseHypothesis: 'Internal policy timing is serialized directly.', confidence: 0.74, suspectedModule: 'AuthErrorMapper', evidence: 'Repeated in 3/3 runs', suggestedFix: 'Return a rounded retry-after header.', reproductionRate: 1, aiGenerated: true, confirmed: false, createdAt: '2026-09-04T21:04:10Z' },
]

export const demoReports: TestReport[] = [
  { id: 1, projectId: 1, agentTaskId: 102, title: 'Authentication comprehensive test', summary: 'Core login behavior is stable, but validation gaps create two reproducible failures. Release risk remains medium until the 500 response is fixed.', totalTests: 18, passedTests: 16, failedTests: 2, skippedTests: 0, passRate: 88.9, requirementCoverage: 'Overall 84%. Account unlock boundaries at 29/30/31 minutes are missing.', riskAssessment: 'MEDIUM · Input validation is the dominant risk area.', bugsSummary: '1 major and 1 minor evidence-backed issue.', aiRecommendations: 'Add DTO length validation, cover token expiry, and add 29/30/31-minute lockout boundary tests.', totalExecutionTimeMs: 12500, totalTokensUsed: 15420, generatedAt: '2026-09-04T21:06:34Z' },
]

export const demoEvaluations: AgentEvaluation[] = [
  { id: 1, taskId: 102, projectId: 1, requirementUnderstandingScore: .94, testCaseQualityScore: .90, coverageScore: .84, bugDetectionScore: .92, rootCauseAccuracyScore: .91, toolSelectionScore: .96, completionRateScore: .89, evaluationDetails: 'Strong tool selection and evidence collection. Missing token-expiry coverage reduced completion.', promptVersion: 'agent-v1.3', modelUsed: 'GPT-5', evaluatedAt: '2026-09-04T21:08:00Z' },
  { id: 2, taskId: 102, projectId: 1, requirementUnderstandingScore: .91, testCaseQualityScore: .86, coverageScore: .79, bugDetectionScore: .88, rootCauseAccuracyScore: .83, toolSelectionScore: .90, completionRateScore: .85, evaluationDetails: 'Baseline run used more tokens and produced a weaker failure hypothesis.', promptVersion: 'agent-v1.2', modelUsed: 'Qwen-X', evaluatedAt: '2026-09-04T20:40:00Z' },
]

export const demoTraces: AgentTrace[] = [
  { id: 1, taskId: 102, stepIndex: 0, eventType: 'USER_TASK', input: 'Comprehensively test the login endpoint', output: 'Task accepted', status: 'COMPLETED', latencyMs: 18, createdAt: '2026-09-04T21:03:00Z' },
  { id: 2, taskId: 102, stepIndex: 1, eventType: 'REQUIREMENT_ANALYSIS', input: 'OpenAPI + security policy', output: '{"requirements":6,"risk":"HIGH"}', model: 'GPT-5', tokensUsed: 820, status: 'COMPLETED', latencyMs: 740, createdAt: '2026-09-04T21:03:01Z' },
  { id: 3, taskId: 102, stepIndex: 2, eventType: 'PLAN_CREATED', input: 'Testing goal and available tools', output: '{"steps":9,"scenarios":4}', model: 'GPT-5', tokensUsed: 1200, status: 'COMPLETED', latencyMs: 890, createdAt: '2026-09-04T21:03:02Z' },
  { id: 4, taskId: 102, stepIndex: 5, eventType: 'TOOL_CALL', input: '{"method":"POST","path":"/api/auth/login","usernameLength":500}', output: '{"status":500,"latencyMs":216}', toolName: 'executeHttpRequest', status: 'COMPLETED', latencyMs: 216, createdAt: '2026-09-04T21:03:25Z' },
  { id: 5, taskId: 102, stepIndex: 6, eventType: 'ASSERTION_FAILED', input: 'status == 400', output: 'received 500', status: 'FAILED', error: 'Unexpected server error', createdAt: '2026-09-04T21:03:26Z' },
  { id: 6, taskId: 102, stepIndex: 7, eventType: 'TOOL_RESULT', input: '{"level":"ERROR"}', output: 'DataIntegrityViolationException: value too long for username varchar(255)', toolName: 'readApplicationLog', status: 'COMPLETED', latencyMs: 122, createdAt: '2026-09-04T21:03:27Z' },
  { id: 7, taskId: 102, stepIndex: 7, eventType: 'FAILURE_ANALYSIS', input: 'HTTP response + log + schema', output: 'Correlating three evidence sources', model: 'GPT-5', tokensUsed: 680, status: 'RUNNING', latencyMs: 1400, createdAt: '2026-09-04T21:03:28Z' },
  { id: 8, taskId: 102, stepIndex: 7, eventType: 'AI_HYPOTHESIS', input: 'Evidence bundle EV-102', output: 'Request DTO lacks @Size(max = 255). Confidence 91%.', model: 'GPT-5', tokensUsed: 540, status: 'RUNNING', createdAt: '2026-09-04T21:03:29Z' },
]
