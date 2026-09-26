import axios from 'axios'
import type { AgentTask, AgentStep, AgentTrace, TestProject, TestBug, Requirement, TestCase, ApiDefinition, TestReport, AgentEvaluation } from '@/types'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 && import.meta.env.VITE_AUTH_REQUIRED === 'true') {
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Auth
export const authApi = {
  login: (data: { username: string; password: string }) =>
    api.post<any>('/auth/login', data),
  register: (data: { username: string; password: string; email: string; displayName: string }) =>
    api.post<any>('/auth/register', data),
}

// Projects
export const projectApi = {
  list: (userId: number) => api.get<any>(`/projects?userId=${userId}`),
  get: (id: number) => api.get<any>(`/projects/${id}`),
  create: (data: Partial<TestProject>) => api.post<any>('/projects', data),
  update: (id: number, data: Partial<TestProject>) => api.put<any>(`/projects/${id}`, data),
  delete: (id: number) => api.delete<any>(`/projects/${id}`),
}

// Agent Tasks
export const agentApi = {
  createTask: (data: { projectId: number; userId: number; goal: string; context?: string }) =>
    api.post<AgentTask>('/agent/tasks', data),
  getTask: (id: number) => api.get<AgentTask>(`/agent/tasks/${id}`),
  getProjectTasks: (projectId: number) => api.get<AgentTask[]>(`/agent/tasks/project/${projectId}`),
  executeTask: (id: number) => api.post<void>(`/agent/tasks/${id}/execute`),
  cancelTask: (id: number) => api.post<void>(`/agent/tasks/${id}/cancel`),
  getTaskSteps: (id: number) => api.get<AgentStep[]>(`/agent/tasks/${id}/steps`),
  getTaskTraces: (id: number) => api.get<any>(`/agent/tasks/${id}/traces`),
}

// OpenAPI
export const openApiApi = {
  import: (projectId: number, spec: string) =>
    api.post<ApiDefinition[]>(`/openapi/import/${projectId}`, spec, {
      headers: { 'Content-Type': 'text/plain' }
    }),
  list: (projectId: number) => api.get<ApiDefinition[]>(`/openapi/list/${projectId}`),
}

// Requirements
export const requirementApi = {
  list: (projectId: number) => api.get<Requirement[]>(`/requirements/project/${projectId}`),
  get: (id: number) => api.get<Requirement>(`/requirements/${id}`),
  create: (data: Partial<Requirement>) => api.post<Requirement>('/requirements', data),
  update: (id: number, data: Partial<Requirement>) => api.put<Requirement>(`/requirements/${id}`, data),
  delete: (id: number) => api.delete<void>(`/requirements/${id}`),
}

// Test Cases
export const testCaseApi = {
  listByProject: (projectId: number) => api.get<TestCase[]>(`/testcases/project/${projectId}`),
  listByScenario: (scenarioId: number) => api.get<TestCase[]>(`/testcases/scenario/${scenarioId}`),
}

// Bugs
export const bugApi = {
  list: (projectId: number) => api.get<TestBug[]>(`/bugs/project/${projectId}`),
  get: (id: number) => api.get<TestBug>(`/bugs/${id}`),
  update: (id: number, data: Partial<TestBug>) => api.put<TestBug>(`/bugs/${id}`, data),
}

// Reports
export const reportApi = {
  list: (projectId: number) => api.get<TestReport[]>(`/reports/project/${projectId}`),
  get: (id: number) => api.get<TestReport>(`/reports/${id}`),
}

// Evaluation
export const evaluationApi = {
  listByProject: (projectId: number) => api.get<AgentEvaluation[]>(`/evaluation/project/${projectId}`),
  listByTask: (taskId: number) => api.get<AgentEvaluation[]>(`/evaluation/task/${taskId}`),
}

// Dashboard
export const dashboardApi = {
  getStats: (projectId: number) => api.get<any>(`/dashboard/stats/${projectId}`),
}

export default api
