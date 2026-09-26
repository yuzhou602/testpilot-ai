import { ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'

interface UseAsyncOptions {
  successMessage?: string
  errorMessage?: string
  onSuccess?: (data: any) => void
  onError?: (error: any) => void
}

export function useAsync<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  options: UseAsyncOptions = {}
) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const data = ref<any>(null)

  async function execute(...args: Parameters<T>) {
    loading.value = true
    error.value = null

    try {
      const result = await fn(...args)
      data.value = result

      if (options.successMessage) {
        ElMessage.success(options.successMessage)
      }

      options.onSuccess?.(result)
      return result
    } catch (e: any) {
      const message = e.response?.data?.message || e.message || options.errorMessage || 'Operation failed'
      error.value = message
      ElMessage.error(message)
      options.onError?.(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    data,
    execute,
  }
}
