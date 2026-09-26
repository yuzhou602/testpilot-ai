<template>
  <div class="workspace">
    <!-- Left: Task List -->
    <div class="ws-tasks">
      <div class="ws-tasks-header">
        <span class="ws-tasks-title">{{ t('workspace.tasks') }}</span>
        <button class="ws-add-btn" :title="t('workspace.newTest')" @click="showNewTask = true"><span>+</span>{{ t('workspace.newTest') }}</button>
      </div>
      <div class="ws-task-list">
        <div v-if="store.tasks.length === 0 && !showNewTask" class="ws-empty-tasks">
          <div class="empty-state-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          </div>
          <span>{{ t('workspace.noTasks') }}</span>
        </div>
        <button v-for="task in store.tasks" :key="task.id"
             class="ws-task-item" :class="{ active: store.currentTask?.id === task.id }"
             @click="selectTask(task)">
          <StatusBadge :status="task.status" />
          <div class="ws-task-info">
            <span class="ws-task-goal">{{ truncate(task.goal, 50) }}</span>
            <span class="ws-task-meta mono">T-{{ task.id }} · {{ formatTime(task.createdAt) }}</span>
          </div>
        </button>
      </div>
    </div>

    <!-- Center: Agent Workspace -->
    <div class="ws-main">
      <!-- No Task / New Task View -->
      <div v-if="!store.currentTask || showNewTask" class="ws-new-task">
        <div class="ws-new-task-inner">
          <div class="ws-new-kicker"><span></span>{{ t('workspace.demoMode') }}</div>
          <h2>{{ t('workspace.newTaskTitle') }}</h2>
          <p>{{ t('workspace.newTaskHint') }}</p>
          <AgentComposer :loading="store.isAgentRunning" :quick-starts="quickStarts" :placeholder="t('workspace.taskGoalPlaceholder')" @submit="startTask" />
          <div class="ws-start-flow" :aria-label="t('workspace.executionFlow')">
            <span>{{ t('workspace.flowUnderstand') }}</span><i></i><span>{{ t('workspace.flowPlan') }}</span><i></i><span>{{ t('workspace.flowExecute') }}</span><i></i><span>{{ t('workspace.flowEvidence') }}</span>
          </div>
        </div>
      </div>

      <!-- Active Task View -->
      <div v-else class="ws-active">
        <!-- Task Toolbar -->
        <div class="ws-toolbar">
          <div class="ws-toolbar-left">
            <span class="ws-task-id mono">T-{{ store.currentTask.id }}</span>
            <span class="ws-toolbar-sep">·</span>
            <span class="ws-task-title">{{ store.currentTask.goal }}</span>
          </div>
          <div class="ws-toolbar-center">
            <StatusBadge :status="store.currentTask.status" />
          </div>
          <div class="ws-toolbar-right">
            <span class="ws-toolbar-stat mono">{{ store.currentTask.currentStepIndex + 1 }} / {{ store.currentTask.totalSteps }} {{ t('workspace.steps') }}</span>
            <button class="ws-tool-button" :title="t('workspace.openFullTrace')" @click="$router.push(`/trace/${store.currentTask.id}`)"><el-icon><Operation /></el-icon></button>
            <button v-if="store.isDemoMode" class="ws-tool-button" :title="t('workspace.replayDemo')" @click="replayDemo"><el-icon><RefreshRight /></el-icon></button>
            <button class="ws-tool-button" :title="store.currentTask.status === 'WAITING_USER' ? t('workspace.resumeTask') : t('workspace.pauseTask')" @click="togglePause">
              <el-icon><VideoPlay v-if="store.currentTask.status === 'WAITING_USER'" /><VideoPause v-else /></el-icon>
            </button>
            <button class="ws-tool-button danger" :title="t('workspace.stopTask')" @click="stopTask"><el-icon><SwitchButton /></el-icon></button>
          </div>
        </div>

        <!-- Mission definition -->
        <section class="ws-mission" :aria-label="t('workspace.mission')">
          <div class="ws-mission-copy">
            <div class="ws-mission-kicker"><span class="mission-signal"></span>{{ t('workspace.mission') }}</div>
            <div class="ws-goal-text">{{ store.currentTask.goal }}</div>
          </div>
          <div class="ws-mission-progress">
            <div><strong class="mono">{{ store.progress }}%</strong><span>{{ t('workspace.missionComplete') }}</span></div>
            <div class="mission-progress-track"><i :style="{ width: store.progress + '%' }"></i></div>
          </div>
        </section>

        <div v-if="store.isAgentRunning || store.currentTask.status === 'WAITING_USER'" class="ws-current-execution">
          <div class="current-state"><span class="current-orbit"><i></i></span><span>{{ t('workspace.agentFocus') }}</span><small class="mono">08 / 09</small></div>
          <div class="current-copy">
            <span class="current-eyebrow">{{ t('workspace.investigating') }}</span>
            <b>{{ t('workspace.boundaryTest') }}</b>
            <div class="current-request"><code><em>POST</em> /api/auth/login</code><span class="mono">500</span><span>{{ t('workspace.unexpectedResponse') }}</span></div>
            <p>{{ t('workspace.collectingEvidence') }}</p>
          </div>
          <div class="current-actions">
            <button @click="contextTab = 'Evidence'">{{ t('workspace.viewEvidence') }}</button>
            <button @click="$router.push(`/trace/${store.currentTask.id}`)">{{ t('workspace.openTrace') }}</button>
          </div>
        </div>

        <!-- Plan -->
        <div class="ws-plan panel" v-if="plan">
          <div class="panel-header">
            <span>{{ t('workspace.executionPlan') }}</span>
            <span class="mono" style="font-size: 11px; color: var(--text-muted);">{{ plan.steps?.length || 0 }} {{ t('workspace.steps') }}</span>
          </div>
          <div class="panel-body">
            <AgentPlan :steps="plan.steps || []" :currentStepIndex="store.currentTask.currentStepIndex" :executionSteps="store.steps" />
          </div>
        </div>

        <!-- Activity Stream -->
        <div class="ws-activity panel">
          <div class="panel-header">
            <span class="activity-title"><i></i>{{ t('workspace.liveActivity') }}</span>
            <div class="activity-tools">
              <button v-for="filter in activityFilters" :key="filter.value" :class="{ active: activityFilter === filter.value }" @click="activityFilter = filter.value">{{ filter.label }}</button>
              <span class="activity-count mono">{{ filteredEvents.length }} {{ t('workspace.events') }}</span>
            </div>
          </div>
          <AgentActivity :events="filteredEvents" :isRunning="store.isAgentRunning && activityFilter === 'all'" />
        </div>

        <!-- Composer at bottom -->
        <div class="ws-bottom-composer" v-if="!terminalTask">
          <AgentComposer :placeholder="t('workspace.continuePlaceholder')" :loading="store.isAgentRunning" @submit="continueTask" />
        </div>
      </div>
    </div>

    <!-- Right: Context Panel -->
    <div class="ws-context" :class="{ collapsed: contextCollapsed }">
      <button class="ws-context-toggle" @click="contextCollapsed = !contextCollapsed">
        <el-icon><DArrowLeft v-if="contextCollapsed" /><DArrowRight v-else /></el-icon>
      </button>
      <template v-if="!contextCollapsed">
        <div class="ws-context-header">
          <button v-for="tab in ['Overview', 'Evidence', 'Environment']" :key="tab" :class="{ active: contextTab === tab }" @click="contextTab = tab">{{ contextTabLabel(tab) }}</button>
        </div>
        <div class="ws-context-body">
          <template v-if="store.currentTask">
            <template v-if="contextTab === 'Overview'">
            <div class="ctx-section">
              <div class="ctx-label">{{ t('workspace.environment') }}</div>
              <span class="env-badge">TEST</span>
            </div>
            <div class="ctx-section">
              <div class="ctx-label">BASE URL</div>
              <div class="ctx-value mono">{{ currentProject?.baseUrl || 'localhost:8080' }}</div>
            </div>
            <div class="ctx-section">
              <div class="ctx-label">{{ t('workspace.progress') }}</div>
              <div class="ctx-progress">
                <div class="ctx-progress-bar">
                  <div class="ctx-progress-fill" :style="{ width: store.progress + '%' }"></div>
                </div>
                <span class="ctx-progress-text mono">{{ store.progress }}%</span>
              </div>
            </div>
            <div class="ctx-section">
              <div class="ctx-label">{{ t('workspace.tests') }}</div>
              <div class="ctx-stats">
                <div class="ctx-stat"><span class="ctx-stat-val pass">{{ store.completedSteps.length }}</span><span class="ctx-stat-label">{{ t('workspace.pass') }}</span></div>
                <div class="ctx-stat"><span class="ctx-stat-val fail">{{ store.failedSteps.length }}</span><span class="ctx-stat-label">{{ t('workspace.fail') }}</span></div>
                <div class="ctx-stat"><span class="ctx-stat-val">{{ store.steps.length }}</span><span class="ctx-stat-label">{{ t('workspace.total') }}</span></div>
              </div>
            </div>
            <div class="ctx-section">
              <div class="ctx-label">TOKENS</div>
              <div class="ctx-value mono">{{ store.currentTask.totalTokens?.toLocaleString() || '—' }}</div>
            </div>
            <div class="ctx-section">
              <div class="ctx-label">{{ t('workspace.duration') }}</div>
              <div class="ctx-value mono">{{ store.currentTask.totalLatencyMs ? (store.currentTask.totalLatencyMs / 1000).toFixed(1) + 's' : '—' }}</div>
            </div>
            <div class="ctx-section" v-if="store.currentTask.resultSummary">
              <div class="ctx-label">{{ t('workspace.result') }}</div>
              <div class="ctx-value">{{ store.currentTask.resultSummary }}</div>
            </div>
            <div class="ctx-section" v-if="store.currentTask.status === 'COMPLETED'">
              <button class="ctx-btn" @click="$router.push(`/trace/${store.currentTask.id}`)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                {{ t('workspace.viewFullTrace') }}
              </button>
            </div>
            </template>
            <template v-else-if="contextTab === 'Evidence'">
              <div class="ctx-section"><div class="ctx-label">{{ t('workspace.currentEvidence') }}</div><div class="ctx-evidence fail"><span>RESPONSE</span><b>500 Internal Server Error</b><code>216 ms · 418 B</code></div></div>
              <div class="ctx-section"><div class="ctx-evidence"><span>{{ t('workspace.applicationLog') }}</span><b>DataIntegrityViolationException</b><code>trace 7f12…ad9</code></div></div>
              <div class="ctx-section"><div class="ctx-evidence"><span>SCHEMA</span><b>username varchar(255)</b><code>Input length 500</code></div></div>
              <button class="ctx-btn" @click="$router.push(`/trace/${store.currentTask.id}`)"><el-icon><Operation /></el-icon>{{ t('workspace.openEvidenceInTrace') }}</button>
            </template>
            <template v-else>
              <div class="ctx-section"><div class="ctx-label">ENVIRONMENT</div><span class="env-badge">TEST</span></div>
              <div class="ctx-section"><div class="ctx-label">BASE URL</div><div class="ctx-value mono">{{ currentProject?.baseUrl || 'localhost:8080' }}</div></div>
              <div class="ctx-section"><div class="ctx-label">BUILD</div><div class="ctx-value mono">8f31a2 · auth-validation</div></div>
              <div class="ctx-section"><div class="ctx-label">BROWSER</div><div class="ctx-value">Chromium 128 · 1440×900</div></div>
              <div class="ctx-section"><div class="ctx-label">TEST DATA</div><div class="ctx-value">auth-boundary-v3</div></div>
              <div class="ctx-section"><div class="ctx-label">FEATURE FLAGS</div><div class="ctx-value mono">new-lockout-policy=true</div></div>
            </template>
          </template>
          <template v-else>
            <div class="ws-context-empty">
              <p>{{ t('workspace.noTaskSelected') }}</p>
            </div>
          </template>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/stores/app'
import { DArrowLeft, DArrowRight, Operation, RefreshRight, SwitchButton, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import StatusBadge from '@/components/common/StatusBadge.vue'
import AgentComposer from '@/components/agent/AgentComposer.vue'
import AgentPlan from '@/components/agent/AgentPlan.vue'
import AgentActivity from '@/components/agent/AgentActivity.vue'

const store = useAppStore()
const { t } = useI18n()
const showNewTask = ref(false)
const contextCollapsed = ref(false)
const contextTab = ref('Overview')
const activityFilter = ref('all')
const quickStarts = computed(() => [
  { label: t('workspace.presetLogin'), value: t('workspace.presetLoginGoal') },
  { label: t('workspace.presetApi'), value: t('workspace.presetApiGoal') },
  { label: t('workspace.presetUi'), value: t('workspace.presetUiGoal') },
])
const activityFilters = computed(() => [
  { value: 'all', label: t('workspace.filterAll') },
  { value: 'failure', label: t('workspace.filterFailure') },
  { value: 'http', label: 'HTTP' },
  { value: 'browser', label: t('workspace.filterBrowser') },
])
const filteredEvents = computed(() => store.sseEvents.filter(event => {
  if (activityFilter.value === 'all') return true
  const type = event.type.toUpperCase()
  if (activityFilter.value === 'failure') return type.includes('FAIL') || type.includes('ERROR')
  if (activityFilter.value === 'http') return type.includes('HTTP') || type.includes('TOOL')
  return type.includes('BROWSER') || type.includes('LOCATOR') || type.includes('HEAL')
}))

function contextTabLabel(tab: string) {
  const keys: Record<string, string> = {
    Overview: 'workspace.overview',
    Evidence: 'workspace.evidence',
    Environment: 'workspace.environment',
  }
  return t(keys[tab] || 'workspace.overview')
}

const currentProject = computed(() => store.currentProject)
const terminalTask = computed(() => ['COMPLETED', 'FAILED', 'CANCELLED'].includes(store.currentTask?.status || ''))

const plan = computed(() => {
  if (!store.currentTask?.planJson) return null
  try { return JSON.parse(store.currentTask.planJson) }
  catch { return null }
})

onMounted(async () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  await store.loadProjects(user.id || 1)
  syncContextWithTask()
})

async function startTask(goal: string) {
  showNewTask.value = false
  await store.createAndExecuteTask(goal)
}

async function continueTask(goal: string) {
  await store.createAndExecuteTask(goal)
}

function selectTask(task: any) {
  store.selectTask(task)
  showNewTask.value = false
  syncContextWithTask()
}

function syncContextWithTask() {
  const status = store.currentTask?.status || ''
  contextTab.value = status === 'ANALYZING_FAILURE' || store.failedSteps.length > 0 ? 'Evidence' : 'Overview'
}

function togglePause() {
  const resuming = store.currentTask?.status === 'WAITING_USER'
  store.togglePauseCurrentTask()
  ElMessage.info(resuming ? 'Agent resumed' : 'Agent paused · execution context preserved')
}

function replayDemo() {
  store.replayCurrentDemoTask()
  ElMessage.success('Demo execution restarted · activity stream is live')
}

async function stopTask() {
  if (!store.currentTask || ['COMPLETED', 'FAILED', 'CANCELLED'].includes(store.currentTask.status)) return
  try {
    await ElMessageBox.confirm('Stop this execution? Existing trace and evidence will be preserved.', 'Stop task', {
      confirmButtonText: 'Stop execution',
      cancelButtonText: 'Keep running',
      type: 'warning',
    })
    await store.cancelCurrentTask()
    ElMessage.success('Execution stopped · evidence preserved')
  } catch {
    // User kept the task running.
  }
}

function truncate(s: string, len: number) {
  return s && s.length > len ? s.substring(0, len) + '...' : s
}

function formatTime(t: string) {
  return new Date(t).toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.workspace {
  display: flex;
  height: 100%;
  overflow: hidden;
  background:
    linear-gradient(rgba(255,255,255,.012) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.01) 1px, transparent 1px),
    var(--bg-base);
  background-size: 32px 32px;
}

/* Left: Tasks */
.ws-tasks {
  width: 208px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(12,18,27,.94);
}

.ws-tasks-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  height: 44px;
  border-bottom: 1px solid var(--border-subtle);
}

.ws-tasks-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: .09em;
  font-family: var(--font-mono);
}

.ws-add-btn {
  min-width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  gap: 5px;
  padding: 0 7px;
  font-size: 11px;
}
.ws-add-btn span { font-size: 14px; line-height: 1; }

.ws-add-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.ws-task-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.ws-empty-tasks {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.ws-task-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 10px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--duration-fast);
  border: 1px solid transparent;
  background: transparent;
  color: inherit;
  text-align: left;
}

.ws-task-item:hover { background: var(--bg-hover); }

.ws-task-item.active {
  background: var(--bg-selected);
  border-color: var(--border);
}

.ws-task-info {
  flex: 1;
  min-width: 0;
}

.ws-task-goal {
  display: block;
  font-size: 12px;
  font-weight: 560;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ws-task-meta {
  display: block;
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* Center: Main */
.ws-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
  background: rgba(7,10,16,.72);
}

/* New Task View */
.ws-new-task {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.ws-new-task-inner {
  max-width: 560px;
  width: 100%;
}

.ws-new-kicker { display: flex; align-items: center; gap: 7px; margin-bottom: 14px; color: var(--text-muted); font: 600 10px/1 var(--font-mono); letter-spacing: .08em; text-transform: uppercase; }
.ws-new-kicker span { width: 6px; height: 6px; border-radius: 50%; background: var(--success); }

.ws-new-task-inner h2 {
  font-size: var(--text-xl);
  font-weight: 600;
  margin-bottom: 6px;
}

.ws-new-task-inner p {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
}

.ws-start-flow { display: flex; align-items: center; justify-content: center; gap: 8px; margin-top: 16px; color: var(--text-muted); font: 500 10px/1 var(--font-mono); }
.ws-start-flow i { width: 18px; height: 1px; background: var(--border-active); }

/* Active Task View */
.ws-active {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ws-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  height: 44px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.ws-toolbar-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  overflow: hidden;
}

.ws-task-id {
  font-size: var(--text-sm);
  color: var(--text-muted);
  font-weight: 600;
}

.ws-toolbar-sep { color: var(--border-active); }
.ws-task-title { min-width: 0; font-size: 13px; font-weight: 560; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ws-toolbar-stat { font-size: var(--text-xs); color: var(--text-muted); }
.ws-toolbar-right { display: flex; flex-shrink: 0; align-items: center; gap: 7px; }
.ws-tool-button { width: 30px; height: 30px; display: grid; place-items: center; border: 1px solid transparent; border-radius: 5px; background: transparent; color: var(--text-muted); cursor: pointer; }
.ws-tool-button:hover { border-color: var(--border-active); color: var(--text-primary); background: var(--bg-hover); }
.ws-tool-button.danger:hover { color: var(--danger); border-color: rgba(239,68,68,.35); }

.ws-mission {
  min-height: 78px;
  display: grid;
  grid-template-columns: minmax(0,1fr) 154px;
  align-items: center;
  gap: 16px;
  margin: 14px 18px 0;
  padding: 13px 16px;
  border-top: 0;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(17,25,37,.42);
}
.ws-mission-kicker { display: flex; align-items: center; gap: 8px; margin-bottom: 7px; color: var(--text-muted); font: 600 9px/1 var(--font-mono); letter-spacing: .07em; text-transform: uppercase; }
.mission-signal { width: 6px; height: 6px; border-radius: 50%; background: #7897aa; }
.ws-mission-copy { min-width: 0; }
.ws-goal-text { overflow: hidden; color: var(--text-primary); font-size: 14px; font-weight: 560; line-height: 1.45; text-overflow: ellipsis; white-space: nowrap; }
.ws-mission-meta { display: flex; align-items: center; gap: 8px; margin-top: 5px; color: var(--text-muted); font-size: 9px; letter-spacing: .04em; }
.ws-mission-meta i { width: 2px; height: 2px; border-radius: 50%; background: var(--border-active); }
.ws-mission-progress { min-width: 68px; display: grid; gap: 8px; }
.ws-mission-progress > div:first-child { display: flex; align-items: baseline; justify-content: flex-end; gap: 7px; }
.ws-mission-progress strong { color: var(--text-primary); font-size: 16px; font-weight: 580; }
.ws-mission-progress span { color: var(--text-muted); font-size: 9px; white-space: nowrap; }
.mission-progress-track { height: 3px; overflow: hidden; border-radius: 2px; background: var(--bg-overlay); }
.mission-progress-track i { display: block; height: 100%; background: var(--primary); transition: width var(--duration-normal); }

.ws-plan {
  margin: 10px 18px 0;
  flex-shrink: 0;
  max-height: 350px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ws-plan .panel-body { min-height: 0; padding: 10px 12px; overflow-y: auto; }

.ws-activity {
  flex: 1;
  margin: 10px 18px 0;
  display: flex;
  flex-direction: column;
  min-height: 118px;
  overflow: hidden;
}

.ws-current-execution {
  position: relative;
  min-height: 126px;
  display: grid;
  grid-template-columns: 116px minmax(0,1fr) 104px;
  align-items: stretch;
  gap: 18px;
  margin: 10px 18px 0;
  padding: 16px;
  overflow: hidden;
  border: 1px solid rgba(239,68,68,.24);
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, rgba(239,68,68,.055), transparent 52%), var(--bg-surface);
  box-shadow: none;
}
.ws-current-execution::after { display: none; }
.current-state { display: flex; align-items: center; align-self: start; gap: 8px; color: var(--text-muted); font: 600 9px var(--font-mono); letter-spacing: .05em; text-transform: uppercase; }
.current-state small { margin-left: auto; color: var(--text-muted); }
.current-orbit { width: 18px; height: 18px; display: grid; place-items: center; border: 1px solid var(--border-active); border-radius: 4px; }
.current-orbit i { width: 5px; height: 5px; border-radius: 50%; background: var(--info); }
.current-copy { min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.current-eyebrow { margin-bottom: 5px; color: var(--warning); font-size: 10px; }
.current-copy b { overflow: hidden; color: var(--text-primary); font-size: 14px; font-weight: 580; text-overflow: ellipsis; white-space: nowrap; }
.current-request { display: flex; align-items: center; gap: 9px; margin-top: 9px; }
.current-copy code { color: var(--text-secondary); font-size: 10px; white-space: nowrap; }
.current-copy code em { margin-right: 5px; color: var(--info); font-style: normal; font-weight: 700; }
.current-request > span:first-of-type { padding: 2px 5px; border: 1px solid rgba(239,68,68,.3); border-radius: 3px; color: var(--danger); font-size: 9px; }
.current-request > span:last-child { color: var(--danger); font-size: 10px; }
.current-copy p { margin: 8px 0 0; color: var(--text-muted); font-size: 10px; line-height: 1.5; }
.current-actions { display: flex; flex-direction: column; justify-content: center; gap: 7px; }
.current-actions button { height: 30px; border: 1px solid var(--border); border-radius: 5px; background: var(--bg-elevated); color: var(--text-secondary); font-size: 10px; cursor: pointer; }
.current-actions button:first-child { border-color: rgba(239,68,68,.3); color: #fca5a5; }
.current-actions button:hover { border-color: var(--border-active); color: var(--text-primary); }
.activity-title { display: inline-flex; align-items: center; gap: 8px; }
.activity-title i { width: 5px; height: 5px; border-radius: 50%; background: var(--success); box-shadow: 0 0 0 3px rgba(34,197,94,.08); }
.activity-count { color: var(--text-muted); font-size: 10px; }
.activity-tools { display: flex; align-items: center; gap: 3px; }
.activity-tools button { height: 24px; padding: 0 7px; border: 0; border-radius: 4px; background: transparent; color: var(--text-muted); font-size: 9px; cursor: pointer; }
.activity-tools button:hover { color: var(--text-primary); background: var(--bg-hover); }
.activity-tools button.active { color: var(--text-primary); background: var(--bg-selected); }
.activity-tools .activity-count { margin-left: 6px; }
@keyframes orbit-breathe { 0%,100% { transform: scale(1); opacity: 1; } 50% { transform: scale(.9); opacity: .62; } }

.ws-bottom-composer {
  padding: 10px 18px 14px;
  flex-shrink: 0;
}
.ws-bottom-composer :deep(.composer-context) { display: none; }
.ws-bottom-composer :deep(.composer-input-wrap) { display: grid; grid-template-columns: minmax(0,1fr) auto; align-items: center; gap: 10px; padding: 6px 10px; }
.ws-bottom-composer :deep(.composer-actions) { padding: 0; border-top: 0; }
.ws-bottom-composer :deep(.composer-hints) { display: none; }
.ws-bottom-composer :deep(.composer-input .el-textarea__inner) { min-height: 30px !important; line-height: 20px; }

/* Right: Context */
.ws-context {
  width: 280px;
  border-left: 1px solid var(--border);
  background: rgba(10,15,23,.97);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width var(--duration-normal);
  position: relative;
}

.ws-context.collapsed {
  width: 28px;
}

.ws-context-toggle {
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-surface);
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  z-index: 5;
}

.ws-context-header {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.ws-context-header button { position: relative; height: 43px; flex: 1; border: 0; background: transparent; color: var(--text-muted); font-size: 10px; cursor: pointer; }
.ws-context-header button::after { content: ''; position: absolute; left: 8px; right: 8px; bottom: -1px; height: 2px; background: transparent; }
.ws-context-header button.active { color: var(--text-primary); }
.ws-context-header button.active::after { background: var(--primary); }

.ws-context-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.ctx-section {
  margin-bottom: 20px;
}

.ctx-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.5px;
  margin-bottom: 7px;
  font-family: var(--font-mono);
}

.ctx-value {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.ctx-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ctx-progress-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-base);
  border-radius: 2px;
  overflow: hidden;
}

.ctx-progress-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 2px;
  transition: width var(--duration-slow);
}

.ctx-progress-text {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.ctx-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.ctx-stat {
  text-align: center;
  padding: 9px 0;
  border: 1px solid var(--border-subtle);
  background: rgba(7,10,16,.72);
  border-radius: var(--radius-md);
}

.ctx-stat-val {
  display: block;
  font-size: var(--text-lg);
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

.ctx-stat-val.pass { color: var(--success); }
.ctx-stat-val.fail { color: var(--danger); }

.ctx-stat-label {
  font-size: 9px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ctx-btn {
  width: 100%;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  transition: all var(--duration-fast);
}

.ctx-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}
.ctx-evidence { display: grid; gap: 3px; padding: 10px; border: 1px solid var(--border-subtle); border-left: 2px solid var(--info); border-radius: 6px; background: var(--bg-elevated); }
.ctx-evidence.fail { border-left-color: var(--danger); }
.ctx-evidence span { color: var(--text-muted); font: 600 9px var(--font-mono); letter-spacing: .04em; }
.ctx-evidence b { overflow: hidden; color: var(--text-secondary); font-size: 11px; font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
.ctx-evidence code { color: var(--text-muted); font-size: 9px; }

.ws-context-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

@media (max-width: 1199px) {
  .ws-context { width: 270px; }
  .ws-tasks { width: 204px; }
}
@media (max-width: 1024px) {
  .ws-context { display: none; }
  .ws-current-execution { grid-template-columns: 115px minmax(0,1fr); }
  .current-actions { display: none; }
}
@media (max-height: 800px) {
  .ws-mission { min-height: 64px; margin-top: 10px; padding-block: 8px; }
  .ws-current-execution { min-height: 94px; margin-top: 8px; padding-block: 9px; }
  .current-copy p { display: none; }
  .ws-plan { margin-top: 8px; }
  .ws-activity { margin-top: 8px; min-height: 108px; }
  .ws-bottom-composer { padding-top: 8px; padding-bottom: 8px; }
}
@media (max-width: 760px) {
  .ws-tasks { display: none; }
  .ws-plan { max-height: 242px; }
  .ws-current-execution { grid-template-columns: 1fr; gap: 5px; overflow: hidden; }
  .current-copy { align-items: flex-start; flex-direction: column; gap: 3px; }
  .current-copy b, .current-copy code { max-width: 100%; overflow: hidden; text-overflow: ellipsis; }
  .ws-goal-text { overflow-wrap: anywhere; white-space: normal; }
  .ws-toolbar-center, .ws-toolbar-stat { display: none; }
  .ws-mission { grid-template-columns: 1fr auto; margin-inline: 10px; }
  .ws-mission-kicker { grid-column: 1 / -1; }
  .ws-plan, .ws-activity, .ws-current-execution { margin-inline: 10px; }
  .ws-bottom-composer { padding-inline: 10px; }
}
</style>
