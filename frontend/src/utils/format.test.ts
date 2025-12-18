import { describe, it, expect } from 'vitest'
import { formatCurrency, formatDate, formatFileSize, truncate } from './format'

describe('formatCurrency', () => {
  it('should format a number as currency', () => {
    expect(formatCurrency(1234.56)).toBe('1234.56 €')
    expect(formatCurrency(0)).toBe('0.00 €')
    expect(formatCurrency(1000)).toBe('1000.00 €')
  })

  it('should support custom currency symbol', () => {
    expect(formatCurrency(100, '$')).toBe('100.00 $')
  })
})

describe('formatDate', () => {
  it('should format a date string', () => {
    const date = new Date('2024-01-15T10:30:00')
    const formatted = formatDate(date)
    expect(formatted).toMatch(/15/)
    expect(formatted).toMatch(/01/)
    expect(formatted).toMatch(/2024/)
  })

  it('should accept string dates', () => {
    const formatted = formatDate('2024-01-15')
    expect(formatted).toBeTruthy()
  })
})

describe('formatFileSize', () => {
  it('should format bytes correctly', () => {
    expect(formatFileSize(0)).toBe('0 B')
    expect(formatFileSize(500)).toBe('500 B')
  })

  it('should format kilobytes correctly', () => {
    expect(formatFileSize(1024)).toBe('1.00 KB')
    expect(formatFileSize(2048)).toBe('2.00 KB')
  })

  it('should format megabytes correctly', () => {
    expect(formatFileSize(1024 * 1024)).toBe('1.00 MB')
    expect(formatFileSize(5 * 1024 * 1024)).toBe('5.00 MB')
  })

  it('should format gigabytes correctly', () => {
    expect(formatFileSize(1024 * 1024 * 1024)).toBe('1.00 GB')
  })
})

describe('truncate', () => {
  it('should truncate long strings', () => {
    expect(truncate('Hello World', 5)).toBe('Hello...')
    expect(truncate('Test', 10)).toBe('Test')
  })

  it('should support custom suffix', () => {
    expect(truncate('Hello World', 5, '---')).toBe('Hello---')
  })
})
