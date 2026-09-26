import { ref, computed } from 'vue'

export function useLoading(initialState = false) {
  const isLoading = ref(initialState)
  const loadingText = ref('')

  function startLoading(text = '') {
    isLoading.value = true
    loadingText.value = text
  }

  function stopLoading() {
    isLoading.value = false
    loadingText.value = ''
  }

  async function withLoading<T>(fn: () => Promise<T>, text = '') {
    startLoading(text)
    try {
      return await fn()
    } finally {
      stopLoading()
    }
  }

  return {
    isLoading,
    loadingText,
    startLoading,
    stopLoading,
    withLoading,
  }
}
