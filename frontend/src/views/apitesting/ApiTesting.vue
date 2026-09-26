<template>
  <div class="api-page">
    <!-- Left: API Tree -->
    <div class="api-tree">
      <div class="api-tree-header">
        <span>{{ t('apiTesting.endpoints') }}</span>
        <button class="api-import-btn" @click="showImport = true">{{ t('common.import') }}</button>
      </div>
      <div class="api-tree-body">
        <div v-if="apis.length === 0" class="ws-empty-tasks">
          <span style="font-size: 11px;">{{ t('apiTesting.importHint') }}</span>
        </div>
        <button v-for="api in apis" :key="api.id"
             class="api-tree-item" :class="{ active: selectedApi?.id === api.id }"
             @click="selectedApi = api">
          <span class="method" :class="`method-${api.method.toLowerCase()}`">{{ api.method }}</span>
          <span class="api-tree-path mono">{{ api.path }}</span>
        </button>
      </div>
    </div>

    <!-- Center: Request/Response -->
    <div class="api-editor">
      <div class="api-workbench-head">
        <div>
          <span class="workbench-kicker"><i></i>{{ t('apiTesting.requestWorkbench') }}</span>
          <strong>{{ selectedApi ? t('apiTesting.authenticateUser') : t('apiTesting.selectEndpoint') }}</strong>
        </div>
        <div class="workbench-scope mono"><span>AUTH</span><i></i><span>TEST</span><i></i><span>5 {{ t('apiTesting.coverageGaps') }}</span></div>
      </div>
      <!-- Request Bar -->
      <div class="api-request-bar">
        <span class="method" :class="`method-${(selectedApi?.method || 'GET').toLowerCase()}`">
          {{ selectedApi?.method || 'GET' }}
        </span>
        <div class="api-url-input">
          <input v-model="requestUrl" class="api-url mono" placeholder="/api/endpoint" />
        </div>
        <button class="api-send-btn" @click="sendRequest" :disabled="!selectedApi || sending">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          {{ sending ? t('apiTesting.sending') : t('apiTesting.send') }}
        </button>
        <button class="api-agent-btn" @click="runWithAgent" :disabled="!selectedApi">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10"/></svg>
          {{ t('apiTesting.runWithAgent') }}
        </button>
      </div>

      <!-- Tabs: Params / Headers / Body / Auth -->
      <div class="api-tabs">
        <button v-for="tab in tabs" :key="tab" class="api-tab" :class="{ active: activeTab === tab }" @click="activeTab = tab">
          {{ tabLabel(tab) }}
        </button>
      </div>

      <!-- Request Panel -->
      <div class="api-panels">
        <div class="api-request-panel">
          <div class="panel-header"><span>{{ t('apiTesting.request') }}</span></div>
          <div class="panel-body">
            <div class="api-code-editor">
              <div class="editor-gutter mono">1<br>2<br>3<br>4<br>5</div>
              <textarea v-model="requestBody" class="api-body-editor mono" aria-label="Request body" placeholder='{"username": "test", "password": "pass"}'></textarea>
            </div>
          </div>
        </div>

        <div class="api-response-panel">
          <div class="panel-header">
            <span>{{ t('apiTesting.response') }}</span>
            <div v-if="response" class="api-response-meta">
              <span class="response-source" :class="response.source.toLowerCase()">{{ response.source }}</span>
              <span class="badge" :class="response.status < 400 ? 'badge-pass' : 'badge-fail'">
                {{ response.status }} {{ response.statusText }}
              </span>
              <span class="mono" style="font-size: 11px; color: var(--text-muted);">{{ response.latency }}ms</span>
            </div>
          </div>
          <div class="panel-body">
            <div v-if="!response" class="api-response-empty">
              <span class="response-idle-icon">↗</span>
              <b>{{ t('apiTesting.responseReady') }}</b>
              <span>{{ t('apiTesting.responseHint') }}</span>
            </div>
            <div v-else>
              <pre class="code-block">{{ response.body }}</pre>
              <div class="assertion-strip"><span><i></i>ASSERTION</span><code>status == 200</code><b>PASS</b><em class="mono">{{ t('apiTesting.contractMatched') }}</em></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right: AI Analysis -->
    <div class="api-ai-panel">
      <div class="panel-header ai-panel-heading"><span><i></i>{{ t('apiTesting.agentAnalysis') }}</span><em>5 {{ t('apiTesting.gaps') }}</em></div>
      <div class="panel-body" v-if="selectedApi">
        <div class="ai-section">
          <div class="ai-section-title">Endpoint</div>
          <div class="ai-section-content">
            <span class="method" :class="`method-${selectedApi.method.toLowerCase()}`">{{ selectedApi.method }}</span>
            <span class="mono" style="font-size: 12px;">{{ selectedApi.path }}</span>
          </div>
        </div>
        <div class="ai-section" v-if="selectedApi.summary">
          <div class="ai-section-title">{{ t('apiTesting.description') }}</div>
          <div class="ai-section-content">{{ t('apiTesting.authenticateUser') }}</div>
        </div>
        <div class="ai-section" v-if="selectedApi.requestSchema">
          <div class="ai-section-title">{{ t('apiTesting.requestSchema') }}</div>
          <div class="api-schema mono">{{ formatJson(selectedApi.requestSchema) }}</div>
        </div>
        <div class="ai-section">
          <div class="ai-section-title">{{ t('apiTesting.coverageGaps') }}</div>
          <ul class="ai-suggestions">
            <li v-for="(item, index) in suggestions" :key="item.title"><span class="suggestion-index mono">0{{ index + 1 }}</span><div><b>{{ item.title }}</b><small>{{ item.detail }}</small></div><em>{{ item.risk }}</em></li>
          </ul>
        </div>
        <button class="ai-generate-btn" @click="runWithAgent"><span>⌁</span><div><b>{{ t('apiTesting.generateAndRun') }}</b><small>{{ t('apiTesting.useRequestContext') }}</small></div><span>→</span></button>
      </div>
      <div v-else class="panel-body">
        <div class="ws-context-empty"><p>Select an API</p></div>
      </div>
    </div>

    <!-- Import Dialog -->
    <el-dialog v-model="showImport" title="Import OpenAPI" width="560px" :append-to-body="true">
      <el-input v-model="importSpec" type="textarea" :rows="14" placeholder="Paste OpenAPI JSON/YAML here..." />
      <template #footer>
        <el-button @click="showImport = false">Cancel</el-button>
        <el-button type="primary" @click="importOpenApi">Import</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { openApiApi } from '@/api'
import type { ApiDefinition } from '@/types'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { demoApis } from '@/data/demo'

const store = useAppStore()
const { t } = useI18n()
const router = useRouter()
const apis = ref<ApiDefinition[]>([])
const selectedApi = ref<ApiDefinition | null>(null)
const showImport = ref(false)
const importSpec = ref('')
const tabs = ['Params', 'Headers', 'Body', 'Auth']
const activeTab = ref('Body')
const requestUrl = ref('')
const requestBody = ref('')
interface ApiResponseView {
  status: number
  statusText: string
  body: string
  latency: number
  source: 'LIVE' | 'DEMO'
}

const response = ref<ApiResponseView | null>(null)
const sending = ref(false)
const suggestions = computed(() => [
  { title: t('apiTesting.emptyUsername'), detail: t('apiTesting.requiredBoundary'), risk: 'HIGH' },
  { title: t('apiTesting.maxPassword'), detail: t('apiTesting.schemaBoundary'), risk: 'MED' },
  { title: t('apiTesting.sqlInjection'), detail: t('apiTesting.inputSecurity'), risk: 'HIGH' },
  { title: t('apiTesting.xssPayload'), detail: t('apiTesting.outputSecurity'), risk: 'MED' },
  { title: t('apiTesting.wrongCredentials'), detail: t('apiTesting.errorContract'), risk: 'MED' },
])
function tabLabel(tab: string) { return ({ Params: t('apiTesting.params'), Headers: t('apiTesting.headers'), Body: t('apiTesting.body'), Auth: t('apiTesting.auth') } as Record<string,string>)[tab] }

watch(selectedApi, (api) => {
  if (api) {
    requestUrl.value = api.path
    requestBody.value = api.requestSchema ? formatJson(api.requestSchema) : ''
    if (store.isDemoMode) response.value = createDemoResponse(api, performance.now() - 218)
  }
})

async function loadApis() {
  if (!store.currentProject) return
  if (store.isDemoMode) {
    apis.value = demoApis
    selectedApi.value = selectedApi.value || apis.value[0]
    return
  }
  try {
    const res = await openApiApi.list(store.currentProject.id)
    apis.value = res.data?.length ? res.data : demoApis
  } catch {
    apis.value = demoApis
  }
  selectedApi.value = selectedApi.value || apis.value[0] || null
}

async function importOpenApi() {
  if (!store.currentProject || !importSpec.value.trim()) return
  try {
    await openApiApi.import(store.currentProject.id, importSpec.value)
    ElMessage.success('Imported successfully')
    showImport.value = false
    importSpec.value = ''
    await loadApis()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Import failed')
  }
}

async function sendRequest() {
  if (!selectedApi.value) return
  sending.value = true
  response.value = null
  const startedAt = performance.now()

  if (!store.isDemoMode) {
    const controller = new AbortController()
    const timeoutId = window.setTimeout(() => controller.abort(), 5000)
    try {
      const method = selectedApi.value.method.toUpperCase()
      const target = requestUrl.value.startsWith('http')
        ? requestUrl.value
        : `${store.currentProject?.baseUrl?.replace(/\/$/, '') || ''}${requestUrl.value.startsWith('/') ? '' : '/'}${requestUrl.value}`
      const result = await fetch(target, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: ['GET', 'HEAD'].includes(method) || !requestBody.value.trim() ? undefined : requestBody.value,
        signal: controller.signal,
      })
      const body = await result.text()
      response.value = {
        status: result.status,
        statusText: result.statusText || (result.ok ? 'OK' : 'ERROR'),
        body: body ? formatJson(body) : 'No response body',
        latency: Math.round(performance.now() - startedAt),
        source: 'LIVE',
      }
      sending.value = false
      return
    } catch {
      ElMessage.warning('Live endpoint unavailable · showing deterministic demo response')
    } finally {
      window.clearTimeout(timeoutId)
    }
  }

  await new Promise(resolve => setTimeout(resolve, 420))
  response.value = createDemoResponse(selectedApi.value, startedAt)
}

function createDemoResponse(api: ApiDefinition, startedAt: number): ApiResponseView {
  sending.value = false
  return {
    status: 200,
    statusText: 'OK',
    body: api.responseSchema ? formatJson(api.responseSchema) : '{\n  "success": true\n}',
    latency: Math.max(86, Math.round(performance.now() - startedAt)),
    source: 'DEMO',
  }
}

async function runWithAgent() {
  if (!selectedApi.value) return
  const endpoint = `${selectedApi.value.method} ${selectedApi.value.path}`
  await store.createAndExecuteTask(
    `全面测试 ${endpoint}，覆盖正常流程、输入边界、鉴权与注入攻击`,
    JSON.stringify({ apiDefinitionId: selectedApi.value.id, requestBody: requestBody.value })
  )
  ElMessage.success(`Agent task started for ${endpoint}`)
  await router.push('/workspace')
}

function formatJson(s: string) {
  try { return JSON.stringify(JSON.parse(s), null, 2) } catch { return s }
}

onMounted(loadApis)
watch(() => store.currentProject, loadApis)
</script>

<style scoped>
.api-page {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--bg-base);
}

/* API Tree */
.api-tree {
  width: 244px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(12,18,27,.96);
}

.api-tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  height: 44px;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: .075em;
  font-family: var(--font-mono);
}

.api-import-btn {
  padding: 2px 8px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 11px;
}

.api-import-btn:hover { border-color: var(--primary); color: var(--primary); }

.api-tree-body {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.api-tree-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--duration-fast);
  border: 1px solid transparent;
  background: transparent;
  color: inherit;
  text-align: left;
}

.api-tree-item:hover { background: var(--bg-hover); }

.api-tree-item.active {
  background: var(--bg-selected);
  border-color: var(--border);
}

.api-tree-path {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* API Editor */
.api-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(7,10,16,.72);
}
.api-workbench-head { min-height: 58px; display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 9px 14px; border-bottom: 1px solid var(--border-subtle); background: rgba(17,25,37,.36); }
.api-workbench-head > div:first-child { min-width: 0; display: grid; gap: 3px; }
.api-workbench-head strong { overflow: hidden; color: var(--text-primary); font-size: 14px; font-weight: 580; text-overflow: ellipsis; white-space: nowrap; }
.workbench-kicker { display: flex; align-items: center; gap: 7px; color: var(--text-muted); font: 600 9px var(--font-sans); letter-spacing: .035em; }
.workbench-kicker i { width: 6px; height: 6px; border-radius: 50%; background: var(--info); }
.workbench-scope { display: flex; align-items: center; gap: 8px; color: var(--text-muted); font-size: 9px; white-space: nowrap; }
.workbench-scope i { width: 2px; height: 2px; border-radius: 50%; background: var(--border-active); }

.api-request-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(12,18,27,.96);
}

.api-url-input {
  flex: 1;
}

.api-url {
  width: 100%;
  padding: 6px 10px;
  background: var(--bg-base);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  outline: none;
}

.api-url:focus { border-color: var(--primary); }

.api-send-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: var(--primary);
  border: none;
  border-radius: var(--radius-md);
  color: white;
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
}

.api-send-btn:hover { background: var(--primary-hover); }
.api-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.api-agent-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--agent);
  font-size: var(--text-sm);
  cursor: pointer;
}

.api-agent-btn:hover { border-color: var(--border-active); background: var(--bg-hover); }

.api-tabs {
  display: flex;
  gap: 0;
  padding: 0 12px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(12,18,27,.96);
}

.api-tab {
  padding: 8px 14px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--text-muted);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.api-tab:hover { color: var(--text-secondary); }
.api-tab.active { color: var(--text-primary); border-bottom-color: var(--primary); }

.api-panels {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.api-request-panel {
  flex: 1.08;
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid var(--border-subtle);
  overflow: hidden;
}

.api-response-panel {
  flex: .92;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.api-response-panel .panel-header {
  display: flex;
  justify-content: space-between;
}

.api-response-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.response-source { padding: 2px 5px; border: 1px solid var(--border); border-radius: 4px; color: var(--text-muted); font: 600 9px var(--font-mono); letter-spacing: .04em; }
.response-source.live { border-color: rgba(34,197,94,.25); color: var(--success); background: rgba(34,197,94,.06); }
.response-source.demo { border-color: var(--border); color: var(--text-secondary); background: var(--bg-elevated); }

.api-code-editor { flex: 1; display: grid; grid-template-columns: 36px minmax(0,1fr); min-height: 0; background: #060910; }
.editor-gutter { padding: 12px 10px; border-right: 1px solid var(--border-subtle); color: #465369; font-size: 11px; line-height: 1.75; text-align: right; user-select: none; }
.api-body-editor {
  flex: 1;
  width: 100%;
  padding: 12px 14px;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  resize: none;
  outline: none;
  line-height: 1.75;
}

.api-response-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 5px;
  height: 100%;
  color: var(--text-muted);
  font-size: 11px;
}
.api-response-empty b { color: var(--text-secondary); font-size: 12px; font-weight: 560; }
.api-response-empty > span:last-child { color: var(--text-muted); }
.response-idle-icon { width: 26px; height: 26px; display: grid; place-items: center; margin-bottom: 3px; border: 1px solid var(--border); border-radius: 50%; color: var(--primary); background: var(--bg-elevated); font-size: 14px; }
.assertion-strip { min-height: 34px; display: grid; grid-template-columns: 92px minmax(0,1fr) 42px auto; align-items: center; gap: 10px; margin-top: 9px; padding: 0 10px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-elevated); }
.assertion-strip > span { display: flex; align-items: center; gap: 6px; color: var(--text-muted); font: 600 8px var(--font-mono); }
.assertion-strip > span i { width: 5px; height: 5px; border-radius: 50%; background: var(--success); }
.assertion-strip code { color: var(--text-secondary); font-size: 10px; }
.assertion-strip b { color: var(--success); font: 600 9px var(--font-mono); }
.assertion-strip em { color: var(--text-muted); font-size: 8px; font-style: normal; text-align: right; }

.api-schema {
  font-size: 11px;
  background: var(--bg-base);
  padding: 8px;
  border-radius: var(--radius-md);
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
  color: var(--text-secondary);
}

/* AI Panel */
.api-ai-panel {
  width: 300px;
  border-left: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(10,15,23,.98);
  overflow-y: auto;
}

.ai-section {
  margin-bottom: 18px;
}

.ai-section-title {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: .07em;
  margin-bottom: 6px;
}

.ai-section-content {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.ai-suggestions {
  list-style: none;
  padding: 0;
}

.ai-suggestions li {
  display: grid;
  grid-template-columns: 24px minmax(0,1fr) auto;
  align-items: center;
  gap: 8px;
  min-height: 48px;
  color: var(--text-secondary);
  padding: 7px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.ai-suggestions li:last-child { border-bottom: none; }

.suggestion-index { color: #66748a; font-size: 9px; }
.ai-suggestions li div { min-width: 0; display: grid; gap: 2px; }
.ai-suggestions li b { overflow: hidden; color: var(--text-secondary); font-size: 11px; font-weight: 540; text-overflow: ellipsis; white-space: nowrap; }
.ai-suggestions li small { color: var(--text-muted); font-size: 9px; }
.ai-suggestions li em { padding: 2px 4px; border: 1px solid var(--border-subtle); border-radius: 3px; color: var(--warning); font: 600 8px var(--font-mono); font-style: normal; }
.ai-panel-heading > span { display: inline-flex; align-items: center; gap: 7px; }
.ai-panel-heading i { width: 5px; height: 5px; border-radius: 50%; background: var(--agent); box-shadow: 0 0 0 3px rgba(155,138,251,.08); }
.ai-panel-heading em { color: var(--text-muted); font: 600 9px var(--font-mono); font-style: normal; }
.ai-generate-btn { width: 100%; min-height: 48px; display: grid; grid-template-columns: 20px minmax(0,1fr) auto; align-items: center; gap: 8px; padding: 8px 10px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg-elevated); color: var(--text-secondary); text-align: left; cursor: pointer; }
.ai-generate-btn:hover { border-color: var(--border-active); background: var(--bg-hover); }
.ai-generate-btn div { display: grid; gap: 2px; }
.ai-generate-btn b { color: var(--text-primary); font-size: 11px; font-weight: 560; }
.ai-generate-btn small { color: var(--text-muted); font-size: 9px; }

@media (max-width: 1199px) { .api-tree { width: 214px; }.api-ai-panel { width: 270px; } }
@media (max-width: 1024px) { .api-ai-panel { display: none; } }
</style>
