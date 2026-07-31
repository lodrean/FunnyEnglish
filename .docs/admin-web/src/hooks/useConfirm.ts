import { useState, useCallback } from 'react'

interface ConfirmOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
}

interface ConfirmState extends ConfirmOptions {
  isOpen: boolean
  resolve: ((value: boolean) => void) | null
}

export const useConfirm = () => {
  const [state, setState] = useState<ConfirmState>({
    isOpen: false,
    title: '',
    message: '',
    confirmText: 'Confirm',
    cancelText: 'Cancel',
    danger: false,
    resolve: null,
  })

  const confirm = useCallback((options: ConfirmOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      setState({
        ...options,
        confirmText: options.confirmText || 'Confirm',
        cancelText: options.cancelText || 'Cancel',
        danger: options.danger || false,
        isOpen: true,
        resolve,
      })
    })
  }, [])

  const handleConfirm = useCallback(() => {
    if (state.resolve) {
      state.resolve(true)
    }
    setState((prev) => ({ ...prev, isOpen: false }))
  }, [state.resolve])

  const handleCancel = useCallback(() => {
    if (state.resolve) {
      state.resolve(false)
    }
    setState((prev) => ({ ...prev, isOpen: false }))
  }, [state.resolve])

  return {
    confirm,
    confirmState: state,
    handleConfirm,
    handleCancel,
  }
}
