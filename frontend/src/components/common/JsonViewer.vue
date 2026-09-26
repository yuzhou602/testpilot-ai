<template>
  <div class="json-viewer">
    <button v-if="collapsible && !expanded" class="json-collapsed" @click="expanded = true">
      <span class="json-toggle">▶</span>
      <span class="json-preview mono">{{ preview }}</span>
    </button>
    <div v-else>
      <button v-if="collapsible" class="json-toggle-row" aria-label="Collapse JSON" @click="expanded = false">
        <span class="json-toggle">▼</span>
      </button>
      <pre class="code-block" v-html="highlighted"></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = withDefaults(defineProps<{
  data: any
  collapsible?: boolean
}>(), {
  collapsible: false
})

const expanded = ref(!props.collapsible)

const formatted = computed(() => {
  if (typeof props.data === 'string') {
    try { return JSON.stringify(JSON.parse(props.data), null, 2) }
    catch { return props.data }
  }
  return JSON.stringify(props.data, null, 2)
})

const preview = computed(() => {
  const s = formatted.value
  return s.length > 80 ? s.substring(0, 80) + '...' : s
})

const highlighted = computed(() => {
  return formatted.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"([^"]+)":/g, '<span class="json-key">"$1"</span>:')
    .replace(/: "([^"]*)"/g, ': <span class="json-string">"$1"</span>')
    .replace(/: (\d+)/g, ': <span class="json-number">$1</span>')
    .replace(/: (true|false)/g, ': <span class="json-bool">$1</span>')
    .replace(/: (null)/g, ': <span class="json-null">$1</span>')
})
</script>

<style scoped>
.json-viewer { font-family: var(--font-mono); }
.json-collapsed {
  width: 100%;
  display: flex; align-items: center; gap: 6px;
  padding: 6px 8px; background: var(--bg-base); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md); cursor: pointer; font-size: var(--text-sm);
  color: inherit; text-align: left;
}
.json-collapsed:hover { border-color: var(--border-active); }
.json-toggle { color: var(--text-muted); font-size: 10px; }
.json-preview { color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.json-toggle-row { width: 100%; padding: 4px 0; border: 0; background: transparent; text-align: left; cursor: pointer; }
.json-toggle-row .json-toggle { color: var(--text-muted); font-size: 10px; }
.json-toggle-row:hover .json-toggle { color: var(--text-secondary); }
:deep(.json-key) { color: #93C5FD; }
:deep(.json-string) { color: var(--success); }
:deep(.json-number) { color: var(--warning); }
:deep(.json-bool) { color: var(--agent); }
:deep(.json-null) { color: var(--text-muted); }
</style>
