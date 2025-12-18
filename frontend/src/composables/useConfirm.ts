/**
 * Composable for confirmation dialogs
 */
export function useConfirm() {
  const dialog = useDialog()

  function confirm(
    message: string,
    options: {
      title?: string
      positiveText?: string
      negativeText?: string
      onPositiveClick?: () => void | Promise<void>
      onNegativeClick?: () => void
    } = {}
  ) {
    const {
      title = 'Confirmation',
      positiveText = 'Confirmer',
      negativeText = 'Annuler',
      onPositiveClick,
      onNegativeClick,
    } = options

    dialog.warning({
      title,
      content: message,
      positiveText,
      negativeText,
      onPositiveClick,
      onNegativeClick,
    })
  }

  return {
    confirm,
  }
}
