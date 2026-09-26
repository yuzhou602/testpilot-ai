<template>
  <div class="agent-plan">
    <div v-for="(step, i) in steps" :key="i" class="plan-step" :class="getStepClass(i)">
      <div class="step-indicator">
        <svg v-if="getStepState(i) === 'done'" width="12" height="12" viewBox="0 0 12 12">
          <path d="M2 6l3 3 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
        <svg v-else-if="getStepState(i) === 'failed'" width="12" height="12" viewBox="0 0 12 12">
          <path d="M3 3l6 6M9 3L3 9" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
        <div v-else-if="getStepState(i) === 'running'" class="step-spinner"></div>
        <span v-else class="step-num">{{ i + 1 }}</span>
      </div>
      <div class="step-line" v-if="i < steps.length - 1"></div>
      <div class="step-content">
        <span class="step-name">{{ step.stepName || step.name }}</span>
        <span v-if="step.tool" class="step-tool mono">
          <span class="step-tool-icon">TOOL</span> {{ step.tool }}
        </span>
      </div>
      <div class="step-status">
        <span v-if="getStepState(i) === 'done'" class="state-done">Done</span>
        <span v-else-if="getStepState(i) === 'failed'" class="state-failed">Failed</span>
        <span v-else-if="getStepState(i) === 'running'" class="state-running">Running</span>
        <span v-else class="state-pending">Pending</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AgentStep } from '@/types'

const props = defineProps<{
  steps: any[]
  currentStepIndex: number
  executionSteps?: AgentStep[]
}>()

function getStepState(i: number): string {
  const execution = props.executionSteps?.find(step => step.stepIndex === i)
  if (execution?.status === 'FAILED') return 'failed'
  if (execution?.status === 'COMPLETED') return 'done'
  if (execution?.status === 'RUNNING') return 'running'
  if (i < props.currentStepIndex) return 'done'
  if (i === props.currentStepIndex) return 'running'
  return 'pending'
}

function getStepClass(i: number) {
  return `step-${getStepState(i)}`
}
</script>

<style scoped>
.agent-plan {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.plan-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  position: relative;
  min-height: 29px;
  padding: 3px 8px 3px 2px;
  border-radius: 5px;
  transition: background var(--duration-fast), transform var(--duration-fast);
}
.plan-step:hover { background: rgba(148,163,184,.035); }
.plan-step.step-running { background: linear-gradient(90deg, rgba(59,130,246,.1), transparent 72%); }
.plan-step.step-failed { background: linear-gradient(90deg, rgba(239,68,68,.075), transparent 72%); }

.step-indicator {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 600;
  background: var(--bg-overlay);
  border: 1px solid var(--border);
  color: var(--text-muted);
  z-index: 1;
  transition: all var(--duration-fast);
}

.step-done .step-indicator {
  background: rgba(34,197,94,.13);
  border-color: rgba(34,197,94,.48);
  color: var(--success);
}

.step-running .step-indicator {
  background: rgba(59,130,246,.16);
  border-color: var(--info);
  color: white;
  box-shadow: 0 0 0 4px rgba(59,130,246,.06);
}
.step-failed .step-indicator { border-color: rgba(239,68,68,.62); background: rgba(239,68,68,.14); color: var(--danger); }

.step-line {
  position: absolute;
  left: 12px;
  top: 26px;
  width: 1px;
  height: calc(100% - 2px);
  background: var(--border);
}

.step-done .step-line { background: rgba(34,197,94,.42); }
.step-failed .step-line { background: var(--border); }

.step-spinner {
  width: 10px;
  height: 10px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.step-content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 1px 0;
}

.step-name {
  font-size: 12px;
  color: var(--text-primary);
  display: block;
  min-width: 0;
  line-height: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.step-done .step-name { color: #98a4b5; }
.step-running .step-name { color: var(--text-primary); font-weight: 600; }

.step-tool {
  flex: none;
  font-size: 9px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 0;
}

.step-tool-icon { padding: 1px 4px; border: 1px solid var(--border-subtle); border-radius: 3px; color: #8795a9; font-size: 8px; letter-spacing: .05em; }

.step-status {
  flex-shrink: 0;
  padding-top: 2px;
}

.state-done { font-size: var(--text-xs); color: var(--success); font-weight: 500; }
.state-running { font-size: var(--text-xs); color: var(--info); font-weight: 500; }
.state-failed { font-size: var(--text-xs); color: var(--danger); font-weight: 500; }
.state-pending { font-size: var(--text-xs); color: var(--text-muted); }
</style>
