import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { Message, MessageBox } from './message.js'

describe('Message', () => {
  beforeEach(() => {
    // Clean up any existing toasts
    document.body.innerHTML = ''
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('getIcon', () => {
    it('should return correct icon for success', () => {
      expect(Message.getIcon('success')).toBe('✅')
    })

    it('should return correct icon for error', () => {
      expect(Message.getIcon('error')).toBe('❌')
    })

    it('should return correct icon for warning', () => {
      expect(Message.getIcon('warning')).toBe('⚠️')
    })

    it('should return correct icon for info', () => {
      expect(Message.getIcon('info')).toBe('ℹ️')
    })

    it('should return info icon for unknown type', () => {
      expect(Message.getIcon('unknown')).toBe('ℹ️')
    })
  })

  describe('showToast', () => {
    it('should create toast element in DOM', () => {
      Message.showToast('Test message', 'success')
      
      const toast = document.querySelector('.toast')
      expect(toast).not.toBeNull()
      expect(toast.classList.contains('toast-success')).toBe(true)
    })

    it('should display the correct message', () => {
      Message.showToast('Hello World', 'info')
      
      const messageEl = document.querySelector('.toast-message')
      expect(messageEl.textContent).toBe('Hello World')
    })

    it('should remove toast after timeout', () => {
      Message.showToast('Test message', 'success')
      
      expect(document.querySelector('.toast')).not.toBeNull()
      
      // Fast-forward past the removal time (3000ms + 300ms animation)
      vi.advanceTimersByTime(3500)
      
      expect(document.querySelector('.toast')).toBeNull()
    })
  })

  describe('success', () => {
    it('should show success toast', () => {
      Message.success('Success message')
      
      const toast = document.querySelector('.toast-success')
      expect(toast).not.toBeNull()
    })
  })

  describe('error', () => {
    it('should show error toast', () => {
      Message.error('Error message')
      
      const toast = document.querySelector('.toast-error')
      expect(toast).not.toBeNull()
    })
  })

  describe('warning', () => {
    it('should show warning toast', () => {
      Message.warning('Warning message')
      
      const toast = document.querySelector('.toast-warning')
      expect(toast).not.toBeNull()
    })
  })

  describe('info', () => {
    it('should show info toast', () => {
      Message.info('Info message')
      
      const toast = document.querySelector('.toast-info')
      expect(toast).not.toBeNull()
    })
  })
})

describe('MessageBox', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  describe('confirm', () => {
    it('should create modal overlay', () => {
      MessageBox.confirm('Are you sure?', 'Confirm')
      
      const overlay = document.querySelector('.message-box-overlay')
      expect(overlay).not.toBeNull()
    })

    it('should display title and message', () => {
      MessageBox.confirm('Test message', 'Test Title')
      
      const title = document.querySelector('.message-box-header h3')
      const message = document.querySelector('.message-box-body p')
      
      expect(title.textContent).toBe('Test Title')
      expect(message.textContent).toBe('Test message')
    })

    it('should resolve when confirm button is clicked', async () => {
      const promise = MessageBox.confirm('Are you sure?', 'Confirm')
      
      const confirmBtn = document.querySelector('[data-action="confirm"]')
      confirmBtn.click()
      
      await expect(promise).resolves.toBe(true)
    })

    it('should reject when cancel button is clicked', async () => {
      const promise = MessageBox.confirm('Are you sure?', 'Confirm')
      
      const cancelBtn = document.querySelector('[data-action="cancel"]')
      cancelBtn.click()
      
      await expect(promise).rejects.toBe('cancel')
    })

    it('should remove modal after action', async () => {
      const promise = MessageBox.confirm('Are you sure?', 'Confirm')
      
      const confirmBtn = document.querySelector('[data-action="confirm"]')
      confirmBtn.click()
      
      await promise
      
      const overlay = document.querySelector('.message-box-overlay')
      expect(overlay).toBeNull()
    })
  })
})
