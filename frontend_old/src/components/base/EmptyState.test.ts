import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmptyState from './EmptyState.vue'

describe('EmptyState', () => {
  it('should render with default description', () => {
    const wrapper = mount(EmptyState)
    expect(wrapper.exists()).toBe(true)
  })

  it('should render with custom description', () => {
    const wrapper = mount(EmptyState, {
      props: {
        description: 'Aucun résultat trouvé',
      },
    })
    expect(wrapper.text()).toContain('Aucun résultat trouvé')
  })

  it('should emit action event when action button is clicked', async () => {
    const wrapper = mount(EmptyState, {
      props: {
        actionText: 'Ajouter',
      },
    })

    const button = wrapper.find('button')
    await button.trigger('click')

    expect(wrapper.emitted('action')).toBeTruthy()
  })
})
