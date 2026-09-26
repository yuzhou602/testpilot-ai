import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      children: [
        { path: '', redirect: '/workspace' },
        { path: 'workspace', name: 'Workspace', meta: { section: 'AI Test' }, component: () => import('@/views/workspace/Workspace.vue') },
        { path: 'projects', name: 'Projects', meta: { section: 'Projects' }, component: () => import('@/views/projects/Projects.vue') },
        { path: 'requirements', name: 'Requirements', meta: { section: 'Requirements' }, component: () => import('@/views/requirements/Requirements.vue') },
        { path: 'test-cases', alias: 'testcases', name: 'TestCases', meta: { section: 'Test Cases' }, component: () => import('@/views/testcases/TestCases.vue') },
        { path: 'api-testing', alias: 'apitesting', name: 'ApiTesting', meta: { section: 'API Testing' }, component: () => import('@/views/apitesting/ApiTesting.vue') },
        { path: 'ui-testing', alias: 'uitesting', name: 'UiTesting', meta: { section: 'UI Testing' }, component: () => import('@/views/uitesting/UiTesting.vue') },
        { path: 'bugs', name: 'Bugs', meta: { section: 'Bugs' }, component: () => import('@/views/bugs/Bugs.vue') },
        { path: 'reports', name: 'Reports', meta: { section: 'Reports' }, component: () => import('@/views/reports/Reports.vue') },
        { path: 'trace/:taskId', name: 'AgentTrace', meta: { section: 'Agent Trace' }, component: () => import('@/views/trace/AgentTrace.vue') },
        { path: 'evaluation', name: 'Evaluation', meta: { section: 'Evaluation' }, component: () => import('@/views/evaluation/Evaluation.vue') },
        { path: 'settings', name: 'Settings', meta: { section: 'Settings' }, component: () => import('@/views/Settings.vue') },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  if (import.meta.env.VITE_AUTH_REQUIRED === 'true' && to.name !== 'Login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
