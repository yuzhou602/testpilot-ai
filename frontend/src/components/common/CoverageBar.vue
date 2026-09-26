<template>
  <div class="coverage-bar">
    <div class="coverage-info">
      <span class="coverage-label">{{ label }}</span>
      <span class="coverage-value mono">{{ percentage }}%</span>
    </div>
    <div class="coverage-track">
      <div class="coverage-fill" :style="{ width: percentage + '%', background: fillColor }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  percentage: number
  label?: string
}>(), {
  label: 'Coverage'
})

const fillColor = computed(() => {
  if (props.percentage >= 80) return 'var(--success)'
  if (props.percentage >= 50) return 'var(--warning)'
  return 'var(--danger)'
})
</script>

<style scoped>
.coverage-bar { width: 100%; }
.coverage-info {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 4px;
}
.coverage-label { font-size: var(--text-sm); color: var(--text-secondary); }
.coverage-value { font-size: var(--text-sm); font-weight: 600; }
.coverage-track {
  height: 4px; background: var(--bg-base); border-radius: 2px; overflow: hidden;
}
.coverage-fill {
  height: 100%; border-radius: 2px; transition: width var(--duration-slow) ease-out;
}
</style>
