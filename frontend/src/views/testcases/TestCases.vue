<template>
  <div class="tc-page">
    <div class="tc-header">
      <div><span class="page-eyebrow">EXECUTABLE SPECIFICATIONS</span><h2>{{ t('testCases.title') }}</h2></div>
      <div class="tc-header-actions">
        <el-input v-model="search" :placeholder="t('testCases.searchCases')" size="small" class="tc-search" clearable />
        <el-button size="small" :disabled="selectedIds.length === 0" @click="runSelected">{{ t('testCases.runSelected') }} · {{ selectedIds.length }}</el-button>
        <el-button size="small" type="primary" @click="generateCases">
          <el-icon><MagicStick /></el-icon> {{ t('testCases.aiGenerate') }}
        </el-button>
      </div>
    </div>

    <div class="tc-table-wrap panel">
      <div class="panel-header">
        <span>{{ filteredCases.length }} {{ t('testCases.cases') }}</span>
        <span class="tc-coverage-note mono">AUTH · 84% {{ t('testCases.coverage') }}</span>
      </div>
      <table class="tc-table">
        <thead>
          <tr>
            <th style="width: 38px;"><input type="checkbox" :checked="allSelected" @change="toggleAll" /></th>
            <th style="width: 90px;">ID</th>
            <th>{{ t('testCases.scenario') }}</th>
            <th style="width: 92px;">{{ t('testCases.requirement') }}</th>
            <th style="width: 82px;">{{ t('testCases.testType') }}</th>
            <th style="width: 64px;">{{ t('testCases.risk') }}</th>
            <th style="width: 74px;">{{ t('testCases.automation') }}</th>
            <th style="width: 90px;">{{ t('testCases.lastResult') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tc in filteredCases" :key="tc.id" class="tc-row">
            <td><input v-model="selectedIds" type="checkbox" :value="tc.id" /></td>
            <td class="mono" style="font-size: 11px; color: var(--primary);">{{ tc.caseCode }}</td>
            <td>
              <div class="tc-title">{{ caseTitle(tc) }}</div>
              <div v-if="tc.description" class="tc-desc">{{ caseDescription(tc) }}</div>
            </td>
            <td><span class="tc-requirement mono">{{ requirementCode(tc.caseCode) }}</span></td>
            <td><span class="tc-type">{{ typeLabel(tc.testType) }}</span></td>
            <td><span class="tc-risk" :class="tc.priority === 0 ? 'high' : 'medium'">{{ tc.priority === 0 ? 'HIGH' : 'MED' }}</span></td>
            <td><span class="tc-auto" :class="{ enabled: tc.automated }">{{ tc.automated ? 'AUTO' : 'MANUAL' }}</span></td>
            <td><span class="tc-result" :class="tc.caseCode === 'TC-104' ? 'fail' : 'pass'"><i></i>{{ tc.caseCode === 'TC-104' ? 'FAIL' : 'PASS' }}</span></td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredCases.length === 0" class="tc-empty">
        <p>No test cases found</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import { testCaseApi } from '@/api'
import type { TestCase } from '@/types'
import { demoTestCases } from '@/data/demo'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const store = useAppStore()
const { t } = useI18n()
const router = useRouter()
const cases = ref<TestCase[]>([])
const search = ref('')
const selectedIds = ref<number[]>([])
function toggleAll() { selectedIds.value = allSelected.value ? [] : filteredCases.value.map(tc => tc.id) }
async function runSelected() { ElMessage.success(t('testCases.selectedStarted', { count: selectedIds.value.length })); await router.push('/workspace') }
function caseTitle(tc: TestCase) { const index = demoTestCases.findIndex(item => item.id === tc.id); return index >= 0 ? t(`testCases.demoTitle${index + 1}`) : tc.title }
function caseDescription(tc: TestCase) { const index = demoTestCases.findIndex(item => item.id === tc.id); return index >= 0 ? t(`testCases.demoDesc${index + 1}`) : truncate(tc.description || '', 80) }
function typeLabel(type?: string) { return type ? t(`testCases.type${type.charAt(0).toUpperCase() + type.slice(1).toLowerCase()}`) : '—' }
function requirementCode(caseCode: string) { return ({ 'TC-101': 'R1', 'TC-102': 'R2', 'TC-103': 'R3', 'TC-104': 'R1', 'TC-105': 'R1' } as Record<string,string>)[caseCode] || '—' }

function generateCases() {
  if (cases.value.some(testCase => testCase.caseCode === 'TC-AI-109')) {
    ElMessage.info(t('testCases.alreadyIncluded'))
    return
  }
  cases.value = [
    ...cases.value,
    {
      id: 109,
      projectId: store.currentProject?.id || 1,
      scenarioId: 5,
      caseCode: 'TC-AI-109',
      title: '账户锁定后 30 分钟边界恢复',
      description: '验证 29、30、31 分钟三个时间边界上的锁定状态。',
      testType: 'BOUNDARY',
      strategy: 'BOUNDARY_VALUE',
      priority: 0,
      aiGenerated: true,
      automated: true,
    },
  ]
  ElMessage.success(t('testCases.generatedFromGap'))
}

const filteredCases = computed(() => {
  if (!search.value) return cases.value
  const q = search.value.toLowerCase()
  return cases.value.filter(c =>
    c.caseCode.toLowerCase().includes(q) ||
    c.title.toLowerCase().includes(q) ||
    (c.testType || '').toLowerCase().includes(q)
  )
})
const allSelected = computed(() => filteredCases.value.length > 0 && filteredCases.value.every(tc => selectedIds.value.includes(tc.id)))

function formatStrategy(s?: string) {
  if (!s) return '—'
  return s.replace(/_/g, ' ').toLowerCase()
}

function truncate(s: string, len: number) {
  return s && s.length > len ? s.substring(0, len) + '...' : s
}

async function load() {
  if (!store.currentProject) return
  if (store.isDemoMode) {
    cases.value = demoTestCases
    return
  }
  try {
    const res = await testCaseApi.listByProject(store.currentProject.id)
    cases.value = res.data?.length ? res.data : demoTestCases
  } catch {
    cases.value = demoTestCases
  }
}

onMounted(load)
watch(() => store.currentProject, load)
</script>

<style scoped>
.tc-page { padding: 16px 20px; height: 100%; display: flex; flex-direction: column; }

.tc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.tc-header h2 { font-size: var(--text-xl); font-weight: 600; }

.tc-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tc-search { width: 200px; }

.tc-table-wrap {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.tc-table {
  width: 100%;
  border-collapse: collapse;
  flex: none;
}

.tc-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.tc-table th {
  padding: 8px 12px;
  text-align: left;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border-subtle);
}

.tc-table td {
  height: 62px;
  padding: 7px 12px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
}

.tc-row { transition: background var(--duration-fast); }
.tc-row:hover { background: var(--bg-hover); }

.tc-title { color: var(--text-primary); font-weight: 500; }
.tc-desc { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
.tc-type { font-size: 11px; color: var(--text-muted); }
.tc-priority { font-size: 12px; color: var(--text-secondary); }
.tc-coverage-note { color: var(--text-muted); font-size: 9px; }
.tc-table input[type="checkbox"] { width: 13px; height: 13px; accent-color: var(--primary); }
.tc-requirement { color: var(--text-secondary); font-size: 10px; }
.tc-risk { padding: 2px 5px; border-radius: 3px; color: var(--warning); background: rgba(245,158,11,.08); font: 600 8px var(--font-mono); }
.tc-risk.high { color: var(--danger); background: rgba(239,68,68,.08); }
.tc-auto { color: var(--text-muted); font: 600 8px var(--font-mono); }
.tc-auto.enabled { color: var(--info); }
.tc-result { display: inline-flex; align-items: center; gap: 5px; color: var(--success); font: 600 9px var(--font-mono); }
.tc-result i { width: 5px; height: 5px; border-radius: 50%; background: currentColor; }
.tc-result.fail { color: var(--danger); }

.tc-empty {
  padding: 40px;
  text-align: center;
  color: var(--text-muted);
}
</style>
