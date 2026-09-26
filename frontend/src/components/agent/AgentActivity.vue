<template>
  <div class="activity-stream" ref="feedRef">
    <div v-for="(event, i) in events" :key="i" class="activity-item" :class="getItemClass(event.type)">
      <span class="activity-time mono">{{ formatTime(event.timestamp) }}</span>
      <div class="activity-badge" :class="`badge-${getBadgeClass(event.type)}`">
        <span class="badge-dot"></span>
      </div>
      <div class="activity-content">
        <div class="activity-header">
          <span class="activity-type mono">{{ formatType(event.type) }}</span>
          <span v-if="event.data?.tool" class="activity-tool mono">{{ event.data.tool }}</span>
        </div>
        <div class="activity-detail">{{ formatDetail(event) }}</div>
        <div v-if="event.data?.latencyMs || event.data?.latency" class="activity-latency mono">{{ event.data.latencyMs || event.data.latency }}ms</div>
      </div>
    </div>

    <div v-if="isRunning" class="activity-item activity-thinking">
      <span class="activity-time mono">--:--</span>
      <div class="activity-badge badge-running"><span class="badge-dot"></span></div>
      <div class="activity-content">
        <span class="activity-type mono">THINKING</span>
        <span class="agent-thinking"><span></span><span></span><span></span></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{
  events: any[]
  isRunning?: boolean
}>()

const feedRef = ref<HTMLElement>()

function formatTime(ts: number) {
  return new Date(ts).toLocaleTimeString('en-US', { hour12: false })
}

function formatType(type: string) {
  return type.replace(/_/g, ' ')
}

function getBadgeClass(type: string) {
  if (type.includes('FAILED') || type.includes('FAILURE') || type.includes('ERROR')) return 'fail'
  if (type.includes('COMPLETED') || type.includes('RESULT') || type.includes('PASS')) return 'pass'
  if (type.includes('STARTED') || type.includes('RUNNING') || type.includes('TOOL')) return 'running'
  return 'info'
}

function getItemClass(type: string) {
  if (type.includes('FAILED') || type.includes('ERROR')) return 'item-error'
  if (type.includes('COMPLETED') || type.includes('PASS')) return 'item-success'
  return ''
}

function formatDetail(event: any) {
  const d = event.data
  if (!d) return ''
  if (d.summary) return d.summary
  if (d.detail) return d.path ? `${d.method || ''} ${d.path} · ${d.detail}`.trim() : d.detail
  if (d.error) return d.error
  if (d.stepName) return d.stepName
  if (d.tool && d.input) return `${d.tool}()`
  return ''
}

watch(() => props.events.length, async () => {
  await nextTick()
  if (feedRef.value) {
    feedRef.value.scrollTop = feedRef.value.scrollHeight
  }
})
</script>

<style scoped>
.activity-stream {
  flex: 1;
  overflow-y: auto;
  padding: 6px 8px 10px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  display: grid;
  grid-template-columns: 58px 8px minmax(0,1fr);
  gap: 10px;
  min-height: 38px;
  padding: 7px 8px;
  font-size: var(--text-sm);
  border-bottom: 1px solid rgba(34,46,62,.62);
  border-radius: 5px;
  transition: background var(--duration-fast), transform var(--duration-fast);
}

.activity-item:hover {
  background: rgba(148,163,184,.045);
}
.activity-item.item-error { background: linear-gradient(90deg, rgba(239,68,68,.06), transparent 70%); }
.activity-item.item-success { background: linear-gradient(90deg, rgba(34,197,94,.025), transparent 70%); }

.activity-time {
  color: var(--text-muted);
  font-size: var(--text-xs);
  flex-shrink: 0;
  width: 56px;
  padding-top: 2px;
}

.activity-badge {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
  background: currentColor;
  box-shadow: 0 0 0 3px color-mix(in srgb, currentColor 10%, transparent);
}

.activity-content {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(116px,.42fr) minmax(0,1fr) auto;
  align-items: center;
  gap: 10px;
}

.activity-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.activity-type {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: .055em;
}

.activity-tool {
  font-size: var(--text-xs);
  color: var(--agent);
}

.activity-detail {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-latency {
  font-size: 9px;
  color: var(--text-muted);
  margin-top: 0;
}

.activity-thinking {
  opacity: 0.6;
}

.item-error .activity-type { color: var(--danger); }
.item-success .activity-type { color: var(--success); }
.badge-fail { color: var(--danger); }
.badge-pass { color: var(--success); }
.badge-running { color: var(--info); }
.badge-info { color: var(--text-muted); }

@media (max-width: 760px) {
  .activity-content { grid-template-columns: 1fr; gap: 2px; }
  .activity-latency { display: none; }
}
</style>
