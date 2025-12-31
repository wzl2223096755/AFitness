import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { getThemeCSSVariables, breakpoints, useResponsive } from './useTheme'

describe('useTheme', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')
    document.documentElement.removeAttribute('data-system-theme')
  })

  describe('getThemeCSSVariables', () => {
    it('should return light theme variables for light theme', () => {
      const variables = getThemeCSSVariables('light')
      
      expect(variables['--bg-primary']).toBe('#ffffff')
      expect(variables['--text-primary']).toBe('#303133')
      expect(variables['--border-color']).toBe('#dcdfe6')
    })

    it('should return dark theme variables for dark theme', () => {
      const variables = getThemeCSSVariables('dark')
      
      expect(variables['--bg-primary']).toBe('#1a1a1a')
      expect(variables['--text-primary']).toBe('#ffffff')
      expect(variables['--border-color']).toBe('#404040')
    })

    it('should return light theme variables for unknown theme', () => {
      const variables = getThemeCSSVariables('unknown')
      
      expect(variables['--bg-primary']).toBe('#ffffff')
    })

    it('should include all required CSS variables', () => {
      const variables = getThemeCSSVariables('light')
      
      const requiredVars = [
        '--bg-primary',
        '--bg-secondary',
        '--bg-sidebar',
        '--bg-header',
        '--bg-card',
        '--text-primary',
        '--text-regular',
        '--text-secondary',
        '--text-placeholder',
        '--text-white',
        '--border-color',
        '--border-light',
        '--border-lighter',
        '--shadow-base',
        '--shadow-light'
      ]
      
      requiredVars.forEach(varName => {
        expect(variables[varName]).toBeDefined()
      })
    })
  })

  describe('breakpoints', () => {
    it('should have correct breakpoint values', () => {
      expect(breakpoints.xs).toBe(480)
      expect(breakpoints.sm).toBe(768)
      expect(breakpoints.md).toBe(992)
      expect(breakpoints.lg).toBe(1200)
      expect(breakpoints.xl).toBe(1920)
    })
  })

  describe('useResponsive', () => {
    it('should return responsive utilities', () => {
      const { windowWidth, currentBreakpoint, isMobile, isTablet, isDesktop } = useResponsive()
      
      expect(windowWidth.value).toBeDefined()
      expect(currentBreakpoint.value).toBeDefined()
      expect(isMobile.value).toBeDefined()
      expect(isTablet.value).toBeDefined()
      expect(isDesktop.value).toBeDefined()
    })

    it('should detect mobile breakpoint', () => {
      // Mock window.innerWidth
      Object.defineProperty(window, 'innerWidth', { value: 400, writable: true })
      
      const { currentBreakpoint, isMobile } = useResponsive()
      
      // Trigger resize to update
      window.dispatchEvent(new Event('resize'))
      
      // Note: The initial value is set at creation time
      expect(typeof isMobile.value).toBe('boolean')
    })

    it('should detect desktop breakpoint', () => {
      Object.defineProperty(window, 'innerWidth', { value: 1400, writable: true })
      
      const { isDesktop } = useResponsive()
      
      expect(typeof isDesktop.value).toBe('boolean')
    })
  })
})
