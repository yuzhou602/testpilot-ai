<template>
  <div class="bug-page">
    <div class="bug-header">
      <div><span class="page-eyebrow">EVIDENCE-BASED DEFECTS</span><h2>{{ t('bugs.title') }}</h2></div>
      <div class="bug-header-actions">
        <span class="bug-count mono">{{ bugs.length }} {{ t('bugs.defects') }} · 1 {{ t('bugs.highConfidence') }}</span>
      </div>
    </div>

    <div class="bug-content">
      <!-- Bug List -->
      <div class="bug-list">
        <div v-for="bug in bugs" :key="bug.id"
             class="bug-card" :class="{ active: selectedBug?.id === bug.id }"
             @click="selectedBug = bug">
          <div class="bug-card-top">
            <SeverityBadge :level="bug.severity" />
            <span class="bug-id mono">BUG-{{ bug.id }}</span>
          </div>
          <div class="bug-card-title">{{ localizedBugTitle(bug) }}</div>
          <div class="bug-card-meta">
            <span v-if="bug.confidence" class="mono">{{ (bug.confidence * 100).toFixed(0) }}% {{ t('bugs.confidence') }}</span>
            <el-tag v-if="bug.aiGenerated" size="small" type="warning" style="font-size: 10px;">AI</el-tag>
            <el-tag v-if="bug.confirmed" size="small" type="success" style="font-size: 10px;">Confirmed</el-tag>
          </div>
        </div>
        <div v-if="bugs.length === 0" class="tc-empty">
          <p>No bugs found</p>
        </div>
      </div>

      <!-- Bug Detail -->
      <div class="bug-detail" v-if="selectedBug">
        <div class="bug-proofbar"><span><i class="fact"></i>{{ t('bugs.observedFact') }}</span><span><i class="evidence"></i>{{ t('bugs.linkedEvidence') }}</span><span><i class="hypothesis"></i>{{ t('bugs.aiHypothesis') }}</span><b class="mono">3 / 3 {{ t('bugs.reproduced') }}</b></div>
        <div class="bug-detail-header">
          <div class="bug-detail-title-row">
            <span class="bug-detail-id mono">BUG-{{ selectedBug.id }}</span>
            <SeverityBadge :level="selectedBug.severity" />
          </div>
          <h3>{{ localizedBugTitle(selectedBug) }}</h3>
        </div>

        <div class="bug-detail-body">
          <!-- Expected / Actual -->
          <div class="bug-section">
            <div class="bug-two-col">
              <div class="bug-col">
                <div class="bug-col-label">{{ t('bugs.expectedContract') }}</div>
                <div class="bug-col-value">{{ selectedBug.id === 1 ? t('bugs.demoExpected') : (selectedBug.expectedResult || '—') }}</div>
              </div>
              <div class="bug-col">
                <div class="bug-col-label">{{ t('bugs.actualObserved') }}</div>
                <div class="bug-col-value danger">{{ selectedBug.id === 1 ? t('bugs.demoActual') : (selectedBug.actualResult || '—') }}</div>
              </div>
            </div>
          </div>

          <!-- Steps -->
          <div class="bug-section" v-if="selectedBug.stepsToReproduce">
            <div class="bug-section-label">{{ t('bugs.reproductionSteps') }}</div>
            <pre class="code-block">{{ selectedBug.id === 1 ? t('bugs.demoSteps') : selectedBug.stepsToReproduce }}</pre>
          </div>

          <!-- Request / Response -->
          <div class="bug-section" v-if="selectedBug.httpRequest || selectedBug.httpResponse">
            <div class="bug-two-col">
              <div class="bug-col" v-if="selectedBug.httpRequest">
                <div class="bug-col-label">Request</div>
                <pre class="code-block">{{ formatJson(selectedBug.httpRequest) }}</pre>
              </div>
              <div class="bug-col" v-if="selectedBug.httpResponse">
                <div class="bug-col-label">Response</div>
                <pre class="code-block">{{ formatJson(selectedBug.httpResponse) }}</pre>
              </div>
            </div>
          </div>

          <!-- Root Cause Hypothesis -->
          <div class="bug-section" v-if="selectedBug.rootCauseHypothesis">
            <div class="bug-section-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--agent)" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10"/></svg>
              {{ t('bugs.aiRootCause') }}
              <span class="hypothesis-note">{{ t('bugs.unconfirmed') }}</span>
            </div>
            <div class="root-cause-panel">
              <div class="rc-row">
                <span class="rc-label">{{ t('bugs.hypothesis') }}</span>
                <span class="rc-value">{{ selectedBug.id === 1 ? t('bugs.demoHypothesis') : selectedBug.rootCauseHypothesis }}</span>
              </div>
              <div class="rc-row" v-if="selectedBug.suspectedModule">
                <span class="rc-label">{{ t('bugs.suspectedModule') }}</span>
                <span class="rc-value mono">{{ selectedBug.suspectedModule }}</span>
              </div>
              <div class="rc-row" v-if="selectedBug.evidence">
                <span class="rc-label">{{ t('workspace.evidence') }}</span>
                <span class="rc-value">{{ selectedBug.evidence }}</span>
              </div>
              <div class="rc-row" v-if="selectedBug.confidence">
                <span class="rc-label">{{ t('bugs.confidence') }}</span>
                <div class="rc-confidence">
                  <div class="rc-confidence-bar">
                    <div class="rc-confidence-fill" :style="{ width: (selectedBug.confidence * 100) + '%' }"></div>
                  </div>
                  <span class="mono">{{ (selectedBug.confidence * 100).toFixed(0) }}%</span>
                </div>
              </div>
              <div class="rc-row" v-if="selectedBug.suggestedFix">
                <span class="rc-label">{{ t('bugs.fix') }}</span>
                <pre class="code-block" style="margin: 0;">{{ selectedBug.suggestedFix }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="bug-empty">
        <div class="empty-state-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 2l1.88 1.88M14.12 3.88L16 2M9 7.13v-1a3.003 3.003 0 1 1 6 0v1"/><path d="M12 20c-3.3 0-6-2.7-6-6v-3a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v3c0 3.3-2.7 6-6 6z"/></svg>
        </div>
        <p>Select a bug to view details</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import { bugApi } from '@/api'
import type { TestBug } from '@/types'
import SeverityBadge from '@/components/common/SeverityBadge.vue'
import { demoBugs } from '@/data/demo'
import { useI18n } from 'vue-i18n'

const store = useAppStore()
const { t } = useI18n()
const bugs = ref<TestBug[]>([])
const selectedBug = ref<TestBug | null>(null)
function localizedBugTitle(bug: TestBug) { return bug.id === 1 ? t('bugs.demoBug1') : bug.id === 2 ? t('bugs.demoBug2') : bug.title }

function formatJson(s: string) {
  try { return JSON.stringify(JSON.parse(s), null, 2) } catch { return s }
}

async function load() {
  if (!store.currentProject) return
  if (store.isDemoMode) {
    bugs.value = demoBugs
    selectedBug.value = selectedBug.value || bugs.value[0]
    return
  }
  try {
    const res = await bugApi.list(store.currentProject.id)
    bugs.value = res.data?.length ? res.data : demoBugs
  } catch {
    bugs.value = demoBugs
  }
  if (!selectedBug.value && bugs.value.length) selectedBug.value = bugs.value[0]
}

onMounted(load)
watch(() => store.currentProject, load)
</script>

<style scoped>
.bug-page { padding: 16px 20px; height: 100%; display: flex; flex-direction: column; }

.bug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.bug-header h2 { font-size: var(--text-xl); font-weight: 600; }
.bug-count { font-size: var(--text-sm); color: var(--text-muted); }

.bug-content {
  flex: 1;
  display: flex;
  gap: 16px;
  overflow: hidden;
}

/* Bug List */
.bug-list {
  width: 320px;
  flex-shrink: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bug-card {
  padding: 10px 12px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.bug-card:hover { border-color: var(--border-active); background: var(--bg-hover); }

.bug-card.active {
  border-color: var(--primary);
  background: var(--bg-active);
}

.bug-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.bug-id {
  font-size: 10px;
  color: var(--text-muted);
}

.bug-card-title {
  font-size: var(--text-base);
  font-weight: 500;
  margin-bottom: 4px;
  line-height: 1.4;
}

.bug-card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: var(--text-muted);
}

/* Bug Detail */
.bug-detail {
  flex: 1;
  overflow-y: auto;
}
.bug-proofbar { min-height: 34px; display: flex; align-items: center; gap: 13px; margin-bottom: 13px; padding: 0 10px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-surface); }.bug-proofbar span { display: flex; align-items: center; gap: 5px; color: var(--text-muted); font-size: 9px; }.bug-proofbar i { width: 5px; height: 5px; border-radius: 50%; }.bug-proofbar i.fact { background: var(--info); }.bug-proofbar i.evidence { background: var(--success); }.bug-proofbar i.hypothesis { background: var(--agent); }.bug-proofbar b { margin-left: auto; color: var(--success); font-size: 8px; font-weight: 500; }

.bug-detail-header {
  margin-bottom: 16px;
}

.bug-detail-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bug-detail-id {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.bug-detail-header h3 {
  font-size: var(--text-lg);
  font-weight: 600;
}

.bug-detail-body { display: flex; flex-direction: column; gap: 16px; }

.bug-section { }

.bug-section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.hypothesis-note { margin-left: auto; color: var(--warning); font: 500 8px var(--font-mono); }

.bug-two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.bug-col-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.bug-col-value {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

.bug-col-value.danger { color: var(--danger); }

/* Root Cause Panel */
.root-cause-panel {
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-surface);
}

.rc-row {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rc-row:last-child { border-bottom: none; }

.rc-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rc-value {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.rc-confidence {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rc-confidence-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-base);
  border-radius: 2px;
  overflow: hidden;
}

.rc-confidence-fill {
  height: 100%;
  background: var(--agent);
  border-radius: 2px;
}

.bug-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  gap: 8px;
}
</style>
