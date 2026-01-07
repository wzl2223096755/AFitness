// 简化的消息系统，替换Element Plus的ElMessage
class Message {
  static success(message) {
    console.log('✅ Success:', message)
    this.showToast(message, 'success')
  }

  static error(message) {
    console.error('❌ Error:', message)
    this.showToast(message, 'error')
  }

  static warning(message) {
    console.warn('⚠️ Warning:', message)
    this.showToast(message, 'warning')
  }

  static info(message) {
    console.info('ℹ️ Info:', message)
    this.showToast(message, 'info')
  }

  static showToast(message, type = 'info') {
    // 创建toast元素
    const toast = document.createElement('div')
    toast.className = `toast toast-${type}`
    toast.innerHTML = `
      <div class="toast-content">
        <span class="toast-icon">${this.getIcon(type)}</span>
        <span class="toast-message">${message}</span>
      </div>
    `

    // 添加样式
    const style = document.createElement('style')
    style.textContent = `
      .toast {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 300px;
        max-width: 500px;
        padding: 12px 16px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        animation: slideIn 0.3s ease;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
      
      .toast-success {
        background: #f0f9ff;
        border: 1px solid #10b981;
        color: #065f46;
      }
      
      .toast-error {
        background: #fef2f2;
        border: 1px solid #ef4444;
        color: #991b1b;
      }
      
      .toast-warning {
        background: #fffbeb;
        border: 1px solid #f59e0b;
        color: #92400e;
      }
      
      .toast-info {
        background: #eff6ff;
        border: 1px solid #3b82f6;
        color: #1e40af;
      }
      
      .toast-content {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .toast-icon {
        font-size: 16px;
        flex-shrink: 0;
      }
      
      .toast-message {
        flex: 1;
        font-size: 14px;
        line-height: 1.4;
      }
      
      @keyframes slideIn {
        from {
          transform: translateX(100%);
          opacity: 0;
        }
        to {
          transform: translateX(0);
          opacity: 1;
        }
      }
      
      @keyframes slideOut {
        from {
          transform: translateX(0);
          opacity: 1;
        }
        to {
          transform: translateX(100%);
          opacity: 0;
        }
      }
    `

    // 如果样式还没有添加，就添加样式
    if (!document.querySelector('#toast-styles')) {
      style.id = 'toast-styles'
      document.head.appendChild(style)
    }

    // 添加到页面
    document.body.appendChild(toast)

    // 自动移除
    setTimeout(() => {
      toast.style.animation = 'slideOut 0.3s ease'
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast)
        }
      }, 300)
    }, 3000)
  }

  static getIcon(type) {
    const icons = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    }
    return icons[type] || icons.info
  }
}

// 简化的确认框系统
class MessageBox {
  static alert(message, title = '提示', options = {}) {
    return new Promise((resolve) => {
      // 创建模态框
      const modal = document.createElement('div')
      modal.className = 'message-box-overlay'
      
      const typeIcon = this.getTypeIcon(options.type)
      const confirmText = options.confirmButtonText || '确定'
      
      modal.innerHTML = `
        <div class="message-box message-box-${options.type || 'info'}">
          <div class="message-box-header">
            ${typeIcon ? `<span class="message-box-icon">${typeIcon}</span>` : ''}
            <h3>${title}</h3>
          </div>
          <div class="message-box-body">
            <p>${message}</p>
          </div>
          <div class="message-box-footer">
            <button class="btn btn-confirm" data-action="confirm">${confirmText}</button>
          </div>
        </div>
      `

      this.addStyles()

      // 添加事件监听
      const handleClick = (e) => {
        const action = e.target.dataset.action
        if (action === 'confirm') {
          document.body.removeChild(modal)
          resolve(true)
        }
      }
      
      modal.addEventListener('click', handleClick)
      
      // 如果允许点击遮罩关闭
      if (options.closeOnClickModal !== false) {
        modal.addEventListener('click', (e) => {
          if (e.target === modal) {
            document.body.removeChild(modal)
            resolve(true)
          }
        })
      }
      
      // 如果允许按ESC关闭
      if (options.closeOnPressEscape !== false) {
        const handleKeydown = (e) => {
          if (e.key === 'Escape') {
            document.body.removeChild(modal)
            document.removeEventListener('keydown', handleKeydown)
            resolve(true)
          }
        }
        document.addEventListener('keydown', handleKeydown)
      }

      // 添加到页面
      document.body.appendChild(modal)
    })
  }

  static confirm(message, title = '确认', options = {}) {
    return new Promise((resolve, reject) => {
      // 创建模态框
      const modal = document.createElement('div')
      modal.className = 'message-box-overlay'
      
      const typeIcon = this.getTypeIcon(options.type)
      const confirmText = options.confirmButtonText || '确定'
      const cancelText = options.cancelButtonText || '取消'
      
      modal.innerHTML = `
        <div class="message-box message-box-${options.type || 'info'}">
          <div class="message-box-header">
            ${typeIcon ? `<span class="message-box-icon">${typeIcon}</span>` : ''}
            <h3>${title}</h3>
          </div>
          <div class="message-box-body">
            <p>${message}</p>
          </div>
          <div class="message-box-footer">
            <button class="btn btn-cancel" data-action="cancel">${cancelText}</button>
            <button class="btn btn-confirm" data-action="confirm">${confirmText}</button>
          </div>
        </div>
      `

      this.addStyles()

      // 添加事件监听
      modal.addEventListener('click', (e) => {
        const action = e.target.dataset.action
        if (action === 'confirm') {
          document.body.removeChild(modal)
          resolve(true)
        } else if (action === 'cancel') {
          document.body.removeChild(modal)
          reject('cancel')
        }
      })

      // 添加到页面
      document.body.appendChild(modal)
    })
  }
  
  static getTypeIcon(type) {
    const icons = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    }
    return icons[type] || ''
  }
  
  static addStyles() {
    // 如果样式还没有添加，就添加样式
    if (document.querySelector('#message-box-styles')) {
      return
    }
    
    const style = document.createElement('style')
    style.id = 'message-box-styles'
    style.textContent = `
      .message-box-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
      
      .message-box {
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        min-width: 400px;
        max-width: 90vw;
        animation: modalSlideIn 0.3s ease;
      }
      
      .message-box-header {
        padding: 20px 20px 10px;
        border-bottom: 1px solid #e5e7eb;
        display: flex;
        align-items: center;
        gap: 10px;
      }
      
      .message-box-icon {
        font-size: 24px;
      }
      
      .message-box-header h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #111827;
      }
      
      .message-box-warning .message-box-header h3 {
        color: #92400e;
      }
      
      .message-box-error .message-box-header h3 {
        color: #991b1b;
      }
      
      .message-box-body {
        padding: 20px;
      }
      
      .message-box-body p {
        margin: 0;
        color: #374151;
        line-height: 1.5;
      }
      
      .message-box-footer {
        padding: 10px 20px 20px;
        display: flex;
        justify-content: flex-end;
        gap: 10px;
      }
      
      .btn {
        padding: 8px 16px;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        background: white;
        color: #374151;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.2s ease;
      }
      
      .btn:hover {
        background: #f9fafb;
      }
      
      .btn-confirm {
        background: #3b82f6;
        border-color: #3b82f6;
        color: white;
      }
      
      .btn-confirm:hover {
        background: #2563eb;
        border-color: #2563eb;
      }
      
      .message-box-warning .btn-confirm {
        background: #f59e0b;
        border-color: #f59e0b;
      }
      
      .message-box-warning .btn-confirm:hover {
        background: #d97706;
        border-color: #d97706;
      }
      
      @keyframes modalSlideIn {
        from {
          opacity: 0;
          transform: scale(0.9);
        }
        to {
          opacity: 1;
          transform: scale(1);
        }
      }
    `
    document.head.appendChild(style)
  }
}

export { Message, MessageBox }

// 兼容Element Plus的导出
export const ElMessage = Message
export const ElMessageBox = MessageBox
export const ElNotification = Message
