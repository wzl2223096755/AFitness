/**
 * Visual Hierarchy Property-Based Tests
 * Feature: typography-layout-optimization, Property 8: Color Contrast WCAG Compliance
 * Validates: Requirements 5.3
 *
 * 测试属性：
 * - 对于任何文本元素及其背景，对比度应至少为 4.5:1（普通文本）
 * - 对于大文本（18px+ 或 14px+ 粗体），对比度应至少为 3:1
 */

import { describe, it, expect } from 'vitest'
import { test, fc } from '@fast-check/vitest'

/**
 * Color System 定义
 * 这些值与 variables.scss 中的定义保持一致
 */
const ColorSystem = {
  // 背景颜色 (Dark Space theme)
  backgrounds: {
    'bg-primary': '#0a0a14',
    'bg-secondary': '#121225',
    'bg-sidebar': '#05050a',
    'bg-card': 'rgba(255, 255, 255, 0.03)'
  },

  // 文字颜色
  textColors: {
    'text-primary': '#ffffff',
    'text-regular': '#e0e0ff',
    'text-secondary': '#8888aa',
    'text-placeholder': '#555577'
  },

  // 主题颜色
  themeColors: {
    'primary': '#8020ff',
    'secondary': '#ff00ff',
    'accent': '#00f2fe',
    'success': '#00ff88',
    'warning': '#ffaa00',
    'danger': '#ff0055',
    'info': '#8888ff'
  },

  // 文本类型定义
  textTypes: {
    'normal': { minSize: 16, isBold: false, minContrast: 4.5 },
    'small': { minSize: 14, isBold: false, minContrast: 4.5 },
    'large': { minSize: 18, isBold: false, minContrast: 3.0 },
    'large-bold': { minSize: 14, isBold: true, minContrast: 3.0 }
  },

  // 常见文本/背景组合
  commonCombinations: [
    { text: 'text-primary', bg: 'bg-primary', type: 'normal' },
    { text: 'text-primary', bg: 'bg-secondary', type: 'normal' },
    { text: 'text-primary', bg: 'bg-card', type: 'normal' },
    { text: 'text-regular', bg: 'bg-primary', type: 'normal' },
    { text: 'text-regular', bg: 'bg-secondary', type: 'normal' },
    { text: 'text-secondary', bg: 'bg-primary', type: 'small' },
    { text: 'text-secondary', bg: 'bg-secondary', type: 'small' }
  ]
}

/**
 * 解析颜色值为 RGB
 * @param {string} color - 颜色值（hex 或 rgba）
 * @returns {{ r: number, g: number, b: number, a: number }} RGB 值
 */
function parseColor(color) {
  // Handle hex colors
  if (color.startsWith('#')) {
    const hex = color.slice(1)
    if (hex.length === 3) {
      return {
        r: parseInt(hex[0] + hex[0], 16),
        g: parseInt(hex[1] + hex[1], 16),
        b: parseInt(hex[2] + hex[2], 16),
        a: 1
      }
    }
    return {
      r: parseInt(hex.slice(0, 2), 16),
      g: parseInt(hex.slice(2, 4), 16),
      b: parseInt(hex.slice(4, 6), 16),
      a: 1
    }
  }

  // Handle rgba colors
  const rgbaMatch = color.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/)
  if (rgbaMatch) {
    return {
      r: parseInt(rgbaMatch[1]),
      g: parseInt(rgbaMatch[2]),
      b: parseInt(rgbaMatch[3]),
      a: rgbaMatch[4] ? parseFloat(rgbaMatch[4]) : 1
    }
  }

  // Default to black if parsing fails
  return { r: 0, g: 0, b: 0, a: 1 }
}

/**
 * 计算相对亮度 (WCAG 2.1 公式)
 * @param {{ r: number, g: number, b: number }} rgb - RGB 值
 * @returns {number} 相对亮度 (0-1)
 */
function calculateRelativeLuminance(rgb) {
  const sRGB = [rgb.r / 255, rgb.g / 255, rgb.b / 255]
  const linearRGB = sRGB.map(c => {
    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4)
  })
  return 0.2126 * linearRGB[0] + 0.7152 * linearRGB[1] + 0.0722 * linearRGB[2]
}

/**
 * 混合半透明颜色与背景色
 * @param {{ r: number, g: number, b: number, a: number }} fg - 前景色
 * @param {{ r: number, g: number, b: number }} bg - 背景色
 * @returns {{ r: number, g: number, b: number }} 混合后的颜色
 */
function blendColors(fg, bg) {
  const alpha = fg.a
  return {
    r: Math.round(fg.r * alpha + bg.r * (1 - alpha)),
    g: Math.round(fg.g * alpha + bg.g * (1 - alpha)),
    b: Math.round(fg.b * alpha + bg.b * (1 - alpha))
  }
}

/**
 * 计算对比度 (WCAG 2.1 公式)
 * @param {string} textColor - 文本颜色
 * @param {string} bgColor - 背景颜色
 * @param {string} [baseBgColor] - 基础背景色（用于半透明背景）
 * @returns {number} 对比度
 */
function calculateContrastRatio(textColor, bgColor, baseBgColor = '#0a0a14') {
  const text = parseColor(textColor)
  let bg = parseColor(bgColor)

  // If background is semi-transparent, blend with base background
  if (bg.a < 1) {
    const baseBg = parseColor(baseBgColor)
    bg = blendColors(bg, baseBg)
  }

  const textLuminance = calculateRelativeLuminance(text)
  const bgLuminance = calculateRelativeLuminance(bg)

  const lighter = Math.max(textLuminance, bgLuminance)
  const darker = Math.min(textLuminance, bgLuminance)

  return (lighter + 0.05) / (darker + 0.05)
}

/**
 * 获取 WCAG 最小对比度要求
 * @param {string} textType - 文本类型
 * @returns {number} 最小对比度
 */
function getMinContrastForTextType(textType) {
  return ColorSystem.textTypes[textType]?.minContrast || 4.5
}

// 文本颜色键
const textColorKeys = Object.keys(ColorSystem.textColors)
// 背景颜色键
const backgroundKeys = Object.keys(ColorSystem.backgrounds)
// 文本类型键
const textTypeKeys = Object.keys(ColorSystem.textTypes)

describe('Visual Hierarchy Property-Based Tests', () => {
  /**
   * Feature: typography-layout-optimization, Property 8: Color Contrast WCAG Compliance
   * Validates: Requirements 5.3
   *
   * 属性：对于任何文本元素及其背景，对比度应至少为 4.5:1（普通文本）
   * 或 3:1（大文本：18px+ 或 14px+ 粗体）
   */
  describe('Property 8: Color Contrast WCAG Compliance', () => {
    test.prop([
      fc.constantFrom(...ColorSystem.commonCombinations)
    ], { numRuns: 100 })(
      'Common text/background combinations should meet WCAG AA contrast requirements',
      (combination) => {
        const textColor = ColorSystem.textColors[combination.text]
        const bgColor = ColorSystem.backgrounds[combination.bg]
        const minContrast = getMinContrastForTextType(combination.type)

        const contrastRatio = calculateContrastRatio(textColor, bgColor)

        // WCAG AA: 4.5:1 for normal text, 3:1 for large text
        expect(contrastRatio).toBeGreaterThanOrEqual(minContrast)
      }
    )

    test.prop([
      fc.constantFrom('text-primary', 'text-regular'),
      fc.constantFrom(...backgroundKeys)
    ], { numRuns: 100 })(
      'Primary and regular text should have at least 4.5:1 contrast on all backgrounds',
      (textKey, bgKey) => {
        const textColor = ColorSystem.textColors[textKey]
        const bgColor = ColorSystem.backgrounds[bgKey]

        const contrastRatio = calculateContrastRatio(textColor, bgColor)

        // WCAG AA: 4.5:1 for normal text
        expect(contrastRatio).toBeGreaterThanOrEqual(4.5)
      }
    )

    test.prop([
      fc.constantFrom('text-secondary'),
      fc.constantFrom('bg-primary', 'bg-secondary')
    ], { numRuns: 100 })(
      'Secondary text should have at least 3:1 contrast (large text threshold)',
      (textKey, bgKey) => {
        const textColor = ColorSystem.textColors[textKey]
        const bgColor = ColorSystem.backgrounds[bgKey]

        const contrastRatio = calculateContrastRatio(textColor, bgColor)

        // WCAG AA: 3:1 for large text (secondary text is often used for labels)
        expect(contrastRatio).toBeGreaterThanOrEqual(3.0)
      }
    )

    /**
     * 测试主题颜色在深色背景上的对比度
     */
    test.prop([
      fc.constantFrom(...Object.keys(ColorSystem.themeColors)),
      fc.constantFrom('bg-primary', 'bg-secondary')
    ], { numRuns: 100 })(
      'Theme colors should have at least 3:1 contrast on dark backgrounds (for large text/icons)',
      (themeKey, bgKey) => {
        const themeColor = ColorSystem.themeColors[themeKey]
        const bgColor = ColorSystem.backgrounds[bgKey]

        const contrastRatio = calculateContrastRatio(themeColor, bgColor)

        // Theme colors are typically used for large text, icons, or interactive elements
        // WCAG AA: 3:1 for large text and UI components
        expect(contrastRatio).toBeGreaterThanOrEqual(3.0)
      }
    )

    /**
     * 测试白色文本在所有背景上的对比度
     */
    it('White text (#ffffff) should have high contrast on all dark backgrounds', () => {
      const whiteText = '#ffffff'

      for (const [bgKey, bgColor] of Object.entries(ColorSystem.backgrounds)) {
        const contrastRatio = calculateContrastRatio(whiteText, bgColor)

        // White text on dark backgrounds should have excellent contrast
        expect(contrastRatio).toBeGreaterThanOrEqual(4.5)
      }
    })

    /**
     * 测试对比度计算函数的正确性
     */
    it('Contrast ratio calculation should be correct for known values', () => {
      // Black on white should be 21:1
      const blackOnWhite = calculateContrastRatio('#000000', '#ffffff')
      expect(blackOnWhite).toBeCloseTo(21, 0)

      // White on black should also be 21:1
      const whiteOnBlack = calculateContrastRatio('#ffffff', '#000000')
      expect(whiteOnBlack).toBeCloseTo(21, 0)

      // Same color should be 1:1
      const sameColor = calculateContrastRatio('#888888', '#888888')
      expect(sameColor).toBeCloseTo(1, 0)
    })

    /**
     * 测试相对亮度计算的正确性
     */
    it('Relative luminance calculation should be correct for known values', () => {
      // White should have luminance of 1
      const whiteLuminance = calculateRelativeLuminance({ r: 255, g: 255, b: 255 })
      expect(whiteLuminance).toBeCloseTo(1, 2)

      // Black should have luminance of 0
      const blackLuminance = calculateRelativeLuminance({ r: 0, g: 0, b: 0 })
      expect(blackLuminance).toBeCloseTo(0, 2)
    })

    /**
     * 测试半透明背景的对比度计算
     */
    it('Semi-transparent backgrounds should be correctly blended for contrast calculation', () => {
      const textColor = '#ffffff'
      const semiTransparentBg = 'rgba(255, 255, 255, 0.03)'
      const baseBg = '#0a0a14'

      const contrastRatio = calculateContrastRatio(textColor, semiTransparentBg, baseBg)

      // Should still have good contrast since the background is mostly dark
      expect(contrastRatio).toBeGreaterThanOrEqual(4.5)
    })

    /**
     * 测试所有定义的文本颜色都有有效的对比度
     */
    test.prop([
      fc.constantFrom(...textColorKeys)
    ], { numRuns: 100 })(
      'All defined text colors should have valid contrast on primary background',
      (textKey) => {
        const textColor = ColorSystem.textColors[textKey]
        const bgColor = ColorSystem.backgrounds['bg-primary']

        const contrastRatio = calculateContrastRatio(textColor, bgColor)

        // All text colors should have at least some contrast
        expect(contrastRatio).toBeGreaterThan(1)

        // Primary and regular text should meet AA standards
        if (textKey === 'text-primary' || textKey === 'text-regular') {
          expect(contrastRatio).toBeGreaterThanOrEqual(4.5)
        }
      }
    )

    /**
     * 测试对比度的对称性
     */
    test.prop([
      fc.constantFrom(...textColorKeys),
      fc.constantFrom(...backgroundKeys)
    ], { numRuns: 100 })(
      'Contrast ratio should be symmetric (text on bg = bg on text)',
      (textKey, bgKey) => {
        const textColor = ColorSystem.textColors[textKey]
        const bgColor = ColorSystem.backgrounds[bgKey]

        // Skip semi-transparent backgrounds for this test
        if (bgColor.includes('rgba')) return

        const ratio1 = calculateContrastRatio(textColor, bgColor)
        const ratio2 = calculateContrastRatio(bgColor, textColor)

        // Contrast ratio should be the same regardless of which is foreground/background
        expect(ratio1).toBeCloseTo(ratio2, 2)
      }
    )
  })

  /**
   * 视觉层次相关的额外测试
   */
  describe('Visual Hierarchy Consistency', () => {
    it('Text colors should form a visual hierarchy (primary > regular > secondary > placeholder)', () => {
      const bgColor = ColorSystem.backgrounds['bg-primary']

      const primaryContrast = calculateContrastRatio(ColorSystem.textColors['text-primary'], bgColor)
      const regularContrast = calculateContrastRatio(ColorSystem.textColors['text-regular'], bgColor)
      const secondaryContrast = calculateContrastRatio(ColorSystem.textColors['text-secondary'], bgColor)
      const placeholderContrast = calculateContrastRatio(ColorSystem.textColors['text-placeholder'], bgColor)

      // Primary should have highest contrast
      expect(primaryContrast).toBeGreaterThanOrEqual(regularContrast)
      // Regular should have higher contrast than secondary
      expect(regularContrast).toBeGreaterThanOrEqual(secondaryContrast)
      // Secondary should have higher contrast than placeholder
      expect(secondaryContrast).toBeGreaterThanOrEqual(placeholderContrast)
    })

    it('All theme colors should be distinguishable from each other', () => {
      const themeColorValues = Object.values(ColorSystem.themeColors)

      for (let i = 0; i < themeColorValues.length; i++) {
        for (let j = i + 1; j < themeColorValues.length; j++) {
          const color1 = parseColor(themeColorValues[i])
          const color2 = parseColor(themeColorValues[j])

          // Calculate color difference (simple Euclidean distance in RGB space)
          const diff = Math.sqrt(
            Math.pow(color1.r - color2.r, 2) +
            Math.pow(color1.g - color2.g, 2) +
            Math.pow(color1.b - color2.b, 2)
          )

          // Colors should be sufficiently different (at least 50 units in RGB space)
          expect(diff).toBeGreaterThan(50)
        }
      }
    })
  })
})
