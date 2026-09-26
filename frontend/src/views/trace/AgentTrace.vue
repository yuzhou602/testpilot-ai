<template>
  <div class="trace-page">
    <header class="trace-toolbar">
      <div class="trace-leading">
        <button class="icon-control" :aria-label="t('trace.back')" @click="router.push('/workspace')"><el-icon><ArrowLeft /></el-icon></button>
        <div><div class="page-eyebrow">AGENT TRACE · <span class="mono">7f12…ad9</span></div><div class="trace-title"><h2>T-{{ taskId }} · {{ t('trace.authenticationTest') }}</h2><StatusBadge v-if="task" :status="task.status" /></div></div>
      </div>
      <div class="toolbar-actions">
        <div class="view-switch" role="group" aria-label="Trace view"><button :class="{ active: view === 'graph' }" @click="view = 'graph'"><el-icon><Share /></el-icon>{{ t('trace.graph') }}</button><button :class="{ active: view === 'timeline' }" @click="view = 'timeline'"><el-icon><Clock /></el-icon>{{ t('trace.timeline') }}</button></div>
        <el-button><el-icon><Search /></el-icon>{{ t('common.search') }}</el-button><el-button>{{ t('trace.compare') }}</el-button>
      </div>
    </header>

    <div v-if="task" class="trace-summary">
      <div class="trace-goal"><span>{{ t('workspace.mission') }}</span><b>{{ task.goal }}</b></div>
      <div><span>{{ t('workspace.duration') }}</span><b class="mono">{{ duration }}</b></div><div><span>Tokens</span><b class="mono">{{ task.totalTokens?.toLocaleString() || '1,248' }}</b></div><div><span>{{ t('trace.nodes') }}</span><b class="mono">{{ traces.length }}</b></div><div><span>{{ t('evaluation.model') }}</span><b class="mono">GPT-5</b></div>
    </div>

    <div class="trace-workspace">
      <aside class="trace-outline">
        <div class="pane-header"><span>{{ t('trace.outline') }}</span><button :title="t('shell.collapse')"><el-icon><DArrowLeft /></el-icon></button></div>
        <div class="outline-body">
          <button v-for="group in outline" :key="group.label" :class="{ active: activeGroup === group.label }" @click="activeGroup = group.label"><span class="outline-dot" :class="group.state"></span><div><b>{{ t(group.label) }}</b><span>{{ group.count }} {{ t('trace.nodes').toLowerCase() }} · {{ group.duration }}</span></div><el-icon><ArrowRight /></el-icon></button>
        </div>
        <div class="outline-filters"><label><input v-model="failurePath" type="checkbox" />{{ t('trace.failureOnly') }}</label><label><input v-model="criticalPath" type="checkbox" />{{ t('trace.criticalPath') }}</label></div>
      </aside>

      <main class="trace-canvas">
        <div class="canvas-toolbar"><div><span class="legend agent"></span>Agent <span class="legend tool"></span>Tool <span class="legend error"></span>{{ t('common.error') }} <span class="legend evidence"></span>{{ t('workspace.evidence') }}</div><span class="mono">{{ t('trace.live') }} · 1.4s</span></div>
        <div class="trace-focus">
          <span class="focus-signal"><i></i></span>
          <div><b>{{ t('trace.failureIsolated') }}</b><small>HTTP 500 → {{ t('workspace.applicationLog') }} → {{ t('trace.rootCauseHypothesis') }}</small></div>
          <em class="mono">3 {{ t('trace.linkedEvidence') }}</em>
        </div>
        <VueFlow v-if="view === 'graph'" :nodes="nodes" :edges="edges" :fit-view-on-init="true" :min-zoom="0.45" :max-zoom="1.5" class="agent-flow" @node-click="onNodeClick">
          <Background :gap="20" :size="1" pattern-color="var(--border-subtle)" />
          <Controls position="bottom-left" :show-interactive="false" />
          <template #node-trace="{ data, selected }">
            <div class="flow-node" :class="[data.kind, { selected }]">
              <header><span class="node-icon"><el-icon><component :is="nodeIcon(data.kind)" /></el-icon></span><span>{{ data.type }}</span><StatusBadge :status="data.status" /></header>
              <b>{{ data.title }}</b><p>{{ data.summary }}</p>
              <footer><span class="mono">{{ data.duration }}</span><span v-if="data.tokens" class="mono">{{ data.tokens }} tokens</span></footer>
            </div>
          </template>
        </VueFlow>

        <div v-else class="timeline-view">
          <button v-for="trace in traces" :key="trace.id" class="timeline-row" :class="nodeKind(trace.eventType)" @click="selectTrace(trace)">
            <span class="timeline-time mono">{{ formatTime(trace.createdAt) }}</span><span class="timeline-spine"><i></i></span><span class="timeline-type mono">{{ formatEventType(trace.eventType) }}</span><span class="timeline-summary">{{ trace.output || trace.input }}</span><span class="timeline-duration mono">{{ trace.latencyMs || 0 }}ms</span>
          </button>
        </div>
      </main>

      <aside class="trace-detail">
        <div class="pane-header"><span>{{ t('trace.nodeDetail') }}</span><button :aria-label="t('common.close')" @click="selectedTrace = null"><el-icon><Close /></el-icon></button></div>
        <template v-if="selectedTrace">
          <div class="node-summary"><span class="soft-tag agent-tag">{{ formatEventType(selectedTrace.eventType) }}</span><h3>{{ detailTitle }}</h3><p>{{ selectedTrace.output || selectedTrace.error }}</p></div>
          <div class="detail-tabs"><button v-for="tab in detailTabs" :key="tab.value" :class="{ active: detailTab === tab.value }" @click="detailTab = tab.value">{{ tab.label }}</button></div>
          <div class="detail-body">
            <template v-if="detailTab === 'Summary'">
              <div class="kv-list"><div class="kv-row"><span class="kv-key">Node ID</span><span class="kv-value mono">span-{{ selectedTrace.id.toString().padStart(4,'0') }}</span></div><div class="kv-row"><span class="kv-key">Step</span><span class="kv-value mono">{{ selectedTrace.stepIndex }}</span></div><div class="kv-row"><span class="kv-key">Duration</span><span class="kv-value mono">{{ selectedTrace.latencyMs || 0 }} ms</span></div><div class="kv-row"><span class="kv-key">Tokens</span><span class="kv-value mono">{{ selectedTrace.tokensUsed || '—' }}</span></div><div class="kv-row"><span class="kv-key">Model</span><span class="kv-value mono">{{ selectedTrace.model || '—' }}</span></div><div class="kv-row"><span class="kv-key">Tool</span><span class="kv-value mono">{{ selectedTrace.toolName || '—' }}</span></div></div>
              <div v-if="selectedTrace.error" class="detail-error"><span>ERROR</span><p>{{ selectedTrace.error }}</p></div>
            </template>
            <pre v-else-if="detailTab === 'Input'" class="code-block">{{ formatJson(selectedTrace.input || 'No input recorded') }}</pre>
            <pre v-else-if="detailTab === 'Output'" class="code-block">{{ formatJson(selectedTrace.output || 'No output recorded') }}</pre>
            <div v-else class="evidence-list"><button><span>RESPONSE</span><b>500 Internal Server Error</b><code>216 ms</code></button><button><span>{{ t('workspace.applicationLog') }}</span><b>DataIntegrityViolationException</b><code>trace 7f12…ad9</code></button><button><span>SCHEMA</span><b>username varchar(255)</b><code>OpenAPI + DB</code></button><div class="evidence-conclusion"><span>{{ t('trace.aiHypothesis') }}</span><b>{{ t('trace.validationMissing') }}</b><small>{{ t('trace.hypothesisNotice') }}</small></div></div>
          </div>
        </template>
        <div v-else class="detail-empty"><el-icon><Aim /></el-icon><b>Select a node</b><span>Inspect input, output, duration, model, tool and linked evidence.</span></div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { Aim, ArrowLeft, ArrowRight, Clock, Close, Connection, DArrowLeft, Document, MagicStick, Search, Share, Warning } from '@element-plus/icons-vue'
import { VueFlow, type NodeMouseEvent } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import { agentApi } from '@/api'
import { demoTasks, demoTraces } from '@/data/demo'
import type { AgentTask, AgentTrace } from '@/types'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { forceDemoMode } from '@/config/runtime'

const route = useRoute(), router = useRouter()
const { t } = useI18n()
const taskId = Number(route.params.taskId) || 102
const task = ref<AgentTask | null>(null)
const traces = ref<AgentTrace[]>([])
const selectedTrace = ref<AgentTrace | null>(null)
const view = ref<'graph'|'timeline'>('graph')
const detailTab = ref('Summary')
const activeGroup = ref('trace.execution')
const failurePath = ref(false), criticalPath = ref(false)
const outline = [{ label: 'trace.goal', count: 1, duration: '120ms', state: 'done' },{ label: 'trace.planning', count: 2, duration: '4.0s', state: 'done' },{ label: 'trace.execution', count: 3, duration: '1.2s', state: 'fail' },{ label: 'trace.analysis', count: 2, duration: '2.5s', state: 'running' },{ label: 'trace.reporting', count: 0, duration: 'pending', state: 'pending' }]
const detailTabs = computed(() => [{ value: 'Summary', label: t('trace.summary') }, { value: 'Input', label: t('trace.input') }, { value: 'Output', label: t('trace.output') }, { value: 'Evidence', label: t('workspace.evidence') }])
const duration = computed(() => task.value?.totalLatencyMs ? `${Math.floor(task.value.totalLatencyMs / 60000).toString().padStart(2,'0')}:${Math.floor((task.value.totalLatencyMs % 60000)/1000).toString().padStart(2,'0')}` : '02:34')
const detailTitle = computed(() => selectedTrace.value?.toolName || formatEventType(selectedTrace.value?.eventType || 'Trace node'))

const nodes = ref<any[]>([]), edges = ref<any[]>([])
function buildGraph() {
  const snake = [
    { x: 60, y: 24 }, { x: 350, y: 24 },
    { x: 350, y: 174 }, { x: 60, y: 174 },
    { x: 60, y: 324 }, { x: 350, y: 324 },
    { x: 350, y: 474 }, { x: 60, y: 474 },
  ]
  nodes.value = traces.value.map((trace, index) => ({ id: String(trace.id), type: 'trace', position: snake[index] || { x: index % 2 ? 350 : 60, y: Math.floor(index / 2) * 150 + 24 }, data: { trace, kind: nodeKind(trace.eventType), type: formatEventType(trace.eventType), title: trace.toolName || nodeTitle(trace), summary: truncate(trace.output || trace.input || '', 74), duration: `${trace.latencyMs || 0}ms`, tokens: trace.tokensUsed, status: nodeStatus(trace.eventType) } }))
  edges.value = traces.value.slice(1).map((trace, index) => ({ id: `e-${index}`, source: String(traces.value[index].id), target: String(trace.id), animated: index === traces.value.length - 2, style: { stroke: trace.eventType.includes('FAIL') ? '#EF4444' : '#475569', strokeWidth: 1.4 } }))
}
function nodeKind(type: string) { if (type.includes('FAIL') || type.includes('ERROR')) return 'error'; if (type.includes('TOOL')) return 'tool'; if (type.includes('HYPOTHESIS') || type.includes('ANALYSIS') || type.includes('PLAN')) return 'agent'; if (type.includes('RESULT')) return 'evidence'; return 'human' }
function nodeStatus(type: string) { return type.includes('FAIL') ? 'FAILED' : type.includes('HYPOTHESIS') ? 'RUNNING' : 'COMPLETED' }
function nodeTitle(trace: AgentTrace) { return ({ USER_TASK: 'User task', REQUIREMENT_ANALYSIS: 'Understand requirements', PLAN_CREATED: 'Build execution plan', ASSERTION_FAILED: 'Validation assertion', FAILURE_ANALYSIS: 'Analyze failure', TOOL_RESULT: 'Collect evidence', AI_HYPOTHESIS: 'Root cause hypothesis' } as Record<string,string>)[trace.eventType] || formatEventType(trace.eventType) }
function nodeIcon(kind: string) { return markRaw(kind === 'tool' ? Connection : kind === 'error' ? Warning : kind === 'evidence' ? Document : kind === 'agent' ? MagicStick : Aim) }
function formatEventType(type: string) { return type.replace(/_/g,' ') }
function formatTime(value: string) { return new Date(value).toLocaleTimeString('en-US',{ hour12:false }) }
function truncate(value: string, length: number) { return value.length > length ? `${value.slice(0,length)}…` : value }
function formatJson(value: string) { try { return JSON.stringify(JSON.parse(value), null, 2) } catch { return value } }
function selectTrace(trace: AgentTrace) { selectedTrace.value = trace; detailTab.value = trace.eventType.includes('FAIL') ? 'Evidence' : 'Summary' }
function onNodeClick(event: NodeMouseEvent) { selectTrace(event.node.data.trace) }
onMounted(async () => {
  if (forceDemoMode) {
    task.value = { ...demoTasks[0], id: taskId }
    traces.value = demoTraces.map(item => ({ ...item, taskId }))
  } else {
    try {
      const [taskRes, traceRes] = await Promise.all([agentApi.getTask(taskId), agentApi.getTaskTraces(taskId)])
      task.value = taskRes.data
      traces.value = traceRes.data?.length ? traceRes.data : demoTraces
    } catch {
      task.value = { ...demoTasks[0], id: taskId }
      traces.value = demoTraces.map(item => ({ ...item, taskId }))
    }
  }
  selectedTrace.value = traces.value.find(item => item.eventType.includes('FAIL')) || traces.value[0] || null
  detailTab.value = selectedTrace.value?.eventType.includes('FAIL') ? 'Evidence' : 'Summary'
  buildGraph()
})
</script>

<style scoped>
.trace-page { height: 100%; display: flex; flex-direction: column; overflow: hidden; background: var(--bg-base); }.trace-toolbar { min-height: 62px; display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 8px 14px; border-bottom: 1px solid var(--border); background: rgba(12,18,27,.97); }.trace-leading, .trace-title { display: flex; align-items: center; gap: 10px; }.trace-title h2 { font-size: 14px; font-weight: 600; }.icon-control { width: 30px; height: 30px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: 5px; background: transparent; color: var(--text-muted); cursor: pointer; }.view-switch { display: flex; padding: 2px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg-base); }.view-switch button { height: 26px; display: flex; align-items: center; gap: 5px; padding: 0 8px; border: 0; border-radius: 4px; background: transparent; color: var(--text-muted); font-size: 10px; cursor: pointer; }.view-switch button.active { color: var(--text-primary); background: var(--bg-active); }
.trace-summary { min-height: 48px; display: grid; grid-template-columns: minmax(360px,1fr) repeat(4,100px); border-bottom: 1px solid var(--border); background: var(--bg-surface); }.trace-summary > div { display: flex; flex-direction: column; justify-content: center; gap: 2px; padding: 7px 12px; }.trace-summary > div + div { border-left: 1px solid var(--border-subtle); }.trace-summary span { color: var(--text-muted); font-size: 9px; text-transform: uppercase; letter-spacing: .05em; }.trace-summary b { overflow: hidden; color: var(--text-secondary); font-size: 10px; font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }.trace-goal b { color: var(--text-primary); font-size: 11px; }
.trace-workspace { min-height: 0; flex: 1; display: grid; grid-template-columns: 210px minmax(430px,1fr) 310px; }.trace-outline, .trace-detail { display: flex; flex-direction: column; min-width: 0; background: rgba(10,15,23,.98); }.trace-outline { border-right: 1px solid var(--border); }.trace-detail { border-left: 1px solid var(--border); }.pane-header { height: 39px; display: flex; align-items: center; justify-content: space-between; padding: 0 11px; border-bottom: 1px solid var(--border-subtle); color: var(--text-secondary); font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: .05em; }.pane-header button { width: 26px; height: 26px; display: grid; place-items: center; border: 0; background: transparent; color: var(--text-muted); cursor: pointer; }.outline-body { flex: 1; padding: 6px; }.outline-body button { width: 100%; min-height: 48px; display: grid; grid-template-columns: 7px minmax(0,1fr) auto; align-items: center; gap: 8px; padding: 7px 8px; border: 0; border-radius: 5px; background: transparent; color: var(--text-muted); text-align: left; cursor: pointer; }.outline-body button:hover { background: var(--bg-hover); }.outline-body button.active { background: var(--bg-selected); color: var(--text-primary); }.outline-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--text-disabled); }.outline-dot.done { background: var(--success); }.outline-dot.fail { background: var(--danger); }.outline-dot.running { background: var(--info); animation: status-pulse 2.2s infinite; }.outline-body button div { display: grid; gap: 2px; }.outline-body b { font-size: 11px; font-weight: 500; }.outline-body span { color: var(--text-muted); font-size: 9px; }.outline-body .el-icon { font-size: 11px; }.outline-filters { display: grid; gap: 7px; padding: 10px 12px; border-top: 1px solid var(--border-subtle); }.outline-filters label { display: flex; align-items: center; gap: 7px; color: var(--text-muted); font-size: 10px; cursor: pointer; }.outline-filters input { accent-color: var(--primary); }
.trace-canvas { min-width: 0; display: flex; flex-direction: column; overflow: hidden; }.canvas-toolbar { height: 34px; display: flex; align-items: center; justify-content: space-between; padding: 0 11px; border-bottom: 1px solid var(--border-subtle); background: var(--bg-surface); color: var(--text-muted); font-size: 9px; }.canvas-toolbar div { display: flex; align-items: center; gap: 6px; }.legend { width: 5px; height: 5px; margin-left: 5px; border-radius: 50%; background: var(--text-muted); }.legend.agent { background: var(--agent); }.legend.tool { background: var(--info); }.legend.error { background: var(--danger); }.legend.evidence { background: var(--success); }.trace-focus { min-height: 46px; display: grid; grid-template-columns: 22px minmax(0,1fr) auto; align-items: center; gap: 9px; padding: 6px 12px; border-bottom: 1px solid var(--border-subtle); background: var(--bg-surface); }.focus-signal { position: relative; width: 18px; height: 18px; display: grid; place-items: center; border: 1px solid var(--border-active); border-radius: 4px; }.focus-signal::after { display: none; }.focus-signal i { width: 5px; height: 5px; border-radius: 50%; background: var(--info); }.trace-focus div { min-width: 0; display: grid; gap: 1px; }.trace-focus b { color: var(--text-primary); font-size: 10px; font-weight: 600; }.trace-focus small { overflow: hidden; color: var(--text-muted); font-size: 9px; text-overflow: ellipsis; white-space: nowrap; }.trace-focus em { color: var(--text-muted); font-size: 8px; font-style: normal; letter-spacing: .03em; }.agent-flow { flex: 1; background: var(--bg-base); }.flow-node { width: 238px; overflow: hidden; border: 1px solid var(--border); border-left: 2px solid var(--text-muted); border-radius: 6px; background: var(--bg-surface); box-shadow: 0 6px 18px rgba(0,0,0,.12); }.flow-node.agent { border-left-color: var(--agent); }.flow-node.tool { border-left-color: var(--info); }.flow-node.error { border-left-color: var(--danger); }.flow-node.evidence { border-left-color: var(--success); }.flow-node.selected { border-color: var(--primary); box-shadow: 0 0 0 1px rgba(111,145,173,.18); }.flow-node header { min-height: 32px; display: grid; grid-template-columns: 20px minmax(0,1fr) auto; align-items: center; gap: 6px; padding: 5px 8px; border-bottom: 1px solid var(--border-subtle); color: var(--text-muted); font: 600 8px var(--font-mono); }.node-icon { width: 19px; height: 19px; display: grid; place-items: center; border-radius: 4px; background: var(--bg-elevated); color: var(--text-secondary); font-size: 11px; }.flow-node > b { display: block; padding: 8px 9px 2px; font-size: 11px; font-weight: 500; }.flow-node > p { min-height: 32px; padding: 0 9px 7px; color: var(--text-muted); font-size: 9px; line-height: 1.5; }.flow-node footer { display: flex; gap: 10px; padding: 5px 9px; border-top: 1px solid var(--border-subtle); color: var(--text-muted); font-size: 8px; }
.timeline-view { flex: 1; overflow-y: auto; padding: 8px 0; background: var(--bg-surface); }.timeline-row { width: 100%; min-height: 46px; display: grid; grid-template-columns: 76px 18px 130px minmax(0,1fr) 65px; align-items: center; gap: 7px; padding: 5px 12px; border: 0; border-bottom: 1px solid var(--border-subtle); background: transparent; color: var(--text-secondary); text-align: left; cursor: pointer; }.timeline-row:hover { background: var(--bg-hover); }.timeline-time, .timeline-duration { color: var(--text-muted); font-size: 9px; }.timeline-spine { position: relative; align-self: stretch; display: grid; place-items: center; }.timeline-spine::before { content: ''; position: absolute; top: 0; bottom: 0; width: 1px; background: var(--border-active); }.timeline-spine i { z-index: 1; width: 7px; height: 7px; border: 1px solid var(--info); border-radius: 50%; background: var(--bg-surface); }.timeline-row.error .timeline-spine i { border-color: var(--danger); background: var(--danger); }.timeline-row.agent .timeline-spine i { border-color: var(--agent); background: var(--agent); }.timeline-type { color: var(--text-muted); font-size: 8px; }.timeline-summary { overflow: hidden; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }.timeline-duration { text-align: right; }
.node-summary { padding: 13px; border-bottom: 1px solid var(--border-subtle); }.node-summary h3 { margin: 9px 0 4px; font-size: 12px; font-weight: 600; }.node-summary p { color: var(--text-muted); font-size: 10px; line-height: 1.55; }.detail-tabs { height: 36px; display: flex; border-bottom: 1px solid var(--border-subtle); }.detail-tabs button { position: relative; flex: 1; border: 0; background: transparent; color: var(--text-muted); font-size: 9px; cursor: pointer; }.detail-tabs button::after { content: ''; position: absolute; left: 7px; right: 7px; bottom: -1px; height: 2px; background: transparent; }.detail-tabs button.active { color: var(--text-primary); }.detail-tabs button.active::after { background: var(--primary); }.detail-body { flex: 1; overflow-y: auto; padding: 13px; }.detail-error { margin-top: 14px; padding: 9px; border: 1px solid rgba(239,68,68,.25); border-left: 2px solid var(--danger); border-radius: 5px; background: rgba(239,68,68,.055); }.detail-error span { color: var(--danger); font: 600 8px var(--font-mono); }.detail-error p { margin-top: 4px; color: var(--text-secondary); font-size: 10px; }.evidence-list { display: grid; gap: 7px; }.evidence-list button { display: grid; gap: 2px; padding: 9px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-elevated); color: var(--text-secondary); text-align: left; cursor: pointer; }.evidence-list button:hover { border-color: var(--border-active); }.evidence-list span { color: var(--info); font: 600 8px var(--font-mono); }.evidence-list b { font-size: 10px; font-weight: 500; }.evidence-list code { color: var(--text-muted); font-size: 8px; }.detail-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; padding: 20px; color: var(--text-muted); text-align: center; }.detail-empty .el-icon { font-size: 24px; }.detail-empty b { color: var(--text-secondary); font-size: 11px; }.detail-empty span { max-width: 210px; font-size: 9px; line-height: 1.6; }
.evidence-conclusion { display: grid; gap: 4px; margin-top: 4px; padding: 10px; border: 1px solid rgba(111,145,173,.28); border-radius: 5px; background: rgba(111,145,173,.06); }
.evidence-conclusion span { color: var(--agent); }
.evidence-conclusion b { color: var(--text-primary); font-size: 10px; }
.evidence-conclusion small { color: var(--text-muted); font-size: 9px; line-height: 1.5; }
:deep(.vue-flow__controls) { overflow: hidden; border: 1px solid var(--border); border-radius: 5px; box-shadow: none; }:deep(.vue-flow__controls-button) { width: 28px; height: 28px; border-color: var(--border-subtle); background: var(--bg-surface); fill: var(--text-secondary); }:deep(.vue-flow__controls-button:hover) { background: var(--bg-elevated); }
@media (max-width: 1150px) { .trace-workspace { grid-template-columns: 180px minmax(400px,1fr); }.trace-detail { display: none; }.trace-summary { grid-template-columns: minmax(260px,1fr) repeat(3,90px); }.trace-summary > div:last-child { display: none; } }
@media (max-width: 760px) { .trace-toolbar .toolbar-actions > :not(.view-switch) { display: none; }.trace-outline { display: none; }.trace-workspace { grid-template-columns: 1fr; }.trace-summary { grid-template-columns: 1fr 75px; }.trace-summary > div:nth-child(n+3) { display: none; }.trace-goal b { max-width: 220px; }.flow-node { width: 210px; }.timeline-row { grid-template-columns: 64px 16px 100px minmax(0,1fr); }.timeline-duration { display: none; } }
</style>
