import type { GlobalThemeOverrides } from 'naive-ui'

/**
 * Custom theme configuration for Naive UI
 */
export const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#667eea',
    primaryColorHover: '#5568d3',
    primaryColorPressed: '#4c5ec9',
    primaryColorSuppl: '#667eea',

    successColor: '#18a058',
    successColorHover: '#36ad6a',
    successColorPressed: '#0c7a43',

    warningColor: '#f0a020',
    warningColorHover: '#fcb040',
    warningColorPressed: '#c97c10',

    errorColor: '#d03050',
    errorColorHover: '#de576d',
    errorColorPressed: '#ab1f3f',

    infoColor: '#2080f0',
    infoColorHover: '#4098fc',
    infoColorPressed: '#1060c9',

    fontFamily:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", sans-serif',
    fontSize: '14px',
    fontSizeMini: '12px',
    fontSizeTiny: '12px',
    fontSizeSmall: '14px',
    fontSizeMedium: '14px',
    fontSizeLarge: '16px',
    fontSizeHuge: '18px',

    borderRadius: '6px',
    borderRadiusSmall: '4px',

    heightMini: '24px',
    heightTiny: '28px',
    heightSmall: '32px',
    heightMedium: '36px',
    heightLarge: '40px',
    heightHuge: '46px',
  },

  Button: {
    textColor: '#ffffff',
    borderRadiusMedium: '6px',
    fontSizeMedium: '14px',
    heightMedium: '36px',
    paddingMedium: '0 18px',
  },

  Card: {
    borderRadius: '8px',
    paddingMedium: '20px',
    color: '#ffffff',
  },

  Input: {
    borderRadius: '6px',
    heightMedium: '36px',
  },

  Select: {
    borderRadius: '6px',
    heightMedium: '36px',
  },

  Form: {
    labelFontSizeTopMedium: '14px',
    labelHeightMedium: '24px',
    feedbackHeightMedium: '20px',
  },

  DataTable: {
    thColor: '#f5f7fa',
    thTextColor: '#606266',
    tdColorHover: '#f5f7fa',
    borderRadius: '8px',
  },

  Dialog: {
    borderRadius: '8px',
  },

  Menu: {
    itemHeight: '42px',
    itemIconSize: '18px',
  },

  Pagination: {
    itemBorderRadius: '6px',
  },
}

/**
 * Design tokens for consistent spacing, colors, etc.
 */
export const designTokens = {
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '16px',
    lg: '24px',
    xl: '32px',
    xxl: '48px',
  },

  colors: {
    primary: '#667eea',
    secondary: '#764ba2',
    success: '#18a058',
    warning: '#f0a020',
    error: '#d03050',
    info: '#2080f0',
    background: '#ffffff',
    backgroundLight: '#f5f7fa',
    border: '#e4e7ed',
    text: {
      primary: '#303133',
      regular: '#606266',
      secondary: '#909399',
      placeholder: '#c0c4cc',
    },
  },

  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    base: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
  },

  transitions: {
    fast: '150ms',
    base: '200ms',
    slow: '300ms',
  },

  zIndex: {
    dropdown: 1000,
    sticky: 1020,
    fixed: 1030,
    modalBackdrop: 1040,
    modal: 1050,
    popover: 1060,
    tooltip: 1070,
  },
} as const
