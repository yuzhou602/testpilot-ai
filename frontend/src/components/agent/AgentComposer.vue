<template>
  <div class="agent-composer">
    <div v-if="quickStarts.length" class="composer-presets" aria-label="Quick test templates">
      <span class="presets-label">{{ t('workspace.quickStart') }}</span>
      <button v-for="item in quickStarts" :key="item.value" type="button" @click="input = item.value">
        <span>{{ item.label }}</span>
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
    <div class="composer-context">
      <span class="context-item">
        <span class="context-dot"></span>
        {{ currentProject?.name || t('workspace.noProject') }}
      </span>
      <span class="context-item">TEST</span>
      <span class="context-item">{{ t('workspace.openapiConnected') }}</span>
    </div>
    <div class="composer-input-wrap">
      <el-input
        v-model="input"
        type="textarea"
        :rows="1"
        :placeholder="placeholder"
        class="composer-input"
        @keydown.enter.ctrl="submit"
        resize="none"
      />
      <div class="composer-actions">
        <div class="composer-hints">
          <span class="hint-key">Ctrl+Enter</span> {{ t('workspace.toStart') }}
        </div>
        <div class="composer-buttons">
          <el-button size="small" text class="composer-attach" title="Attach requirement">
            <span style="font-size: 14px;">+</span>
          </el-button>
          <el-button size="small" type="primary" @click="submit" :disabled="!input.trim() || loading">
            {{ loading ? t('workspace.starting') : t('workspace.startTest') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const props = withDefaults(defineProps<{
  placeholder?: string
  loading?: boolean
  quickStarts?: Array<{ label: string; value: string }>
}>(), {
  placeholder: 'Describe what you want TestPilot to test...',
  quickStarts: () => [],
})

const emit = defineEmits<{ submit: [goal: string] }>()
const store = useAppStore()
const { t } = useI18n()
const input = ref('')

const currentProject = computed(() => store.currentProject)

function submit() {
  if (!input.value.trim()) return
  emit('submit', input.value.trim())
  input.value = ''
}
</script>

<style scoped>
.agent-composer {
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  background: rgba(12,18,27,.96);
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(0,0,0,.14);
}

.composer-presets {
  display: grid;
  gap: 4px;
  padding: 10px;
  border-bottom: 1px solid var(--border-subtle);
}

.presets-label {
  padding: 2px 4px 5px;
  color: var(--text-muted);
  font: 600 10px/1 var(--font-mono);
  letter-spacing: .08em;
  text-transform: uppercase;
}

.composer-presets button {
  min-height: 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 9px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-secondary);
  font-size: 12px;
  text-align: left;
  cursor: pointer;
  transition: background var(--duration-fast), border-color var(--duration-fast), color var(--duration-fast);
}

.composer-presets button:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
  border-color: var(--border);
}

.composer-presets button .el-icon { color: var(--text-muted); }

.composer-context {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  background: rgba(17,25,37,.72);
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.context-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.context-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--success);
}

.composer-input-wrap {
  padding: 7px 12px 8px;
}

.composer-input :deep(textarea) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: var(--text-primary) !important;
  min-height: 28px !important;
  font-size: 12px !important;
  padding: 5px 0 2px !important;
}

.composer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 5px;
  border-top: 1px solid var(--border-subtle);
}

.composer-hints {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.hint-key {
  padding: 1px 4px;
  background: var(--bg-overlay);
  border-radius: var(--radius-xs);
  font-family: var(--font-mono);
  font-size: 10px;
}

.composer-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
