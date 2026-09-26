import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { agentApi, projectApi } from '@/api'
import type { AgentTask, AgentStep, TestProject, SseEvent } from '@/types'
import { demoEvents, demoProject, demoSteps, demoTasks } from '@/data/demo'
import { forceDemoMode } from '@/config/runtime'

export const useAppStore = defineStore('app', () => {
  const currentProject = ref<TestProject | null>(null)
  const projects = ref<TestProject[]>([])
  const currentTask = ref<AgentTask | null>(null)
  const tasks = ref<AgentTask[]>([])
  const steps = ref<AgentStep[]>([])
  const sseEvents = ref<SseEvent[]>([])
  const isAgentRunning = ref(false)
  const theme = ref<'dark' | 'light'>('dark')
  const isDemoMode = ref(false)
  const streamConnected = ref(false)

  let eventSource: EventSource | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let currentTaskId: number | null = null
  let demoRunVersion = 0
  let reconnectAttempts = 0
  const MAX_RECONNECT_ATTEMPTS = 5
  const HEARTBEAT_INTERVAL = 30000
  const RECONNECT_BASE_DELAY = 1000

  const currentStep = computed(() => {
    if (!currentTask.value) return null
    return steps.value.find(s => s.stepIndex === currentTask.value!.currentStepIndex)
  })

  const completedSteps = computed(() =>
    steps.value.filter(s => s.status === 'COMPLETED')
  )

  const failedSteps = computed(() =>
    steps.value.filter(s => s.status === 'FAILED')
  )

  const progress = computed(() => {
    if (!currentTask.value || currentTask.value.totalSteps === 0) return 0
    return Math.round((currentTask.value.currentStepIndex / currentTask.value.totalSteps) * 100)
  })

  async function loadProjects(userId: number) {
    if (forceDemoMode) {
      projects.value = [demoProject]
      currentProject.value = currentProject.value || demoProject
      isDemoMode.value = true
      await loadTasks(currentProject.value.id)
      return
    }
    try {
      const res = await projectApi.list(userId)
      projects.value = res.data || []
      isDemoMode.value = false
    } catch {
      projects.value = [demoProject]
      isDemoMode.value = true
    }
    if (projects.value.length > 0 && !currentProject.value) {
      currentProject.value = projects.value[0]
      await loadTasks(currentProject.value.id)
    }
  }

  async function selectProject(project: TestProject) {
    currentProject.value = project
    await loadTasks(project.id)
  }

  async function loadTasks(projectId: number) {
    if (forceDemoMode) {
      tasks.value = demoTasks
      currentTask.value = currentTask.value || demoTasks[0]
      steps.value = demoSteps
      sseEvents.value = demoEvents
      isAgentRunning.value = currentTask.value.status === 'ANALYZING_FAILURE' || currentTask.value.status === 'RUNNING'
      isDemoMode.value = true
      return
    }
    try {
      const res = await agentApi.getProjectTasks(projectId)
      tasks.value = res.data || []
      if (tasks.value.length) currentTask.value = currentTask.value || tasks.value[0]
    } catch {
      tasks.value = demoTasks
      currentTask.value = demoTasks[0]
      steps.value = demoSteps
      sseEvents.value = demoEvents
      isAgentRunning.value = true
      isDemoMode.value = true
    }
  }

  function selectTask(task: AgentTask) {
    currentTask.value = task
    if (!isDemoMode.value) return

    const isGeneratedDemoTask = !demoTasks.some(item => item.id === task.id)
    if (task.id === demoTasks[0].id || isGeneratedDemoTask) {
      steps.value = demoSteps
      sseEvents.value = demoEvents
      isAgentRunning.value = task.status === 'ANALYZING_FAILURE' || task.status === 'RUNNING'
      return
    }

    steps.value = task.status === 'COMPLETED'
      ? demoSteps.map(step => ({ ...step, taskId: task.id, status: 'COMPLETED' as const }))
      : []
    sseEvents.value = []
    isAgentRunning.value = false
  }

  function togglePauseCurrentTask() {
    if (!currentTask.value || ['COMPLETED', 'FAILED', 'CANCELLED'].includes(currentTask.value.status)) return
    const isPaused = currentTask.value.status === 'WAITING_USER'
    currentTask.value.status = isPaused ? 'ANALYZING_FAILURE' : 'WAITING_USER'
    currentTask.value.waitingForUser = !isPaused
    isAgentRunning.value = isPaused
  }

  function replayCurrentDemoTask() {
    if (!currentTask.value || !isDemoMode.value) return
    const version = ++demoRunVersion
    const taskId = currentTask.value.id
    steps.value = demoSteps.map(step => ({
      ...step,
      taskId,
      status: 'PENDING',
      startedAt: null,
      completedAt: null,
      errorMessage: null,
    }))
    sseEvents.value = []
    currentTask.value.status = 'RUNNING'
    currentTask.value.currentStepIndex = 0
    currentTask.value.resultSummary = 'Execution in progress'
    currentTask.value.completedAt = null
    currentTask.value.waitingForUser = false
    isAgentRunning.value = true
    pushDemoEvent('TASK_STARTED', { taskId, goal: currentTask.value.goal })
    runDemoStep(0, version)
  }

  function runDemoStep(index: number, version: number) {
    if (version !== demoRunVersion || !currentTask.value || index >= steps.value.length) return
    if (!isAgentRunning.value) {
      window.setTimeout(() => runDemoStep(index, version), 240)
      return
    }

    const step = steps.value[index]
    currentTask.value.currentStepIndex = step.stepIndex
    currentTask.value.status = step.stepIndex >= 7 ? 'ANALYZING_FAILURE' : 'RUNNING'
    step.status = 'RUNNING'
    step.startedAt = new Date().toISOString()
    pushDemoEvent('STEP_STARTED', { stepIndex: step.stepIndex, stepName: step.stepName })

    window.setTimeout(() => {
      if (version !== demoRunVersion || !currentTask.value) return
      if (step.stepIndex === 6) {
        step.status = 'FAILED'
        step.errorMessage = 'Expected 400 validation error, received 500 Internal Server Error'
        pushDemoEvent('STEP_FAILED', { stepIndex: step.stepIndex, error: step.errorMessage })
        pushDemoEvent('FAILURE_ANALYSIS', { message: 'Collecting response, application log, and schema evidence' })
      } else {
        step.status = 'COMPLETED'
        step.completedAt = new Date().toISOString()
        pushDemoEvent('STEP_COMPLETED', { stepIndex: step.stepIndex, result: { success: true } })
      }

      if (index === steps.value.length - 1) {
        currentTask.value.status = 'COMPLETED'
        currentTask.value.resultSummary = '18 tests · 16 passed · 2 failed · evidence report generated'
        currentTask.value.completedAt = new Date().toISOString()
        isAgentRunning.value = false
        pushDemoEvent('TASK_COMPLETED', { summary: currentTask.value.resultSummary })
        return
      }
      runDemoStep(index + 1, version)
    }, step.stepIndex === 7 ? 1050 : 620)
  }

  function pushDemoEvent(type: string, data: Record<string, unknown>) {
    sseEvents.value.push({ type, data, timestamp: Date.now() })
  }

  async function cancelCurrentTask() {
    if (!currentTask.value) return
    if (!isDemoMode.value) await agentApi.cancelTask(currentTask.value.id)
    demoRunVersion++
    currentTask.value.status = 'CANCELLED'
    currentTask.value.waitingForUser = false
    isAgentRunning.value = false
    disconnectSse()
  }

  async function createAndExecuteTask(goal: string, context?: string) {
    if (!currentProject.value) throw new Error('No project selected')

    if (isDemoMode.value) return createDemoTask(goal, context)

    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
      const task = await agentApi.createTask({
        projectId: currentProject.value.id,
        userId: user.id || 1,
        goal,
        context,
      })
      currentTask.value = task.data
      steps.value = []
      sseEvents.value = []
      isAgentRunning.value = true
      connectSse(task.data.id)
      await agentApi.executeTask(task.data.id)
      return task.data
    } catch {
      return createDemoTask(goal, context)
    }
  }

  function createDemoTask(goal: string, context?: string) {
    const task: AgentTask = {
      ...demoTasks[0],
      id: Date.now(),
      goal,
      context,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    currentTask.value = task
    tasks.value = [task, ...tasks.value]
    steps.value = demoSteps.map(step => ({ ...step, taskId: task.id }))
    sseEvents.value = demoEvents
    isAgentRunning.value = true
    isDemoMode.value = true
    return task
  }

  function connectSse(taskId: number) {
    disconnectSse()
    currentTaskId = taskId
    reconnectAttempts = 0
    doConnectSse(taskId)
  }

  function doConnectSse(taskId: number) {
    const token = localStorage.getItem('accessToken')
    eventSource = new EventSource(`/api/agent/tasks/${taskId}/stream?token=${token}`)

    eventSource.onopen = () => {
      streamConnected.value = true
      reconnectAttempts = 0
      startHeartbeat()
    }

    const eventTypes = [
      'TASK_STARTED', 'PLAN_CREATED', 'STEP_STARTED', 'STEP_COMPLETED',
      'STEP_FAILED', 'TOOL_CALL', 'TOOL_RESULT', 'ASSERTION',
      'REPLAN', 'WAITING_USER', 'FAILURE_ANALYSIS', 'BUG_CREATED',
      'TASK_COMPLETED', 'TASK_FAILED', 'AGENT_THINKING', 'COVERAGE_UPDATE'
    ]

    eventTypes.forEach(type => {
      eventSource!.addEventListener(type, (event) => {
        const data = JSON.parse(event.data)
        sseEvents.value.push({ type, data: data.data, timestamp: data.timestamp })
        handleSseEvent(type, data.data)
      })
    })

    eventSource.onerror = () => {
      streamConnected.value = false
      stopHeartbeat()
      eventSource?.close()
      eventSource = null

      if (isAgentRunning.value && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        const delay = Math.min(RECONNECT_BASE_DELAY * Math.pow(2, reconnectAttempts), 10000)
        reconnectAttempts++
        reconnectTimer = setTimeout(() => {
          doConnectSse(taskId)
        }, delay)
      }
    }
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (eventSource && eventSource.readyState === EventSource.OPEN) {
        // EventSource automatically sends keepalive
      }
    }, HEARTBEAT_INTERVAL)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function disconnectSse() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    stopHeartbeat()
    streamConnected.value = false
    currentTaskId = null
  }

  function handleSseEvent(type: string, data: any) {
    switch (type) {
      case 'TASK_STARTED':
        if (currentTask.value) currentTask.value.status = 'RUNNING'
        break
      case 'PLAN_CREATED':
        if (currentTask.value) {
          currentTask.value.totalSteps = data.steps || 0
          currentTask.value.status = 'RUNNING'
        }
        break
      case 'STEP_STARTED':
        const newStep: AgentStep = {
          id: Date.now(),
          taskId: currentTask.value?.id || 0,
          stepIndex: data.stepIndex,
          stepName: data.stepName,
          stepType: '',
          status: 'RUNNING',
          retryCount: 0,
          startedAt: new Date().toISOString(),
        }
        steps.value.push(newStep)
        if (currentTask.value) currentTask.value.currentStepIndex = data.stepIndex
        break
      case 'STEP_COMPLETED':
        const completedIdx = steps.value.findIndex(s => s.stepIndex === data.stepIndex)
        if (completedIdx >= 0) {
          steps.value[completedIdx].status = 'COMPLETED'
          steps.value[completedIdx].output = JSON.stringify(data.result)
          steps.value[completedIdx].completedAt = new Date().toISOString()
        }
        break
      case 'STEP_FAILED':
        const failedIdx = steps.value.findIndex(s => s.stepIndex === data.stepIndex)
        if (failedIdx >= 0) {
          steps.value[failedIdx].status = 'FAILED'
          steps.value[failedIdx].errorMessage = data.error
        }
        break
      case 'TASK_COMPLETED':
        isAgentRunning.value = false
        disconnectSse()
        if (currentTask.value) {
          currentTask.value.status = 'COMPLETED'
          currentTask.value.resultSummary = data.summary
          currentTask.value.completedAt = new Date().toISOString()
        }
        break
      case 'TASK_FAILED':
        isAgentRunning.value = false
        disconnectSse()
        if (currentTask.value) currentTask.value.status = 'FAILED'
        break
    }
  }

  function setTheme(t: 'dark' | 'light') {
    theme.value = t
    document.documentElement.classList.toggle('dark', t === 'dark')
  }

  return {
    currentProject, projects, currentTask, tasks, steps, sseEvents,
    isAgentRunning, theme, isDemoMode, streamConnected,
    currentStep, completedSteps, failedSteps, progress,
    loadProjects, selectProject, loadTasks, selectTask, createAndExecuteTask,
    togglePauseCurrentTask, replayCurrentDemoTask, cancelCurrentTask, setTheme,
    disconnectSse,
  }
})
