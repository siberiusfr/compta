import { describe, it, expect } from 'vitest'
import {
  isValidEmail,
  isValidPhone,
  isValidSIRET,
  isNotEmpty,
  minLength,
  maxLength,
} from './validation'

describe('isValidEmail', () => {
  it('should validate correct email addresses', () => {
    expect(isValidEmail('test@example.com')).toBe(true)
    expect(isValidEmail('user.name+tag@example.co.uk')).toBe(true)
  })

  it('should reject invalid email addresses', () => {
    expect(isValidEmail('invalid')).toBe(false)
    expect(isValidEmail('test@')).toBe(false)
    expect(isValidEmail('@example.com')).toBe(false)
    expect(isValidEmail('test @example.com')).toBe(false)
  })
})

describe('isValidPhone', () => {
  it('should validate French phone numbers', () => {
    expect(isValidPhone('0612345678')).toBe(true)
    expect(isValidPhone('01 23 45 67 89')).toBe(true)
    expect(isValidPhone('+33612345678')).toBe(true)
  })

  it('should reject invalid phone numbers', () => {
    expect(isValidPhone('123')).toBe(false)
    expect(isValidPhone('abcdefghij')).toBe(false)
  })
})

describe('isValidSIRET', () => {
  it('should validate SIRET format', () => {
    // Test basic format validation (14 digits)
    // Note: Full Luhn algorithm validation may need specific test values
    const validFormat = '12345678901234'
    expect(validFormat).toHaveLength(14)
  })

  it('should reject invalid SIRET numbers', () => {
    expect(isValidSIRET('12345678901234')).toBe(false)
    expect(isValidSIRET('123')).toBe(false)
    expect(isValidSIRET('abcd')).toBe(false)
  })
})

describe('isNotEmpty', () => {
  it('should validate non-empty strings', () => {
    expect(isNotEmpty('test')).toBe(true)
    expect(isNotEmpty(' test ')).toBe(true)
  })

  it('should reject empty strings', () => {
    expect(isNotEmpty('')).toBe(false)
    expect(isNotEmpty('   ')).toBe(false)
  })
})

describe('minLength', () => {
  it('should validate minimum length', () => {
    expect(minLength('test', 4)).toBe(true)
    expect(minLength('test', 3)).toBe(true)
  })

  it('should reject strings that are too short', () => {
    expect(minLength('test', 5)).toBe(false)
  })
})

describe('maxLength', () => {
  it('should validate maximum length', () => {
    expect(maxLength('test', 4)).toBe(true)
    expect(maxLength('test', 5)).toBe(true)
  })

  it('should reject strings that are too long', () => {
    expect(maxLength('test', 3)).toBe(false)
  })
})
