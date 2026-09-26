<template>
  <div class="report-page">
    <div class="report-header">
      <div><span class="page-eyebrow">EVIDENCE REPORTS</span><h2>{{ t('reports.title') }}</h2></div>
      <div class="report-actions"><button>{{ t('reports.exportPdf') }}</button><button class="primary">{{ t('reports.share') }}</button></div>
    </div>

    <div class="report-content" v-if="reports.length > 0">
      <div v-for="report in reports" :key="report.id" class="report-card panel">
        <div class="report-card-header">
          <div class="report-card-title">
            <span class="report-id mono">R-{{ report.id }}</span>
            <h3>{{ t('reports.demoTitle') }}</h3>
          </div>
          <span class="report-time">{{ new Date(report.generatedAt).toLocaleString() }}</span>
        </div>

        <div class="report-stats">
          <div class="report-stat">
            <span class="report-stat-val pass">{{ report.passedTests || 0 }}</span>
            <span class="report-stat-label">{{ t('reports.passed') }}</span>
          </div>
          <div class="report-stat">
            <span class="report-stat-val fail">{{ report.failedTests || 0 }}</span>
            <span class="report-stat-label">{{ t('reports.failed') }}</span>
          </div>
          <div class="report-stat">
            <span class="report-stat-val">{{ report.totalTests || 0 }}</span>
            <span class="report-stat-label">{{ t('reports.totalTests') }}</span>
          </div>
          <div class="report-stat">
            <span class="report-stat-val" :class="getPassRateClass(report.passRate)">
              {{ report.passRate ? report.passRate.toFixed(1) + '%' : '—' }}
            </span>
            <span class="report-stat-label">{{ t('reports.passRate') }}</span>
          </div>
          <div class="report-stat">
            <span class="report-stat-val">{{ report.totalExecutionTimeMs ? (report.totalExecutionTimeMs / 1000).toFixed(1) + 's' : '—' }}</span>
            <span class="report-stat-label">{{ t('workspace.duration') }}</span>
          </div>
        </div>

        <div class="execution-strip"><span>{{ t('reports.execution') }}</span><div><i :style="{ width: (report.passRate || 0) + '%' }"></i></div><b class="mono">{{ report.passedTests || 0 }} / {{ report.totalTests || 0 }} {{ t('reports.passed') }}</b></div>

        <div class="report-summary" v-if="report.summary">
          <p>{{ t('reports.demoSummary') }}</p>
        </div>

        <div class="report-findings">
          <div><span>{{ t('reports.riskAssessment') }}</span><b class="risk-text">{{ t('reports.demoRisk') }}</b></div>
          <div><span>{{ t('reports.coverageGaps') }}</span><b>{{ t('reports.demoCoverageGap') }}</b></div>
          <div><span>{{ t('reports.evidenceBugs') }}</span><b>{{ t('reports.demoBugs') }}</b></div>
        </div>

        <div class="report-recommendations" v-if="report.aiRecommendations">
          <div class="report-rec-label">{{ t('reports.aiRecommendations') }}</div>
          <p>{{ t('reports.demoRecommendations') }}</p>
        </div>

        <div class="report-analysis-grid">
          <section>
            <header><span>{{ t('reports.keyFindings') }}</span><em class="mono">3 {{ t('reports.signals') }}</em></header>
            <div class="finding-row"><i class="danger"></i><div><b>{{ t('reports.findingValidation') }}</b><p>{{ t('reports.findingValidationDetail') }}</p></div><strong>HIGH</strong></div>
            <div class="finding-row"><i class="warning"></i><div><b>{{ t('reports.findingLockout') }}</b><p>{{ t('reports.findingLockoutDetail') }}</p></div><strong>MED</strong></div>
            <div class="finding-row"><i class="success"></i><div><b>{{ t('reports.findingStable') }}</b><p>{{ t('reports.findingStableDetail') }}</p></div><strong>PASS</strong></div>
          </section>
          <section>
            <header><span>{{ t('reports.coverageMap') }}</span><em class="mono">84% {{ t('reports.overall') }}</em></header>
            <div class="coverage-map-row"><span>{{ t('reports.authentication') }}</span><div><i style="width: 92%"></i></div><b class="mono">92%</b></div>
            <div class="coverage-map-row"><span>{{ t('reports.accountLockout') }}</span><div><i class="warning" style="width: 68%"></i></div><b class="mono">68%</b></div>
            <div class="coverage-map-row"><span>{{ t('reports.tokenLifecycle') }}</span><div><i style="width: 81%"></i></div><b class="mono">81%</b></div>
            <button class="trace-link" @click="router.push('/trace/102')">{{ t('reports.openEvidenceTrace') }} <span>→</span></button>
          </section>
        </div>
      </div>
    </div>

    <div v-else class="tc-empty" style="padding: 60px;">
      <p>No reports yet</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import { reportApi } from '@/api'
import type { TestReport } from '@/types'
import { demoReports } from '@/data/demo'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const store = useAppStore()
const { t } = useI18n()
const router = useRouter()
const reports = ref<TestReport[]>([])

function getPassRateClass(rate?: number) {
  if (!rate) return ''
  if (rate >= 80) return 'pass'
  if (rate >= 50) return 'warn'
  return 'fail'
}

async function load() {
  if (!store.currentProject) return
  if (store.isDemoMode) {
    reports.value = demoReports
    return
  }
  try {
    const res = await reportApi.list(store.currentProject.id)
    reports.value = res.data?.length ? res.data : demoReports
  } catch {
    reports.value = demoReports
  }
}

onMounted(load)
watch(() => store.currentProject, load)
</script>

<style scoped>
.report-page { padding: 16px 20px; height: 100%; overflow-y: auto; }

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.report-header h2 { font-size: var(--text-xl); font-weight: 600; }
.report-actions { display: flex; gap: 7px; }.report-actions button { height: 30px; padding: 0 10px; border: 1px solid var(--border); border-radius: 5px; background: var(--bg-surface); color: var(--text-secondary); font-size: 10px; cursor: pointer; }.report-actions button.primary { border-color: rgba(99,102,241,.42); background: var(--primary); color: #fff; }

.report-content { display: flex; flex-direction: column; gap: 12px; }

.report-card { padding: 16px; background: rgba(12,18,27,.82); }

.report-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.report-card-title { display: flex; flex-direction: column; gap: 4px; }
.report-id { font-size: 11px; color: var(--text-muted); }
.report-card-title h3 { font-size: var(--text-lg); font-weight: 600; }
.report-time { font-size: 11px; color: var(--text-muted); }

.report-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.report-stat {
  text-align: center;
}

.report-stat-val {
  display: block;
  font-size: var(--text-xl);
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

.report-stat-val.pass { color: var(--success); }
.report-stat-val.fail { color: var(--danger); }
.report-stat-val.warn { color: var(--warning); }

.report-stat-label {
  font-size: 9px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.execution-strip { display: grid; grid-template-columns: 74px minmax(0,1fr) 110px; align-items: center; gap: 10px; margin-bottom: 12px; padding: 8px 10px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-base); }.execution-strip > span { color: var(--text-muted); font: 600 8px var(--font-mono); }.execution-strip > div { height: 4px; overflow: hidden; border-radius: 2px; background: var(--bg-elevated); }.execution-strip i { display: block; height: 100%; background: var(--success); }.execution-strip b { color: var(--text-muted); font-size: 8px; font-weight: 500; text-align: right; }

.report-findings { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); margin: 12px 0; border: 1px solid var(--border-subtle); border-radius: var(--radius-md); }
.report-findings > div { min-height: 72px; display: grid; align-content: center; gap: 6px; padding: 12px; }
.report-findings > div + div { border-left: 1px solid var(--border-subtle); }
.report-findings span { color: var(--text-muted); font-size: 10px; text-transform: uppercase; letter-spacing: .04em; }
.report-findings b { color: var(--text-secondary); font-size: 12px; font-weight: 500; line-height: 1.5; }
.report-findings .risk-text { color: var(--warning); }
@media (max-width: 760px) { .report-findings { grid-template-columns: 1fr; } .report-findings > div + div { border-left: 0; border-top: 1px solid var(--border-subtle); } }
@media (max-width: 640px) {
  .report-page { padding: 12px; overflow-x: hidden; }
  .report-card { min-width: 0; padding: 12px; }
  .report-card-header { flex-wrap: wrap; gap: 6px 12px; }
  .report-time { width: 100%; }
  .report-stats { flex-wrap: wrap; gap: 12px 18px; }
  .report-summary, .report-recommendations, .report-findings { min-width: 0; overflow-wrap: anywhere; }
}

.report-summary {
  padding: 10px 12px;
  background: var(--bg-elevated);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}

.report-summary p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
}

.report-recommendations {
  padding: 10px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
}

.report-rec-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--agent);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.report-recommendations p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
}
.report-analysis-grid { display: grid; grid-template-columns: 1.15fr .85fr; gap: 12px; margin-top: 12px; }.report-analysis-grid section { overflow: hidden; border: 1px solid var(--border-subtle); border-radius: 6px; background: var(--bg-base); }.report-analysis-grid header { height: 34px; display: flex; align-items: center; justify-content: space-between; padding: 0 11px; border-bottom: 1px solid var(--border-subtle); }.report-analysis-grid header span { color: var(--text-secondary); font-size: 9px; font-weight: 600; letter-spacing: .04em; }.report-analysis-grid header em { color: var(--text-muted); font-size: 8px; font-style: normal; }.finding-row { min-height: 51px; display: grid; grid-template-columns: 6px minmax(0,1fr) auto; align-items: center; gap: 9px; padding: 7px 11px; border-bottom: 1px solid var(--border-subtle); }.finding-row:last-child { border-bottom: 0; }.finding-row > i { width: 5px; height: 5px; border-radius: 50%; background: var(--info); }.finding-row > i.danger { background: var(--danger); }.finding-row > i.warning { background: var(--warning); }.finding-row > i.success { background: var(--success); }.finding-row div { display: grid; gap: 2px; }.finding-row b { color: var(--text-secondary); font-size: 10px; font-weight: 500; }.finding-row p { color: var(--text-muted); font-size: 9px; }.finding-row strong { color: var(--text-muted); font: 600 8px var(--font-mono); }.coverage-map-row { display: grid; grid-template-columns: 100px minmax(0,1fr) 34px; align-items: center; gap: 8px; padding: 9px 11px 0; }.coverage-map-row span { color: var(--text-secondary); font-size: 9px; }.coverage-map-row > div { height: 4px; overflow: hidden; border-radius: 2px; background: var(--bg-elevated); }.coverage-map-row i { display: block; height: 100%; background: var(--success); }.coverage-map-row i.warning { background: var(--warning); }.coverage-map-row b { color: var(--text-muted); font-size: 8px; text-align: right; }.trace-link { width: calc(100% - 22px); height: 29px; display: flex; align-items: center; justify-content: space-between; margin: 13px 11px 11px; padding: 0 9px; border: 1px solid rgba(99,102,241,.28); border-radius: 4px; background: rgba(99,102,241,.06); color: var(--text-secondary); font-size: 9px; cursor: pointer; }.trace-link span { color: var(--primary); }
@media (max-width: 980px) { .report-analysis-grid { grid-template-columns: 1fr; } }
</style>
