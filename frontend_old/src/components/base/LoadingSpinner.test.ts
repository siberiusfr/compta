import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingSpinner from './LoadingSpinner.vue'

describe('LoadingSpinner', () => {
  it('should render with default props', () => {
    const wrapper = mount(LoadingSpinner)
    expect(wrapper.exists()).toBe(true)
  })

  it('should apply fullscreen class when fullscreen prop is true', () => {
    const wrapper = mount(LoadingSpinner, {
      props: {
        fullscreen: true,
      },
    })

    expect(wrapper.find('.loading-spinner--fullscreen').exists()).toBe(true)
  })

  it('should not apply fullscreen class when fullscreen prop is false', () => {
    const wrapper = mount(LoadingSpinner, {
      props: {
        fullscreen: false,
      },
    })

    expect(wrapper.find('.loading-spinner--fullscreen').exists()).toBe(false)
  })
})
