<template>
  <div class="tool-card" :class="{ expanded: isOpen }">
    <button class="tool-card-header" :aria-expanded="isOpen" @click="isOpen = !isOpen">
      <div class="tool-card-left">
        <span class="tool-icon" :class="`tool-${type}`">{{ icon }}</span>
        <span class="tool-name mono">{{ name }}</span>
        <span class="tool-detail">{{ detail }}</span>
      </div>
      <div class="tool-card-right">
        <span v-if="latency" class="tool-latency mono">{{ latency }}ms</span>
        <StatusBadge v-if="status" :status="status" />
        <span class="tool-toggle">{{ isOpen ? '▼' : '▶' }}</span>
      </div>
    </button>
    <div v-if="isOpen" class="tool-card-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

withDefaults(defineProps<{
  name: string
  detail?: string
  type?: string
  latency?: number | string
  status?: string
}>(), {
  type: 'http',
  detail: '',
})

const isOpen = ref(false)

const icon = computed(() => {
  // Will be set via CSS
  return ''
})

import { computed } from 'vue'
</script>

<style scoped>
.tool-card {
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: border-color var(--duration-fast);
}
.tool-card:hover { border-color: var(--border-active); }
.tool-card.expanded { border-color: var(--border-active); }

.tool-card-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  background: var(--bg-elevated);
  border: 0;
  color: inherit;
  text-align: left;
}

.tool-card-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.tool-icon {
  width: 20px;
  height: 20px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}

.tool-http { background: rgba(59,130,246,0.12); color: var(--info); }
.tool-browser { background: rgba(139,92,246,0.12); color: var(--agent); }
.tool-sql { background: rgba(34,197,94,0.12); color: var(--success); }
.tool-log { background: rgba(245,158,11,0.12); color: var(--warning); }
.tool-assert { background: rgba(239,68,68,0.12); color: var(--danger); }
.tool-agent { background: rgba(139,92,246,0.12); color: var(--agent); }

.tool-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
}

.tool-detail {
  font-size: var(--text-sm);
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-card-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.tool-latency {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.tool-toggle {
  font-size: 10px;
  color: var(--text-muted);
}

.tool-card-body {
  padding: 0 12px 12px;
  border-top: 1px solid var(--border-subtle);
}
</style>
