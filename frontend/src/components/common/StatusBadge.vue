<template>
  <span class="badge" :class="statusClass">
    <span class="badge-dot"></span>
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
}>()

const statusClass = computed(() => {
  const map: Record<string, string> = {
    COMPLETED: 'badge-pass', PASSED: 'badge-pass', PASS: 'badge-pass',
    FAILED: 'badge-fail', FAIL: 'badge-fail', ERROR: 'badge-fail',
    RUNNING: 'badge-running', ANALYZING: 'badge-running', PLANNING: 'badge-running',
    WAITING_TOOL: 'badge-running', WAITING_USER: 'badge-error', REPLANNING: 'badge-running',
    ANALYZING_FAILURE: 'badge-error',
    CREATED: 'badge-pending', READY: 'badge-pending', PENDING: 'badge-pending', SKIPPED: 'badge-pending',
    CANCELLED: 'badge-pending',
  }
  return map[props.status] || 'badge-pending'
})

const label = computed(() => {
  const map: Record<string, string> = {
    COMPLETED: 'DONE', PASSED: 'PASS', PASS: 'PASS',
    FAILED: 'FAIL', FAIL: 'FAIL', ERROR: 'ERROR',
    RUNNING: 'RUNNING', ANALYZING: 'ANALYZING', PLANNING: 'PLANNING',
    WAITING_TOOL: 'WAITING', WAITING_USER: 'WAITING USER',
    REPLANNING: 'REPLANNING', ANALYZING_FAILURE: 'ANALYZING',
    CREATED: 'CREATED', READY: 'READY', PENDING: 'PENDING',
    SKIPPED: 'SKIPPED', CANCELLED: 'CANCELLED',
  }
  return map[props.status] || props.status
})
</script>
