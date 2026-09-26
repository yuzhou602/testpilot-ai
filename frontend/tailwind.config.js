/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        base: 'var(--bg-base)',
        surface: 'var(--bg-surface)',
        elevated: 'var(--bg-elevated)',
        overlay: 'var(--bg-overlay)',
        border: { DEFAULT: 'var(--border)', subtle: 'var(--border-subtle)', active: 'var(--border-active)' },
        text: { primary: 'var(--text-primary)', secondary: 'var(--text-secondary)', muted: 'var(--text-muted)' },
        agent: 'var(--agent)',
        primary: 'var(--primary)',
        success: 'var(--success)',
        warning: 'var(--warning)',
        danger: 'var(--danger)',
        info: 'var(--info)',
      },
      fontFamily: {
        sans: ['Inter', 'PingFang SC', 'Microsoft YaHei', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      fontSize: {
        'xs': '11px',
        'sm': '12px',
        'base': '13px',
        'md': '14px',
        'lg': '16px',
        'xl': '18px',
        '2xl': '24px',
      },
      borderRadius: {
        'xs': 'var(--radius-xs)',
        'sm': 'var(--radius-sm)',
        'md': 'var(--radius-md)',
        'lg': 'var(--radius-lg)',
        'xl': 'var(--radius-xl)',
      },
      spacing: {
        '1': '4px',
        '2': '8px',
        '3': '12px',
        '4': '16px',
        '5': '20px',
        '6': '24px',
        '8': '32px',
      },
      animation: {
        'pulse-slow': 'status-pulse 2.2s ease-in-out infinite',
        'enter': 'enter-up var(--duration-normal) var(--ease-enter)',
      },
      transitionDuration: {
        'fast': 'var(--duration-fast)',
        'normal': 'var(--duration-normal)',
        'slow': 'var(--duration-slow)',
      },
      zIndex: {
        'sidebar': 'var(--z-sidebar)',
        'header': 'var(--z-header)',
        'dropdown': 'var(--z-dropdown)',
        'modal': 'var(--z-modal)',
        'tooltip': 'var(--z-tooltip)',
      },
      boxShadow: {
        'panel': '0 8px 24px rgba(0, 0, 0, 0.18)',
        'focus': '0 0 0 2px rgba(99, 102, 241, 0.14)',
      },
    },
  },
  plugins: [],
}
