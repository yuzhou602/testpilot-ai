import { ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'

interface ErrorHandlerOptions {
  showMessage?: boolean
  showDialog?: boolean
  retry?: () => Promise<void>
  maxRetries?: number
}

export function useErrorHandler(options: ErrorHandlerOptions = {}) {
  const {
    showMessage = true,
    showDialog = false,
    retry,
    maxRetries = 3,
  } = options

  const error = ref<Error | null>(null)
  const retryCount = ref(0)
  const isRetrying = ref(false)

  function handleError(err: Error | string, context?: string) {
    const errorObj = err instanceof Error ? err : new Error(err)
    error.value = errorObj

    const message = context
      ? `${context}: ${errorObj.message}`
      : errorObj.message

    if (showMessage) {
      ElMessage.error(message)
    }

    if (showDialog) {
      ElMessageBox.alert(message, 'Error', {
        confirmButtonText: retry && retryCount.value < maxRetries ? 'Retry' : 'OK',
        type: 'error',
      }).then(async () => {
        if (retry && retryCount.value < maxRetries) {
          await handleRetry()
        }
      })
    }

    console.error('[ErrorHandler]', message, errorObj)
  }

  async function handleRetry() {
    if (!retry || retryCount.value >= maxRetries) return

    isRetrying.value = true
    retryCount.value++

    try {
      await retry()
      error.value = null
    } catch (err) {
      handleError(err as Error, `Retry ${retryCount.value}/${maxRetries} failed`)
    } finally {
      isRetrying.value = false
    }
  }

  function clearError() {
    error.value = null
    retryCount.value = 0
  }

  return {
    error,
    retryCount,
    isRetrying,
    handleError,
    handleRetry,
    clearError,
  }
}
