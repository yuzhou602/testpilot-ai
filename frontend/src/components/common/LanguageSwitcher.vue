<template>
  <button
    class="language-switch"
    type="button"
    :aria-label="locale === 'zh' ? 'Switch interface to English' : '将界面切换为中文'"
    :title="locale === 'zh' ? 'Switch to English' : '切换为中文'"
    @click="toggleLocale"
  >
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
      <circle cx="12" cy="12" r="9"/>
      <path d="M3 12h18M12 3c2.3 2.5 3.5 5.5 3.5 9S14.3 18.5 12 21c-2.3-2.5-3.5-5.5-3.5-9S9.7 5.5 12 3z"/>
    </svg>
    <b :class="{ active: locale === 'zh' }">中</b>
    <span>/</span>
    <b :class="{ active: locale === 'en' }">EN</b>
  </button>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { setLocale } from '@/locales'

const { locale } = useI18n()

function toggleLocale() {
  setLocale(locale.value === 'zh' ? 'en' : 'zh')
}
</script>

<style scoped>
.language-switch { height: 28px; display: inline-flex; align-items: center; gap: 4px; padding: 0 7px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-elevated); color: var(--text-muted); cursor: pointer; transition: border-color var(--duration-fast), background var(--duration-fast), color var(--duration-fast); }
.language-switch:hover { border-color: var(--border-active); background: var(--bg-hover); color: var(--text-secondary); }
.language-switch b { color: var(--text-disabled); font: 600 9px/1 var(--font-mono); transition: color var(--duration-fast); }
.language-switch b.active { color: var(--text-primary); }
.language-switch span { color: var(--border-active); font-size: 9px; }
</style>
