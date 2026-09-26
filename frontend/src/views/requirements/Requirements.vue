<template>
  <div class="req-page">
    <!-- Left: Requirement Tree -->
    <div class="req-tree">
      <div class="panel-header">
        <span>{{ t('requirements.title') }}</span>
      </div>
      <div class="req-tree-body">
        <div v-if="requirements.length === 0" class="ws-empty-tasks">
          <span style="font-size: 11px;">{{ t('requirements.noRequirements') }}</span>
        </div>
        <div v-for="req in requirements" :key="req.id"
             class="req-tree-item" :class="{ active: selectedReq?.id === req.id }"
             @click="selectReq(req)">
          <div class="req-tree-title">{{ requirementTitle(req) }}</div>
          <div class="req-tree-meta">
            <RiskBadge :level="req.riskLevel" />
            <span class="req-coverage mono">{{ req.coverageScore }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Center: Requirement Detail -->
    <div class="req-detail">
      <template v-if="selectedReq">
        <div class="req-context-bar">
          <div><span>{{ t('requirements.review') }}</span><b class="mono">AUTH · {{ selectedReq.id }}</b></div>
          <div class="req-context-stats"><span><i class="covered"></i>{{ rules.filter(rule => rule.covered).length }} {{ t('requirements.covered') }}</span><span><i class="gap"></i>{{ uncoveredRules.length }} {{ t('requirements.gap') }}</span></div>
        </div>
        <div class="req-detail-header">
          <h3>{{ requirementTitle(selectedReq) }}</h3>
          <RiskBadge :level="selectedReq.riskLevel" />
        </div>
        <div class="req-detail-desc">{{ selectedReq.id === 2 ? t('requirements.demoDescription') : (selectedReq.description || '—') }}</div>

        <!-- Rules -->
        <div class="req-rules panel">
          <div class="panel-header"><span>{{ t('requirements.businessRules') }}</span></div>
          <div class="req-rules-body">
            <div v-for="rule in rules" :key="rule.id" class="req-rule" :class="{ covered: rule.covered }">
              <div class="req-rule-check">
                <svg v-if="rule.covered" width="14" height="14" viewBox="0 0 12 12"><path d="M2 6l3 3 5-5" stroke="var(--success)" stroke-width="2" fill="none" stroke-linecap="round"/></svg>
                <span v-else class="req-rule-unchecked">○</span>
              </div>
              <div class="req-rule-content">
                <span class="req-rule-code mono">{{ rule.ruleCode }}</span>
                <span class="req-rule-desc">{{ ruleDescription(rule) }}</span>
              </div>
              <span class="req-rule-count mono">{{ rule.coverageCount }} {{ t('requirements.tests') }}</span>
            </div>
          </div>
        </div>

        <!-- AI Recommendation -->
        <div class="req-ai-rec panel">
          <div class="panel-header"><span>{{ t('requirements.aiCoverage') }}</span><em class="mono">{{ t('requirements.evidenceRequired') }}</em></div>
          <div class="panel-body">
            <p style="font-size: 13px; color: var(--text-secondary); line-height: 1.6;">
              <template v-if="uncoveredRules.length">{{ t('requirements.missingCoverage') }}：{{ uncoveredRules.map(rule => ruleDescription(rule)).join(' · ') }}。{{ t('requirements.generateBeforeRelease') }}</template>
              <template v-else>{{ t('requirements.fullyCovered') }}</template>
            </p>
            <div v-if="uncoveredRules.length" class="boundary-cases"><span>29 min</span><span>30 min</span><span>31 min</span><small>{{ t('requirements.boundaryProposed') }}</small></div>
            <button v-if="uncoveredRules.length" class="req-gen-btn" @click="generateMissingTests">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10"/></svg>
              {{ t('requirements.generateMissing') }}
            </button>
          </div>
        </div>
      </template>
      <div v-else class="req-empty">
        <div class="empty-state-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <p>Select a requirement</p>
      </div>
    </div>

    <!-- Right: Coverage -->
    <div class="req-coverage">
      <div class="panel-header"><span>{{ t('requirements.coverage') }}</span></div>
      <div class="req-coverage-body">
        <div class="coverage-overall">
          <div><span>{{ t('requirements.overallCoverage') }}</span><b class="coverage-big mono">{{ overallCoverage }}%</b></div>
          <div class="coverage-track"><i :style="{ width: overallCoverage + '%' }"></i></div>
          <small>{{ uncoveredRules.length }} {{ t('requirements.rulesNeedEvidence') }}</small>
        </div>
        <div class="coverage-list">
          <div v-for="rule in rules" :key="rule.id" class="coverage-rule">
            <span class="coverage-rule-code mono">{{ rule.ruleCode }}</span>
            <div class="coverage-bar-mini">
              <div class="coverage-fill-mini" :style="{ width: (rule.covered ? '100' : '0') + '%', background: rule.covered ? 'var(--success)' : 'var(--danger)' }"></div>
            </div>
            <span class="coverage-rule-pct mono">{{ rule.covered ? '100' : '0' }}%</span>
          </div>
        </div>
        <div class="coverage-risk"><span>{{ t('requirements.releaseSignal') }}</span><b>{{ t('requirements.mediumRisk') }}</b><p>{{ t('requirements.unlockUnverified') }}</p></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import { requirementApi } from '@/api'
import type { Requirement, RequirementRule } from '@/types'
import RiskBadge from '@/components/common/RiskBadge.vue'
import { demoRequirements } from '@/data/demo'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { useI18n } from 'vue-i18n'

const store = useAppStore()
const { t } = useI18n()
const requirements = ref<Requirement[]>([])
const selectedReq = ref<Requirement | null>(null)
const rules = ref<RequirementRule[]>([])
function requirementTitle(req: Requirement) { return demoRequirements.some(item => item.id === req.id) ? t(`requirements.demoTitle${req.id}`) : req.title }
function ruleDescription(rule: RequirementRule) { return rule.ruleCode === 'R3' ? t('requirements.ruleR3') : rule.ruleCode === 'R4' ? t('requirements.ruleR4') : rule.ruleDescription }

const overallCoverage = computed(() => {
  if (requirements.value.length === 0) return 0
  const sum = requirements.value.reduce((a, r) => a + r.coverageScore, 0)
  return Math.round(sum / requirements.value.length)
})
const uncoveredRules = computed(() => rules.value.filter(rule => !rule.covered))

async function loadRequirements() {
  if (!store.currentProject) return
  if (store.isDemoMode) {
    requirements.value = demoRequirements
    if (!selectedReq.value) selectReq(requirements.value.find(req => req.coverageScore < 100) || requirements.value[0])
    return
  }
  try {
    const res = await requirementApi.list(store.currentProject.id)
    requirements.value = res.data?.length ? res.data : demoRequirements
  } catch {
    requirements.value = demoRequirements
  }
  if (requirements.value.length > 0 && !selectedReq.value) {
    selectReq(requirements.value.find(req => req.coverageScore < 100) || requirements.value[0])
  }
}

async function selectReq(req: Requirement) {
  selectedReq.value = req
  if (store.isDemoMode || req.rules?.length) {
    rules.value = req.rules || []
    return
  }
  try {
    const res = await requirementApi.get(req.id)
    rules.value = res.data?.rules || []
  } catch { rules.value = [] }
}

function generateMissingTests() {
  const uncovered = rules.value.filter(rule => !rule.covered)
  if (!uncovered.length) {
    ElMessage.success(t('requirements.coverageComplete'))
    return
  }
  uncovered.forEach(rule => {
    rule.covered = true
    rule.coverageCount = Math.max(rule.coverageCount, 3)
  })
  if (selectedReq.value) selectedReq.value.coverageScore = 100
  ElMessage.success(t('requirements.generatedBoundary', { count: uncovered.length * 3 }))
}

onMounted(loadRequirements)
watch(() => store.currentProject, loadRequirements)
</script>

<style scoped>
.req-page {
  display: flex;
  height: 100%;
  overflow: hidden;
}

/* Tree */
.req-tree {
  width: 260px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: var(--bg-surface);
}

.req-tree-body {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.req-tree-item {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--duration-fast);
}

.req-tree-item:hover { background: var(--bg-hover); }

.req-tree-item.active {
  background: var(--bg-active);
  border-color: var(--border);
}

.req-tree-title {
  font-size: var(--text-base);
  font-weight: 500;
  margin-bottom: 4px;
}

.req-tree-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.req-coverage {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

/* Detail */
.req-detail {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

.req-context-bar { height: 45px; display: flex; align-items: center; justify-content: space-between; margin: 0 -20px 16px; padding: 0 20px; border-bottom: 1px solid var(--border-subtle); background: rgba(12,18,27,.72); }
.req-context-bar > div:first-child { display: flex; align-items: center; gap: 10px; }
.req-context-bar span { color: var(--text-muted); font-size: 9px; letter-spacing: .05em; }
.req-context-bar b { color: var(--text-secondary); font-size: 9px; font-weight: 500; }
.req-context-stats { display: flex; gap: 13px; }
.req-context-stats span { display: flex; align-items: center; gap: 5px; text-transform: uppercase; }
.req-context-stats i { width: 5px; height: 5px; border-radius: 50%; }.req-context-stats i.covered { background: var(--success); }.req-context-stats i.gap { background: var(--warning); }

.req-detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.req-detail-header h3 {
  font-size: var(--text-xl);
  font-weight: 600;
}

.req-detail-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
}

.req-rules { margin-bottom: 16px; }

.req-rules-body { padding: 8px 12px; }

.req-rule {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.req-rule:last-child { border-bottom: none; }

.req-rule-check {
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.req-rule-unchecked {
  color: var(--text-muted);
  font-size: 14px;
}

.req-rule-content {
  flex: 1;
  display: flex;
  gap: 8px;
}

.req-rule-code {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--primary);
  flex-shrink: 0;
}

.req-rule-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.req-rule-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
  flex-shrink: 0;
}

.req-ai-rec .panel-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.req-ai-rec .panel-header em { color: var(--warning); font: 500 8px var(--font-mono); font-style: normal; }
.boundary-cases { display: flex; align-items: center; gap: 6px; }
.boundary-cases span { padding: 4px 7px; border: 1px solid var(--border); border-radius: 4px; background: var(--bg-base); color: var(--text-secondary); font: 500 9px var(--font-mono); }
.boundary-cases small { margin-left: 4px; color: var(--text-muted); font-size: 9px; }

.req-gen-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--agent);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  width: fit-content;
}

.req-gen-btn:hover {
  background: var(--bg-hover);
}

.req-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  gap: 8px;
}

/* Coverage Panel */
.req-coverage {
  width: 220px;
  border-left: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: var(--bg-surface);
  overflow-y: auto;
}

.req-coverage-body {
  padding: 16px;
}

.coverage-overall {
  display: grid;
  gap: 9px;
  padding: 12px;
  margin-bottom: 16px;
  background: var(--bg-base);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
}
.coverage-overall > div:first-child { display: flex; align-items: baseline; justify-content: space-between; }
.coverage-overall span { color: var(--text-muted); font-size: 9px; text-transform: uppercase; letter-spacing: .04em; }
.coverage-overall small { color: var(--text-muted); font-size: 9px; line-height: 1.4; }
.coverage-track { height: 4px; overflow: hidden; border-radius: 2px; background: var(--bg-elevated); }.coverage-track i { display: block; height: 100%; background: var(--primary); }

.coverage-big {
  font-size: 20px;
  font-weight: 650;
  color: var(--primary);
}

.coverage-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-top: 4px;
}

.coverage-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coverage-rule {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coverage-rule-code {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
  width: 28px;
  flex-shrink: 0;
}

.coverage-bar-mini {
  flex: 1;
  height: 4px;
  background: var(--bg-base);
  border-radius: 2px;
  overflow: hidden;
}

.coverage-fill-mini {
  height: 100%;
  border-radius: 2px;
  transition: width var(--duration-slow);
}

.coverage-rule-pct {
  font-size: var(--text-xs);
  color: var(--text-muted);
  width: 32px;
  text-align: right;
  flex-shrink: 0;
}
.coverage-risk { display: grid; gap: 4px; margin-top: 18px; padding-top: 14px; border-top: 1px solid var(--border-subtle); }.coverage-risk span { color: var(--text-muted); font: 600 8px var(--font-mono); }.coverage-risk b { color: var(--warning); font-size: 11px; }.coverage-risk p { color: var(--text-muted); font-size: 9px; line-height: 1.45; }
@media (max-width: 1100px) { .req-coverage { width: 190px; } .req-tree { width: 230px; } }
</style>
