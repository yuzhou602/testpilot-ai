<template>
  <div class="app-shell" :class="{ 'nav-compact': sidebarCompact, 'nav-open': mobileNavOpen }">
    <a class="skip-link" href="#main-workspace">{{ t('shell.skipWorkspace') }}</a>

    <header class="global-header">
      <div class="header-leading">
        <button class="icon-button mobile-menu" :aria-label="t('shell.openNavigation')" @click="mobileNavOpen = !mobileNavOpen">
          <el-icon><Menu /></el-icon>
        </button>
        <button class="brand" :aria-label="t('shell.openWorkspace')" @click="router.push('/workspace')">
          <span class="brand-mark" aria-hidden="true">
            <span class="brand-cross"></span><span class="brand-core"></span>
          </span>
          <span class="brand-word">TestPilot <b>AI</b></span>
        </button>
        <span class="crumb-separator">/</span>
        <el-dropdown trigger="click" @command="selectProject">
          <button class="project-switcher">
            <span>{{ currentProject?.name || t('shell.selectProject') }}</span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="project in store.projects" :key="project.id" :command="project">
                <span class="project-menu-item"><span class="project-dot"></span>{{ project.name }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-if="route.meta.section">
          <span class="crumb-separator desktop-only">/</span>
          <span class="route-crumb desktop-only">{{ translatedSection }}</span>
        </template>
      </div>

      <div class="header-status" :class="{ running: store.isAgentRunning }" aria-live="polite">
        <span class="status-orbit"><span></span></span>
        <span>{{ store.isAgentRunning ? t('shell.agentExecuting') : t('shell.agentOnline') }}</span>
        <span v-if="store.isDemoMode" class="demo-label">DEMO</span>
      </div>

      <div class="header-actions">
        <span class="env-badge">TEST</span>
        <button class="tour-button" :title="t('tour.open')" @click="tourOpen = true"><span>?</span><b>{{ t('tour.open') }}</b></button>
        <button class="model-button mono" :title="t('shell.activeModel')">GPT-5 <el-icon><ArrowDown /></el-icon></button>
        <LanguageSwitcher />
        <button class="icon-button" :aria-label="t('shell.notifications')" :title="t('shell.notifications')">
          <el-icon><Bell /></el-icon><span class="notification-dot"></span>
        </button>
        <button class="icon-button" :aria-label="store.theme === 'dark' ? t('shell.useLight') : t('shell.useDark')" @click="toggleTheme">
          <el-icon><Sunny v-if="store.theme === 'dark'" /><Moon v-else /></el-icon>
        </button>
        <button class="user-avatar" :aria-label="t('shell.userMenu')">{{ userInitial }}</button>
      </div>
    </header>

    <div class="shell-body">
      <nav class="developer-sidebar" :aria-label="t('shell.primaryNavigation')">
        <div class="nav-scroll">
          <section v-for="group in navigation" :key="group.label" class="nav-group">
            <div class="nav-group-label">{{ group.label }}</div>
            <router-link
              v-for="item in group.items"
              :key="item.label"
              :to="item.to"
              class="nav-item"
              :title="sidebarCompact ? item.label : undefined"
              @click="mobileNavOpen = false"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span class="nav-item-label">{{ item.label }}</span>
              <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
            </router-link>
          </section>
          <section class="nav-group nav-more-group">
            <button class="nav-more-toggle" :aria-expanded="showAdvancedNavigation" @click="showAdvancedNavigation = !showAdvancedNavigation">
              <el-icon><MoreFilled /></el-icon>
              <span class="nav-item-label">{{ t('shell.moreTools') }}</span>
              <el-icon class="nav-more-arrow"><ArrowDown /></el-icon>
            </button>
            <div v-show="showAdvancedNavigation" class="nav-more-items">
              <router-link v-for="item in advancedNavigation" :key="item.label" :to="item.to" class="nav-item" @click="mobileNavOpen = false">
                <el-icon><component :is="item.icon" /></el-icon>
                <span class="nav-item-label">{{ item.label }}</span>
              </router-link>
            </div>
          </section>
        </div>

        <div class="sidebar-footer">
          <div class="runtime-status" :title="t('shell.runtimeReachable')">
            <span class="runtime-dot"></span>
            <span class="nav-item-label">{{ t('shell.runtimeConnected') }}</span>
          </div>
          <button class="collapse-button" :aria-label="sidebarCompact ? t('shell.expand') : t('shell.collapse')" @click="sidebarCompact = !sidebarCompact">
            <el-icon><DArrowLeft /></el-icon>
            <span class="nav-item-label">{{ t('shell.collapse') }}</span>
          </button>
        </div>
      </nav>

      <button v-if="mobileNavOpen" class="nav-scrim" :aria-label="t('shell.closeNavigation')" @click="mobileNavOpen = false"></button>
      <main id="main-workspace" class="main-workspace" tabindex="-1">
        <router-view />
      </main>
    </div>
    <DemoGuide v-model="tourOpen" />
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Aim, ArrowDown, Bell, Connection, DataAnalysis, DArrowLeft, Document,
  Files, FolderOpened, MagicStick, Menu, Monitor, Moon, Operation,
  Postcard, PriceTag, Setting, Sunny, Tickets, TrendCharts,
  MoreFilled,
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import type { TestProject } from '@/types'
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue'
import DemoGuide from '@/components/common/DemoGuide.vue'
import { setLocale } from '@/locales'

const store = useAppStore()
const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const sidebarCompact = ref(localStorage.getItem('tp-sidebar') === 'compact')
const mobileNavOpen = ref(false)
const showAdvancedNavigation = ref(false)
const tourOpen = ref(false)
const user = ref<{ displayName?: string }>({})

const currentProject = computed(() => store.currentProject)
const userInitial = computed(() => user.value.displayName?.[0]?.toUpperCase() || 'JF')
const tracePath = computed(() => `/trace/${store.currentTask?.id || 102}`)
const translatedSection = computed(() => {
  const sectionByPath: Record<string, string> = {
    '/workspace': 'nav.workspace', '/projects': 'nav.projects', '/requirements': 'nav.requirements',
    '/test-cases': 'nav.testCases', '/api-testing': 'nav.apiTesting', '/ui-testing': 'nav.uiTesting',
    '/bugs': 'nav.bugs', '/reports': 'nav.reports', '/evaluation': 'nav.evaluation', '/settings': 'nav.settings',
  }
  return route.path.startsWith('/trace/') ? t('shell.traces') : t(sectionByPath[route.path] || 'nav.workspace')
})

const navigation = computed(() => [
  { label: t('shell.workspaceGroup'), items: [
    { label: t('nav.runTest'), to: '/workspace', icon: markRaw(MagicStick) },
    { label: t('nav.apiTesting'), to: '/api-testing', icon: markRaw(Connection) },
    { label: t('nav.uiTesting'), to: '/ui-testing', icon: markRaw(Monitor) },
    { label: t('nav.bugs'), to: '/bugs', icon: markRaw(PriceTag), badge: 2 },
    { label: t('nav.reports'), to: '/reports', icon: markRaw(DataAnalysis) },
  ] },
])

const advancedNavigation = computed(() => [
    { label: t('nav.projects'), to: '/projects', icon: markRaw(FolderOpened) },
    { label: t('nav.requirements'), to: '/requirements', icon: markRaw(Document) },
    { label: t('nav.testCases'), to: '/test-cases', icon: markRaw(Tickets) },
    { label: t('shell.traces'), to: tracePath.value, icon: markRaw(Operation) },
    { label: t('nav.evaluation'), to: '/evaluation', icon: markRaw(TrendCharts) },
    { label: t('nav.settings'), to: '/settings', icon: markRaw(Setting) },
])

const advancedPaths = ['/projects', '/requirements', '/test-cases', '/evaluation', '/settings']
watch(() => route.path, (path) => {
  if (advancedPaths.includes(path) || path.startsWith('/trace/')) showAdvancedNavigation.value = true
}, { immediate: true })

async function selectProject(project: TestProject) {
  await store.selectProject(project)
}

function toggleTheme() {
  const next = store.theme === 'dark' ? 'light' : 'dark'
  store.setTheme(next)
  localStorage.setItem('tp-theme', next)
}

onMounted(async () => {
  const requestedLocale = route.query.lang
  if (requestedLocale === 'zh' || requestedLocale === 'en') setLocale(requestedLocale)
  user.value = JSON.parse(localStorage.getItem('user') || '{"displayName":"JF"}')
  store.setTheme((localStorage.getItem('tp-theme') as 'dark' | 'light') || 'dark')
  if (!store.projects.length) await store.loadProjects(1)
  if (route.query.tour === '1') {
    tourOpen.value = true
    router.replace({ path: route.path, query: { ...route.query, tour: undefined } })
  }
})
</script>

<style scoped>
.app-shell { height: 100dvh; display: flex; flex-direction: column; background: var(--bg-base); }
.skip-link { position: fixed; left: 12px; top: -48px; z-index: 999; padding: 8px 12px; background: var(--primary); color: #fff; border-radius: 6px; text-decoration: none; }
.skip-link:focus { top: 8px; }
.global-header { height: 52px; display: grid; grid-template-columns: minmax(0,1fr) auto minmax(0,1fr); align-items: center; gap: 16px; padding: 0 14px 0 12px; flex: none; background: var(--bg-surface); border-bottom: 1px solid var(--border); z-index: var(--z-header); }
.header-leading, .header-actions, .header-status { display: flex; align-items: center; min-width: 0; }
.header-leading { gap: 9px; }
.header-actions { justify-content: flex-end; gap: 7px; }
.brand { height: 34px; display: inline-flex; align-items: center; gap: 9px; border: 0; background: transparent; color: var(--text-primary); cursor: pointer; }
.brand-mark { position: relative; width: 24px; height: 24px; display: grid; place-items: center; border: 1px solid var(--border-active); border-radius: 5px; background: var(--bg-elevated); overflow: hidden; }
.brand-cross::before, .brand-cross::after { content: ''; position: absolute; left: 5px; right: 5px; top: 11px; height: 1px; background: #526b7d; }
.brand-cross::after { transform: rotate(90deg); }
.brand-core { width: 6px; height: 6px; border: 1px solid #90aabd; border-radius: 1px; background: var(--primary); z-index: 1; }
.brand-word { font-size: 14px; font-weight: 650; letter-spacing: -.02em; white-space: nowrap; }
.brand-word b { color: var(--text-muted); font-weight: 600; }
.crumb-separator { color: var(--border-active); }
.project-switcher, .model-button { height: 30px; display: inline-flex; align-items: center; gap: 6px; border: 0; background: transparent; color: var(--text-secondary); cursor: pointer; border-radius: 5px; }
.project-switcher { max-width: 180px; padding: 0 5px; font-size: 13px; font-weight: 500; }
.project-switcher span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.project-switcher:hover, .model-button:hover { background: var(--bg-hover); color: var(--text-primary); }
.route-crumb { color: var(--text-muted); font-size: 12px; white-space: nowrap; }
.project-menu-item { display: flex; align-items: center; gap: 8px; }
.project-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary); }
.header-status { gap: 7px; height: 26px; padding: 0 9px; color: var(--text-muted); border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-elevated); font-size: 11px; }
.header-status.running { color: #86b2a0; border-color: rgba(74,134,110,.3); }
.status-orbit { width: 11px; height: 11px; display: grid; place-items: center; border: 1px solid currentColor; border-radius: 50%; opacity: .9; }
.status-orbit span { width: 3px; height: 3px; border-radius: 50%; background: currentColor; }
.running .status-orbit { animation: status-pulse 2.2s ease-in-out infinite; }
.demo-label { padding-left: 7px; border-left: 1px solid var(--border); color: var(--text-muted); font: 600 9px/1 var(--font-mono); }
.env-badge { padding: 2px 6px; color: var(--info); background: rgba(59,130,246,.1); border: 1px solid rgba(59,130,246,.2); border-radius: 4px; font: 700 10px/16px var(--font-mono); letter-spacing: .05em; }
.model-button { padding: 0 7px; font-size: 10px; }
.tour-button { height: 28px; display: inline-flex; align-items: center; gap: 5px; padding: 0 7px; border: 1px solid var(--border-subtle); border-radius: 5px; background: var(--bg-elevated); color: var(--text-muted); cursor: pointer; }
.tour-button:hover { border-color: var(--border-active); color: var(--text-primary); }
.tour-button span { display: grid; place-items: center; width: 13px; height: 13px; border: 1px solid currentColor; border-radius: 50%; font: 8px var(--font-mono); }
.tour-button b { font-size: 9px; font-weight: 500; }
.icon-button, .user-avatar { position: relative; width: 32px; height: 32px; display: grid; place-items: center; border: 0; border-radius: 6px; background: transparent; color: var(--text-muted); cursor: pointer; transition: background var(--duration-fast), color var(--duration-fast); }
.icon-button:hover { background: var(--bg-hover); color: var(--text-primary); }
.notification-dot { position: absolute; right: 7px; top: 6px; width: 5px; height: 5px; border-radius: 50%; background: var(--danger); box-shadow: 0 0 0 2px var(--bg-surface); }
.user-avatar { margin-left: 2px; border: 1px solid var(--border-active); background: var(--bg-elevated); color: var(--text-secondary); font-size: 11px; font-weight: 700; }
.mobile-menu { display: none; }
.shell-body { min-height: 0; flex: 1; display: flex; overflow: hidden; }
.developer-sidebar { width: 200px; display: flex; flex-direction: column; flex: none; overflow: hidden; background: var(--bg-sidebar); border-right: 1px solid var(--border); transition: width var(--duration-slow) var(--ease-enter); z-index: var(--z-sidebar); }
.nav-scroll { flex: 1; overflow-y: auto; padding: 7px 7px 12px; }
.nav-group { padding-top: 9px; }
.nav-group-label { height: 22px; padding: 0 9px; color: var(--text-muted); font: 600 10px/22px var(--font-mono); letter-spacing: .08em; text-transform: uppercase; white-space: nowrap; overflow: hidden; }
.nav-item { position: relative; height: 34px; display: flex; align-items: center; gap: 9px; padding: 0 9px; margin: 1px 0; border-radius: 6px; color: var(--text-secondary); text-decoration: none; font-size: 13px; transition: background var(--duration-fast), color var(--duration-fast); }
.nav-item::before { content: ''; position: absolute; left: -7px; width: 2px; height: 18px; border-radius: 0 2px 2px 0; background: transparent; }
.nav-item:hover { color: var(--text-primary); background: var(--bg-hover); }
.nav-item.router-link-active { color: var(--text-primary); background: var(--bg-selected); }
.nav-item.router-link-active::before { background: var(--primary); }
.nav-item .el-icon { width: 17px; height: 17px; flex: none; color: var(--text-muted); }
.nav-item.router-link-active .el-icon { color: #8da7ba; }
.nav-more-toggle { width: 100%; height: 34px; display: flex; align-items: center; gap: 9px; padding: 0 9px; border: 0; border-radius: 6px; background: transparent; color: var(--text-secondary); font-size: 13px; cursor: pointer; }
.nav-more-toggle:hover { color: var(--text-primary); background: var(--bg-hover); }
.nav-more-toggle > .el-icon:first-child { width: 17px; color: var(--text-muted); }
.nav-more-arrow { margin-left: auto; transition: transform var(--duration-fast); }
.nav-more-toggle[aria-expanded="true"] .nav-more-arrow { transform: rotate(180deg); }
.nav-more-items { padding-top: 2px; }
.nav-compact .nav-more-arrow { display: none; }
.nav-item-label { overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.nav-badge { margin-left: auto; min-width: 18px; padding: 0 5px; border-radius: 8px; color: #fca5a5; background: rgba(239,68,68,.1); font: 600 10px/17px var(--font-mono); text-align: center; }
.sidebar-footer { padding: 8px 7px; border-top: 1px solid var(--border-subtle); }
.runtime-status, .collapse-button { height: 32px; display: flex; align-items: center; gap: 9px; width: 100%; padding: 0 9px; color: var(--text-muted); font-size: 11px; }
.runtime-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--success); box-shadow: 0 0 0 3px rgba(34,197,94,.08); }
.collapse-button { margin-top: 3px; border: 0; border-radius: 5px; background: transparent; cursor: pointer; }
.collapse-button:hover { background: var(--bg-hover); color: var(--text-primary); }
.collapse-button .el-icon { transition: transform var(--duration-slow); }
.main-workspace { min-width: 0; flex: 1; overflow: hidden; background: var(--bg-base); }
.nav-compact .developer-sidebar { width: 56px; }
.nav-compact .nav-group-label, .nav-compact .nav-item-label, .nav-compact .nav-badge { opacity: 0; pointer-events: none; }
.nav-compact .nav-group-label { height: 10px; }
.nav-compact .nav-item, .nav-compact .runtime-status, .nav-compact .collapse-button { justify-content: center; padding: 0; }
.nav-compact .collapse-button .el-icon { transform: rotate(180deg); }
.nav-scrim { display: none; }

@media (max-width: 1199px) {
  .tour-button b { display: none; }
  .tour-button { width: 28px; justify-content: center; padding: 0; }
  .developer-sidebar { width: 56px; }
  .nav-group-label, .nav-item-label, .nav-badge { opacity: 0; pointer-events: none; }
  .nav-group-label { height: 10px; }
  .nav-item, .runtime-status, .collapse-button { justify-content: center; padding: 0; }
  .collapse-button { display: none; }
}
@media (max-width: 767px) {
  .global-header { grid-template-columns: minmax(0,1fr) auto; padding: 0 8px; }
  .mobile-menu { display: grid; }
  .brand-word, .crumb-separator, .project-switcher .el-icon, .model-button, .header-status, .desktop-only { display: none; }
  .project-switcher { max-width: 120px; }
  .header-actions { gap: 2px; }
  .developer-sidebar { position: fixed; left: 0; top: 52px; bottom: 0; width: 224px; transform: translateX(-100%); box-shadow: 14px 0 36px rgba(0,0,0,.26); transition: transform var(--duration-slow) var(--ease-enter); }
  .nav-open .developer-sidebar { transform: translateX(0); }
  .nav-open .nav-group-label, .nav-open .nav-item-label, .nav-open .nav-badge { opacity: 1; pointer-events: auto; }
  .nav-open .nav-group-label { height: 22px; }
  .nav-open .nav-item, .nav-open .runtime-status { justify-content: flex-start; padding: 0 9px; }
  .nav-scrim { position: fixed; inset: 52px 0 0; z-index: 15; display: block; border: 0; background: rgba(3,7,18,.56); }
}
</style>
