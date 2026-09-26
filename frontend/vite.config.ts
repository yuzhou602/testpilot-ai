import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { resolve } from 'path'

const elementParent: Record<string, string> = {
  'dropdown-item': 'dropdown',
  'dropdown-menu': 'dropdown',
  'form-item': 'form',
  'option': 'select',
  'option-group': 'select',
}

function elementPlusChunk(id: string) {
  if (id.includes('@element-plus/icons-vue')) return 'vendor-element-icons'
  return 'vendor-element-core'
}

function elementPlusDirectResolver(name: string) {
  if (!name.startsWith('El')) return
  const partial = name.slice(2).replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()
  const component = elementParent[partial] || partial
  return {
    name,
    from: `element-plus/es/components/${component}/index.mjs`,
    sideEffects: `element-plus/es/components/${component}/style/css.mjs`,
  }
}

export default defineConfig({
  plugins: [
    vue(),
    Components({
      dts: 'src/components.d.ts',
      resolvers: [elementPlusDirectResolver],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '^/api/': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('@vue-flow')) return 'vendor-flow'
          if (id.includes('element-plus') || id.includes('@element-plus')) return elementPlusChunk(id)
          if (id.includes('vue-i18n') || id.includes('vue-router') || id.includes('pinia') || id.includes('@vueuse') || /node_modules[\\/]vue[\\/]/.test(id)) return 'vendor-vue'
          if (id.includes('axios')) return 'vendor-http'
          return 'vendor-utilities'
        },
      },
    },
  },
})
