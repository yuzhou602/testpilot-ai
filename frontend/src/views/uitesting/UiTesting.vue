<template>
  <div class="ui-page">
    <!-- Left: Steps -->
    <div class="ui-steps">
      <div class="panel-header"><span>{{ t('uiTesting.steps') }}</span><span class="mono ui-step-count">07 / 08</span></div>
      <div class="ui-steps-body">
        <div v-for="(step, i) in steps" :key="i" class="ui-step" :class="`step-${step.status}`">
          <div class="ui-step-icon">
            <svg v-if="step.status === 'done'" width="14" height="14" viewBox="0 0 12 12"><path d="M2 6l3 3 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/></svg>
            <div v-else-if="step.status === 'running'" class="step-spinner-sm"></div>
            <el-icon v-else-if="step.status === 'failed'" style="color: var(--danger);"><CloseBold /></el-icon>
            <span v-else style="color: var(--text-muted);">{{ i + 1 }}</span>
          </div>
          <div class="ui-step-content">
            <span class="ui-step-name">{{ t(step.name) }}</span>
            <span v-if="step.detail" class="ui-step-detail mono">{{ step.detail }}</span>
          </div>
          <StatusBadge v-if="step.badge" :status="step.badge" />
        </div>
      </div>
    </div>

    <!-- Center: Browser Preview -->
    <div class="ui-browser">
      <div class="ui-browser-bar">
        <div class="browser-dots">
          <span class="dot-r"></span><span class="dot-y"></span><span class="dot-g"></span>
        </div>
        <div class="browser-url mono">{{ currentUrl || 'about:blank' }}</div>
        <div class="browser-actions">
          <button v-if="running" class="browser-action-btn stop" @click="stopRun"><el-icon><VideoPause /></el-icon>{{ t('uiTesting.stop') }}</button>
          <button class="browser-action-btn" :disabled="running" @click="runSteps"><el-icon><VideoPlay /></el-icon>{{ running ? t('uiTesting.running') : t('uiTesting.run') }}</button>
        </div>
      </div>
      <div class="ui-browser-viewport">
        <div class="viewport-hud">
          <span class="mono"><i></i>{{ t('uiTesting.liveBrowser') }}</span>
          <span class="mono">CHROMIUM · 1440×900</span>
        </div>
        <div v-if="healing" class="healing-live-badge"><span></span>{{ t('uiTesting.healingActive') }}</div>
        <div v-if="!screenshot" class="browser-empty">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
          <p>{{ t('uiTesting.emptyBrowser') }}</p>
        </div>
        <div v-else class="browser-screenshot">
          <div class="screenshot-placeholder">
            <div class="mock-page">
              <div class="mock-header"><b>ShopSphere</b><span>{{ t('uiTesting.secureLogin') }}</span></div>
              <div class="mock-form">
                <div class="mock-field">
                  <label>{{ t('auth.username') }}</label>
                  <input type="text" value="testuser" readonly />
                </div>
                <div class="mock-field">
                  <label>{{ t('auth.password') }}</label>
                  <input type="password" value="••••••" readonly />
                </div>
                <button class="mock-btn">{{ t('auth.login') }}</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Inspector -->
    <div class="ui-inspector">
      <div class="panel-header"><span>{{ t('uiTesting.inspector') }}</span></div>
      <div class="ui-inspector-body">
        <div class="inspector-section">
          <div class="inspector-label">LOCATOR</div>
          <div class="inspector-value mono">{{ currentLocator || '—' }}</div>
        </div>
        <div class="inspector-section">
          <div class="inspector-label">{{ t('uiTesting.status') }}</div>
          <StatusBadge v-if="locatorStatus" :status="locatorStatus" />
          <span v-else class="inspector-value">—</span>
        </div>

        <!-- Self-Healing Panel -->
        <div v-if="healing" class="healing-panel">
          <div class="healing-header">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <span>{{ t('uiTesting.locatorHealing') }}</span>
            <em>{{ t('uiTesting.recoveryTrace') }}</em>
          </div>

          <div class="healing-locators">
            <div class="healing-locator old">
              <div class="healing-locator-label">{{ t('uiTesting.before') }}</div>
              <code>{{ healing.old }}</code>
              <span class="healing-fail">{{ t('uiTesting.elementNotFound') }}</span>
            </div>

            <div class="healing-arrow">↓</div>

            <div class="healing-locator new">
              <div class="healing-locator-label">{{ t('uiTesting.after') }}</div>
              <code>{{ healing.new }}</code>
              <div class="healing-meta">
                <span>{{ t('uiTesting.textEvidence') }}: "{{ healing.text }}"</span>
                <span>Role: {{ healing.role }}</span>
              </div>
            </div>
          </div>

          <div class="healing-confidence">
            <span>{{ t('uiTesting.confidence') }}</span>
            <div class="confidence-bar">
              <div class="confidence-fill" :style="{ width: healing.confidence + '%' }"></div>
            </div>
            <span class="mono">{{ healing.confidence }}%</span>
          </div>

          <div class="healing-evidence">
            <div><span>{{ t('uiTesting.accessibleRole') }}</span><b class="mono">100%</b></div>
            <div><span>{{ t('uiTesting.visibleText') }}</span><b class="mono">98%</b></div>
            <div><span>DOM {{ t('uiTesting.position') }}</span><b class="mono">96%</b></div>
            <div><span>{{ t('uiTesting.historyMatch') }}</span><b class="mono">86%</b></div>
          </div>

          <div class="healing-decision">
            <span>{{ t('uiTesting.decision') }}</span>
            <b>{{ t('uiTesting.useCandidate') }}</b>
            <small>{{ t('uiTesting.decisionReason') }}</small>
          </div>

          <div class="healing-result">
            <StatusBadge status="COMPLETED" />
            <span>{{ t('uiTesting.recovered') }}</span>
          </div>
          <div class="healing-actions">
            <button @click="keepRunOnly">{{ t('uiTesting.keepRunOnly') }}</button>
            <button class="primary" @click="proposeUpdate">{{ t('uiTesting.proposeUpdate') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { CloseBold, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const currentUrl = ref('http://localhost:3001/login')
const screenshot = ref(true)
const currentLocator = ref('button[type="submit"]')
const locatorStatus = ref('COMPLETED')
const healing = ref<any>({ old: '#login-btn', new: 'button[type="submit"]', text: 'Login', role: 'button', confidence: 94 })
const running = ref(false)
let runVersion = 0

const steps = ref([
  { name: 'uiTesting.stepNavigate', detail: 'http://localhost:3001/login', status: 'done', badge: 'COMPLETED' },
  { name: 'uiTesting.stepFindUsername', detail: '#username', status: 'done', badge: 'COMPLETED' },
  { name: 'uiTesting.stepFillUsername', detail: 'testuser', status: 'done', badge: 'COMPLETED' },
  { name: 'uiTesting.stepFindPassword', detail: '#password', status: 'done', badge: 'COMPLETED' },
  { name: 'uiTesting.stepFillPassword', detail: '••••••', status: 'done', badge: 'COMPLETED' },
  { name: 'uiTesting.stepClickLogin', detail: '#login-btn', status: 'failed', badge: 'FAILED' },
  { name: 'uiTesting.stepHealing', detail: 'button[type=submit]', status: 'running', badge: 'RUNNING' },
  { name: 'uiTesting.stepAssertDashboard', detail: '', status: 'pending', badge: '' },
])

async function runSteps() {
  if (running.value) return
  const version = ++runVersion
  running.value = true
  screenshot.value = false
  currentUrl.value = 'http://localhost:3001/login'
  currentLocator.value = '—'
  locatorStatus.value = ''
  healing.value = null
  steps.value.forEach(step => { step.status = 'pending'; step.badge = '' })

  try {
    for (let index = 0; index <= 4; index++) {
      setStep(index, 'running', 'RUNNING')
      await waitFor(index === 0 ? 420 : 280, version)
      setStep(index, 'done', 'COMPLETED')
      if (index === 0) screenshot.value = true
      if (index === 1 || index === 3) currentLocator.value = steps.value[index].detail
    }

    setStep(5, 'running', 'RUNNING')
    currentLocator.value = '#login-btn'
    await waitFor(420, version)
    setStep(5, 'failed', 'FAILED')
    locatorStatus.value = 'FAILED'

    setStep(6, 'running', 'RUNNING')
    await waitFor(520, version)
    healing.value = {
      old: '#login-btn',
      new: 'button[type="submit"]',
      text: 'Login',
      role: 'button',
      confidence: 94
    }
    await waitFor(680, version)
    currentLocator.value = 'button[type="submit"]'
    locatorStatus.value = 'COMPLETED'
    setStep(6, 'done', 'COMPLETED')

    setStep(7, 'running', 'RUNNING')
    await waitFor(360, version)
    setStep(7, 'done', 'COMPLETED')
    ElMessage.success(t('uiTesting.runCompleted'))
  } catch {
    // A newer run or an explicit stop invalidated this replay.
  } finally {
    if (version === runVersion) running.value = false
  }
}

function setStep(index: number, status: string, badge: string) {
  steps.value[index].status = status
  steps.value[index].badge = badge
}

async function waitFor(ms: number, version: number) {
  await new Promise(resolve => window.setTimeout(resolve, ms))
  if (version !== runVersion) throw new Error('Replay cancelled')
}

function stopRun() {
  runVersion++
  running.value = false
  const active = steps.value.find(step => step.status === 'running')
  if (active) { active.status = 'pending'; active.badge = '' }
  ElMessage.info(t('uiTesting.runStopped'))
}

function keepRunOnly() {
  ElMessage.info(t('uiTesting.runOnlySaved'))
}

function proposeUpdate() {
  ElMessage.success(t('uiTesting.updateProposed'))
}

onBeforeUnmount(() => { runVersion++ })
</script>

<style scoped>
.ui-page {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--bg-base);
}

/* Steps Panel */
.ui-steps {
  width: 244px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(12,18,27,.96);
}

.ui-steps-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.ui-step-count { color: var(--text-muted); font-size: 9px; }

.ui-step {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  border-radius: var(--radius-md);
  margin-bottom: 2px;
  transition: background var(--duration-fast);
}
.ui-step.step-running { background: linear-gradient(90deg, rgba(59,130,246,.09), transparent 82%); }
.ui-step.step-failed { background: linear-gradient(90deg, rgba(239,68,68,.065), transparent 82%); }

.ui-step:hover { background: var(--bg-hover); }

.ui-step-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
}

.step-done .ui-step-icon { color: var(--success); }
.step-running .ui-step-icon { color: var(--agent); }
.step-failed .ui-step-icon { color: var(--danger); }

.ui-step-content { flex: 1; min-width: 0; }

.ui-step-name {
  display: block;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 18px;
}

.step-done .ui-step-name { color: var(--text-secondary); }

.ui-step-detail {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 1px;
}

.step-spinner-sm {
  width: 10px;
  height: 10px;
  border: 1px solid rgba(126,156,175,.28);
  border-top-color: var(--agent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Browser */
.ui-browser {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(7,10,16,.75);
}

.ui-browser-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 7px 12px;
  background: rgba(12,18,27,.97);
  border-bottom: 1px solid var(--border-subtle);
}

.browser-dots {
  display: flex;
  gap: 5px;
}

.browser-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-r { background: #EF4444; }
.dot-y { background: #F59E0B; }
.dot-g { background: #22C55E; }

.browser-url {
  flex: 1;
  padding: 6px 10px;
  background: var(--bg-base);
  border-radius: var(--radius-md);
  font-size: 11px;
  color: var(--text-muted);
}

.browser-action-btn {
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  background: var(--primary);
  border: none;
  border-radius: var(--radius-md);
  color: white;
  font-size: 11px;
  cursor: pointer;
}
.browser-actions { display: flex; align-items: center; gap: 6px; }
.browser-action-btn.stop { border: 1px solid rgba(239,68,68,.28); background: rgba(239,68,68,.08); color: #fca5a5; }
.browser-action-btn:disabled { opacity: .5; cursor: not-allowed; }

.ui-browser-viewport {
  position: relative;
  flex: 1;
  background:
    linear-gradient(rgba(255,255,255,.013) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.012) 1px, transparent 1px),
    #060910;
  background-size: 28px 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.viewport-hud { position: absolute; left: 16px; right: 16px; top: 13px; z-index: 2; display: flex; align-items: center; justify-content: space-between; color: #56657a; font-size: 8px; letter-spacing: .06em; pointer-events: none; }
.viewport-hud span:first-child { display: inline-flex; align-items: center; gap: 7px; color: #7b8ba2; }
.viewport-hud i { width: 5px; height: 5px; border-radius: 50%; background: var(--success); box-shadow: 0 0 0 3px rgba(34,197,94,.07); }
.healing-live-badge { position: absolute; left: 50%; bottom: 18px; z-index: 2; display: flex; align-items: center; gap: 7px; padding: 6px 9px; border: 1px solid var(--border); border-radius: 5px; background: rgba(10,15,23,.94); color: var(--text-secondary); font: 600 8px var(--font-mono); letter-spacing: .035em; transform: translateX(-50%); }
.healing-live-badge span { width: 6px; height: 6px; border-radius: 50%; background: var(--primary); animation: status-pulse 2.2s infinite; }

.browser-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.browser-screenshot {
  width: 100%;
  height: 100%;
  padding: 48px 34px 44px;
}

.screenshot-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-page {
  width: min(720px, 94%);
  height: min(560px, 86%);
  background: white;
  border-radius: 7px;
  overflow: hidden;
  box-shadow: 0 18px 55px rgba(0,0,0,.36), 0 0 0 1px rgba(255,255,255,.09);
}

.mock-header {
  min-height: 46px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  justify-content: space-between;
  background: #17212b;
  color: white;
  font-size: 13px;
  font-weight: 600;
}
.mock-header span { color: #a8b4c0; font-size: 10px; font-weight: 500; }

.mock-form {
  width: 380px;
  max-width: calc(100% - 40px);
  margin: 64px auto 0;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  box-shadow: 0 12px 34px rgba(15,23,42,.08);
}

.mock-field label {
  display: block;
  font-size: 12px;
  color: #333;
  margin-bottom: 4px;
}

.mock-field input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
}

.mock-btn {
  position: relative;
  width: 100%;
  padding: 10px;
  background: #315f7b;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  outline: 2px solid rgba(49,95,123,.48);
  outline-offset: 4px;
}
.mock-btn::after { content: 'candidate · 94%'; position: absolute; right: -5px; top: -29px; padding: 3px 6px; border-radius: 3px; background: #17212b; color: #b7cad7; font: 600 8px var(--font-mono); letter-spacing: .03em; }

/* Inspector */
.ui-inspector {
  width: 320px;
  border-left: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(10,15,23,.98);
  overflow-y: auto;
}

.ui-inspector-body {
  display: grid;
  grid-template-columns: minmax(0,1fr) auto;
  column-gap: 16px;
  padding: 14px 16px;
}

.inspector-section {
  min-width: 0;
  margin-bottom: 14px;
}

.inspector-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.inspector-value {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  word-break: break-all;
}

/* Self-Healing Panel */
.healing-panel {
  grid-column: 1 / -1;
  margin-top: 4px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: rgba(7,10,16,.38);
}

.healing-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(17,25,37,.52);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.healing-header em { margin-left: auto; color: var(--text-muted); font: 600 8px var(--font-mono); font-style: normal; letter-spacing: .055em; }

.healing-locators {
  padding: 12px;
}

.healing-locator {
  padding: 10px;
  border-radius: var(--radius-md);
  margin-bottom: 4px;
}

.healing-locator.old {
  background: rgba(239,68,68,.045);
  border: 1px solid rgba(239,68,68,.22);
}

.healing-locator.new {
  background: rgba(34,197,94,.045);
  border: 1px solid rgba(34,197,94,.22);
}

.healing-locator-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.healing-locator code {
  display: block;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-primary);
}

.healing-fail {
  font-size: 11px;
  color: var(--danger);
  margin-top: 4px;
}

.healing-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
}

.healing-arrow {
  text-align: center;
  color: var(--text-muted);
  padding: 4px 0;
}

.healing-confidence {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 10px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.confidence-bar {
  flex: 1;
  height: 5px;
  background: var(--bg-base);
  border-radius: 2px;
  overflow: hidden;
}

.confidence-fill {
  height: 100%;
  background: var(--success);
  border-radius: 2px;
}

.healing-result {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
  font-size: var(--text-sm);
  color: var(--success);
  background: rgba(34,197,94,.025);
}
.healing-decision { display: grid; gap: 3px; margin: 0 12px 10px; padding: 9px; border: 1px solid var(--border-subtle); border-radius: 5px; background: rgba(111,145,173,.055); }
.healing-decision span { color: var(--text-muted); font: 600 8px/1 var(--font-mono); letter-spacing: .06em; text-transform: uppercase; }
.healing-decision b { color: var(--text-primary); font-size: 10px; font-weight: 600; }
.healing-decision small { color: var(--text-muted); font-size: 9px; line-height: 1.5; }
.healing-evidence { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; margin: 0 12px 10px; overflow: hidden; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--border-subtle); }
.healing-evidence div { display: flex; justify-content: space-between; gap: 8px; padding: 6px 7px; background: var(--bg-elevated); }
.healing-evidence span { color: var(--text-muted); font-size: 9px; }
.healing-evidence b { color: var(--text-secondary); font-size: 9px; font-weight: 500; }
.healing-actions { display: flex; justify-content: flex-end; gap: 6px; padding: 8px 10px; border-top: 1px solid var(--border-subtle); }
.healing-actions button { min-height: 27px; padding: 0 8px; border: 1px solid var(--border); border-radius: 5px; background: var(--bg-elevated); color: var(--text-secondary); font-size: 9px; cursor: pointer; }
.healing-actions button.primary { color: #fff; border-color: var(--primary); background: var(--primary); }
.healing-actions button:hover { border-color: var(--border-active); color: var(--text-primary); }
@media (max-width: 1000px) { .ui-inspector { display: none; } }
@media (max-width: 1199px) { .ui-steps { width: 214px; }.ui-inspector { width: 290px; }.mock-form { margin-top: 44px; } }
@media (max-width: 700px) { .ui-steps { display: none; } }
</style>
