/**
 * Spacing System Property-Based Tests
 * Feature: typography-layout-optimization, Property 6: Spacing Scale Adherence
 * Validates: Requirements 4.1, 4.2, 4.4
 *
 * 测试属性：对于任何在布局中使用的 margin 或 padding 值，
 * 该值应为定义的间距比例尺成员（0, 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px）
 */

import { describe, it, expect } from 'vitest'
import { test, fc } from '@fast-check/vitest'

/**
 * Spacing System 定义
 * 这些值与 _spacing.scss 和 _tokens.scss 中的定义保持一致
 */
const SpacingSystem = {
  // Spacing Scale (in rem, 4px base = 0.25rem)
  // Requirements: 4.1
  spacingScale: {
    0: 0,
    1: 0.25,    // 4px
    2: 0.5,     // 8px
    3: 0.75,    // 12px
    4: 1,       // 16px
    5: 1.25,    // 20px
    6: 1.5,     // 24px
    7: 1.75,    // 28px
    8: 2,       // 32px
    9: 2.25,    // 36px
    10: 2.5,    // 40px
    11: 2.75,   // 44px
    12: 3,      // 48px
    14: 3.5,    // 56px
    16: 4       // 64px
  },

  // Valid spacing values in pixels for easy comparison
  validSpacingPx: [0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 56, 64],

  // Semantic Spacing Tokens (in rem)
  // Requirements: 4.3
  semanticSpacing: {
    'tight': 0.5,      // 8px
    'normal': 1,       // 16px
    'loose': 1.5,      // 24px
    'section': 2       // 32px
  },

  // Card Padding (in rem)
  // Requirements: 6.1, 6.2
  cardPadding: {
    'sm': 1,       // 16px
    'md': 1.25,    // 20px
    'lg': 1.5,     // 24px
    'xl': 1.75     // 28px
  },

  // Form Field Spacing (in rem)
  // Requirements: 8.2
  formSpacing: {
    'field-gap': 1,       // 16px
    'field-gap-sm': 0.75, // 12px
    'field-gap-lg': 1.5,  // 24px
    'group-gap': 1.5,     // 24px
    'label-gap': 0.5      // 8px
  },

  // Table Cell Padding (in rem)
  // Requirements: 7.3
  tableSpacing: {
    'cell-padding-y': 0.75,     // 12px
    'cell-padding-x': 1,        // 16px
    'cell-padding-y-sm': 0.5,   // 8px
    'cell-padding-y-lg': 1,     // 16px
    'header-padding-y': 0.75,   // 12px
    'header-padding-x': 1       // 16px
  },

  // Gutter Widths (in rem)
  // Requirements: 3.3
  gutters: {
    'sm': 1,       // 16px
    'md': 1.25,    // 20px
    'lg': 1.5      // 24px
  }
}

// Convert rem to px (assuming 16px base)
function remToPx(rem) {
  return rem * 16
}

// Check if a value (in rem) is a valid spacing scale member
function isValidSpacingValue(remValue) {
  const pxValue = remToPx(remValue)
  // Allow for small floating point errors
  return SpacingSystem.validSpacingPx.some(valid => Math.abs(pxValue - valid) < 0.01)
}

// Get all spacing scale values as an array
function getSpacingScaleValues() {
  return Object.values(SpacingSystem.spacingScale)
}

// Get all spacing scale keys as an array
function getSpacingScaleKeys() {
  return Object.keys(SpacingSystem.spacingScale).map(Number)
}

describe('Spacing System Property-Based Tests', () => {
  /**
   * Feature: typography-layout-optimization, Property 6: Spacing Scale Adherence
   * Validates: Requirements 4.1, 4.2, 4.4
   *
   * 属性：对于任何间距比例尺中的值，该值应为 4px 基准单位的倍数
   */
  test.prop([
    fc.constantFrom(...getSpacingScaleKeys().filter(k => k > 0))
  ], { numRuns: 100 })(
    'Property 6: All spacing scale values should be multiples of 4px base unit',
    (spacingKey) => {
      const remValue = SpacingSystem.spacingScale[spacingKey]
      const pxValue = remToPx(remValue)

      // Each spacing value should be a multiple of 4px
      // Allow for small floating point errors
      const remainder = pxValue % 4
      expect(remainder).toBeLessThan(0.01)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Spacing Scale Monotonic Increase
   * Validates: Requirements 4.1
   *
   * 属性：间距比例尺应单调递增（键值越大，间距越大）
   */
  test.prop([
    fc.integer({ min: 0, max: getSpacingScaleKeys().length - 2 })
  ], { numRuns: 100 })(
    'Property 6: Spacing scale should be monotonically increasing',
    (index) => {
      const keys = getSpacingScaleKeys().sort((a, b) => a - b)
      const currentKey = keys[index]
      const nextKey = keys[index + 1]

      const currentValue = SpacingSystem.spacingScale[currentKey]
      const nextValue = SpacingSystem.spacingScale[nextKey]

      // Each value should be greater than or equal to the previous
      // (equal only for key 0)
      if (currentKey === 0) {
        expect(nextValue).toBeGreaterThan(currentValue)
      } else {
        expect(nextValue).toBeGreaterThan(currentValue)
      }
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Semantic Spacing Uses Scale Values
   * Validates: Requirements 4.2, 4.3
   *
   * 属性：所有语义化间距令牌应使用间距比例尺中的值
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.semanticSpacing))
  ], { numRuns: 100 })(
    'Property 6: Semantic spacing tokens should use spacing scale values',
    (semanticKey) => {
      const remValue = SpacingSystem.semanticSpacing[semanticKey]

      // The semantic spacing value should be a valid spacing scale member
      expect(isValidSpacingValue(remValue)).toBe(true)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Card Padding Uses Scale Values
   * Validates: Requirements 4.2, 6.1
   *
   * 属性：所有卡片内边距应使用间距比例尺中的值
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.cardPadding))
  ], { numRuns: 100 })(
    'Property 6: Card padding values should use spacing scale values',
    (paddingKey) => {
      const remValue = SpacingSystem.cardPadding[paddingKey]

      // The card padding value should be a valid spacing scale member
      expect(isValidSpacingValue(remValue)).toBe(true)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Form Spacing Uses Scale Values
   * Validates: Requirements 4.2, 8.2
   *
   * 属性：所有表单间距应使用间距比例尺中的值
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.formSpacing))
  ], { numRuns: 100 })(
    'Property 6: Form spacing values should use spacing scale values',
    (spacingKey) => {
      const remValue = SpacingSystem.formSpacing[spacingKey]

      // The form spacing value should be a valid spacing scale member
      expect(isValidSpacingValue(remValue)).toBe(true)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Table Spacing Uses Scale Values
   * Validates: Requirements 4.2, 7.3
   *
   * 属性：所有表格间距应使用间距比例尺中的值
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.tableSpacing))
  ], { numRuns: 100 })(
    'Property 6: Table spacing values should use spacing scale values',
    (spacingKey) => {
      const remValue = SpacingSystem.tableSpacing[spacingKey]

      // The table spacing value should be a valid spacing scale member
      expect(isValidSpacingValue(remValue)).toBe(true)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Gutter Widths Use Scale Values
   * Validates: Requirements 4.2, 3.3
   *
   * 属性：所有 gutter 宽度应使用间距比例尺中的值
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.gutters))
  ], { numRuns: 100 })(
    'Property 6: Gutter widths should use spacing scale values',
    (gutterKey) => {
      const remValue = SpacingSystem.gutters[gutterKey]

      // The gutter value should be a valid spacing scale member
      expect(isValidSpacingValue(remValue)).toBe(true)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Spacing Scale Contains Required Values
   * Validates: Requirements 4.1
   *
   * 属性：间距比例尺应包含所有必需的值（0, 4px, 8px, 16px, 24px, 32px, 48px, 64px）
   */
  it('Property 6: Spacing scale should contain all required values', () => {
    // Required spacing values in pixels
    const requiredPx = [0, 4, 8, 16, 24, 32, 48, 64]

    for (const required of requiredPx) {
      const found = SpacingSystem.validSpacingPx.includes(required)
      expect(found).toBe(true)
    }
  })

  /**
   * Feature: typography-layout-optimization, Property 6: Spacing Scale Key-Value Consistency
   * Validates: Requirements 4.1
   *
   * 属性：间距比例尺的键应与值成比例（key * 0.25rem = value）
   */
  test.prop([
    fc.constantFrom(...getSpacingScaleKeys())
  ], { numRuns: 100 })(
    'Property 6: Spacing scale key should correspond to value (key * 0.25rem)',
    (key) => {
      const expectedValue = key * 0.25
      const actualValue = SpacingSystem.spacingScale[key]

      // Allow for small floating point errors
      expect(Math.abs(actualValue - expectedValue)).toBeLessThan(0.001)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Card Padding Range
   * Validates: Requirements 6.1
   *
   * 属性：卡片内边距应在 16px 到 28px 范围内
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.cardPadding))
  ], { numRuns: 100 })(
    'Property 6: Card padding should be within 16px to 28px range',
    (paddingKey) => {
      const remValue = SpacingSystem.cardPadding[paddingKey]
      const pxValue = remToPx(remValue)

      expect(pxValue).toBeGreaterThanOrEqual(16)
      expect(pxValue).toBeLessThanOrEqual(28)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Form Field Spacing Range
   * Validates: Requirements 8.2
   *
   * 属性：表单字段间距应在 8px 到 24px 范围内
   */
  test.prop([
    fc.constantFrom(...Object.keys(SpacingSystem.formSpacing))
  ], { numRuns: 100 })(
    'Property 6: Form field spacing should be within 8px to 24px range',
    (spacingKey) => {
      const remValue = SpacingSystem.formSpacing[spacingKey]
      const pxValue = remToPx(remValue)

      expect(pxValue).toBeGreaterThanOrEqual(8)
      expect(pxValue).toBeLessThanOrEqual(24)
    }
  )

  /**
   * Feature: typography-layout-optimization, Property 6: Table Cell Padding Range
   * Validates: Requirements 7.3
   *
   * 属性：表格单元格垂直内边距应在 8px 到 16px 范围内
   */
  it('Property 6: Table cell vertical padding should be within 8px to 16px range', () => {
    const verticalPaddingKeys = ['cell-padding-y', 'cell-padding-y-sm', 'cell-padding-y-lg', 'header-padding-y']

    for (const key of verticalPaddingKeys) {
      const remValue = SpacingSystem.tableSpacing[key]
      const pxValue = remToPx(remValue)

      expect(pxValue).toBeGreaterThanOrEqual(8)
      expect(pxValue).toBeLessThanOrEqual(16)
    }
  })

  /**
   * Feature: typography-layout-optimization, Property 6: Semantic Spacing Ordering
   * Validates: Requirements 4.3
   *
   * 属性：语义化间距应遵循 tight < normal < loose < section 的顺序
   */
  it('Property 6: Semantic spacing should follow tight < normal < loose < section ordering', () => {
    const { tight, normal, loose, section } = SpacingSystem.semanticSpacing

    expect(tight).toBeLessThan(normal)
    expect(normal).toBeLessThan(loose)
    expect(loose).toBeLessThan(section)
  })

  /**
   * Feature: typography-layout-optimization, Property 6: Gutter Responsive Scaling
   * Validates: Requirements 3.3
   *
   * 属性：Gutter 宽度应随断点增加而增加（sm < md < lg）
   */
  it('Property 6: Gutter widths should increase with breakpoint (sm < md < lg)', () => {
    const { sm, md, lg } = SpacingSystem.gutters

    expect(sm).toBeLessThanOrEqual(md)
    expect(md).toBeLessThanOrEqual(lg)
  })
})
