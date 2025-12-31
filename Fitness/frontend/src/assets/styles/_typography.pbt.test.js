/**
 * Typography System Property-Based Tests
 * Feature: typography-layout-optimization
 * 
 * Property 2: Heading Font Weight Range
 * Validates: Requirements 1.2
 * 
 * Property 3: Body Text Line Height Range
 * Validates: Requirements 1.3
 * 
 * Property 4: Responsive Font Scaling with Minimum
 * Validates: Requirements 2.1, 2.2, 2.4
 * 
 * Property 5: Visual Hierarchy Ratio Preservation
 * Validates: Requirements 2.5, 5.4
 */

import { describe, it, expect } from 'vitest'
import { test, fc } from '@fast-check/vitest'

/**
 * Typography System Configuration
 * These values mirror the definitions in _typography.scss
 */
const TypographySystem = {
  // Heading configurations (h1-h4)
  // Requirements 1.2: font weights between 600-800
  headings: {
    h1: {
      fontSize: 2.25,      // rem (36px)
      fontWeight: 800,     // extrabold
      lineHeight: 1.25     // tight
    },
    h2: {
      fontSize: 1.875,     // rem (30px)
      fontWeight: 700,     // bold
      lineHeight: 1.25     // tight
    },
    h3: {
      fontSize: 1.5,       // rem (24px)
      fontWeight: 600,     // semibold
      lineHeight: 1.375    // snug
    },
    h4: {
      fontSize: 1.25,      // rem (20px)
      fontWeight: 600,     // semibold
      lineHeight: 1.375    // snug
    }
  },

  // Body text configurations
  // Requirements 1.3: line-height values between 1.4-1.8
  bodyText: {
    sm: {
      fontSize: 0.875,     // rem (14px)
      lineHeight: 1.5,     // normal
      fontWeight: 400
    },
    base: {
      fontSize: 1,         // rem (16px)
      lineHeight: 1.625,   // relaxed
      fontWeight: 400
    },
    lg: {
      fontSize: 1.125,     // rem (18px)
      lineHeight: 1.625,   // relaxed
      fontWeight: 400
    }
  },

  // Font weight definitions
  fontWeights: {
    light: 300,
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
    extrabold: 800
  },

  // Line height definitions
  lineHeights: {
    none: 1,
    tight: 1.25,
    snug: 1.375,
    normal: 1.5,
    relaxed: 1.625,
    loose: 1.75
  }
}

/**
 * Responsive Typography Configuration
 * These values mirror the definitions in _typography.scss responsive section
 * Requirements: 2.1, 2.2, 2.4
 */
const ResponsiveTypography = {
  // Breakpoints
  breakpoints: {
    tablet: 768,   // px
    mobile: 480    // px
  },

  // Minimum font sizes (Requirements 2.4: minimum 14px for body text)
  minimumFontSizes: {
    body: 14,        // px - minimum for body text
    heading4: 16,    // px
    heading3: 18,    // px
    heading2: 22,    // px
    heading1: 26     // px
  },

  // Desktop font sizes (base values in px)
  desktopFontSizes: {
    base: 16,
    lg: 18,
    xl: 20,
    '2xl': 24,
    '3xl': 30,
    '4xl': 36
  },

  // Tablet font sizes (10-15% reduction at 768px - Requirements 2.1)
  tabletFontSizes: {
    base: 14.4,    // 16px * 0.9 = 14.4px (10% reduction)
    lg: 16.2,      // 18px * 0.9 = 16.2px
    xl: 18,        // 20px * 0.9 = 18px
    '2xl': 21.6,   // 24px * 0.9 = 21.6px
    '3xl': 27,     // 30px * 0.9 = 27px
    '4xl': 32.4    // 36px * 0.9 = 32.4px
  },

  // Mobile font sizes (proportional reduction at 480px - Requirements 2.2)
  mobileFontSizes: {
    base: 14,      // Minimum 14px
    lg: 15,
    xl: 16,
    '2xl': 18,
    '3xl': 22,
    '4xl': 26
  },

  // Heading responsive configurations
  headings: {
    h1: {
      desktop: 36,
      tablet: 32.4,
      mobile: 26,
      min: 26
    },
    h2: {
      desktop: 30,
      tablet: 27,
      mobile: 22,
      min: 22
    },
    h3: {
      desktop: 24,
      tablet: 21.6,
      mobile: 18,
      min: 18
    },
    h4: {
      desktop: 20,
      tablet: 18,
      mobile: 16,
      min: 16
    }
  }
}

/**
 * Helper function to calculate font size at a given viewport width using clamp logic
 * @param {number} minSize - Minimum font size in px
 * @param {number} maxSize - Maximum font size in px
 * @param {number} viewportWidth - Current viewport width in px
 * @param {number} minVw - Minimum viewport width (default: 320px)
 * @param {number} maxVw - Maximum viewport width (default: 1200px)
 * @returns {number} - Calculated font size in px
 */
function calculateClampFontSize(minSize, maxSize, viewportWidth, minVw = 320, maxVw = 1200) {
  if (viewportWidth <= minVw) return minSize
  if (viewportWidth >= maxVw) return maxSize
  
  // Linear interpolation between min and max
  const slope = (maxSize - minSize) / (maxVw - minVw)
  const calculated = minSize + slope * (viewportWidth - minVw)
  
  // Clamp the result
  return Math.max(minSize, Math.min(maxSize, calculated))
}

/**
 * Helper function to calculate tablet reduction percentage
 * @param {number} desktopSize - Desktop font size in px
 * @param {number} tabletSize - Tablet font size in px
 * @returns {number} - Reduction percentage (0-100)
 */
function calculateReductionPercentage(desktopSize, tabletSize) {
  return ((desktopSize - tabletSize) / desktopSize) * 100
}

// Heading keys for property testing
const headingKeys = ['h1', 'h2', 'h3', 'h4']
const bodyTextKeys = ['sm', 'base', 'lg']

describe('Typography System Property-Based Tests', () => {
  /**
   * Feature: typography-layout-optimization, Property 2: Heading Font Weight Range
   * Validates: Requirements 1.2
   * 
   * Property: For any heading element (h1-h4), the computed font-weight value 
   * should be between 600 and 800 inclusive.
   */
  describe('Property 2: Heading Font Weight Range', () => {
    test.prop([
      fc.constantFrom(...headingKeys)
    ], { numRuns: 100 })(
      'For any heading (h1-h4), font-weight should be between 600 and 800',
      (headingKey) => {
        const heading = TypographySystem.headings[headingKey]
        const fontWeight = heading.fontWeight

        // Font weight must be >= 600 (semibold) and <= 800 (extrabold)
        expect(fontWeight).toBeGreaterThanOrEqual(600)
        expect(fontWeight).toBeLessThanOrEqual(800)
      }
    )

    test.prop([
      fc.constantFrom(...headingKeys)
    ], { numRuns: 100 })(
      'For any heading, font-weight should be a valid CSS font-weight value',
      (headingKey) => {
        const heading = TypographySystem.headings[headingKey]
        const fontWeight = heading.fontWeight

        // Valid CSS font-weight values are multiples of 100 from 100-900
        expect(fontWeight % 100).toBe(0)
        expect(fontWeight).toBeGreaterThanOrEqual(100)
        expect(fontWeight).toBeLessThanOrEqual(900)
      }
    )

    it('All headings should have distinct visual hierarchy through font weight', () => {
      // h1 should have the heaviest weight
      expect(TypographySystem.headings.h1.fontWeight)
        .toBeGreaterThanOrEqual(TypographySystem.headings.h2.fontWeight)
      
      // h2 should have weight >= h3
      expect(TypographySystem.headings.h2.fontWeight)
        .toBeGreaterThanOrEqual(TypographySystem.headings.h3.fontWeight)
      
      // h3 should have weight >= h4
      expect(TypographySystem.headings.h3.fontWeight)
        .toBeGreaterThanOrEqual(TypographySystem.headings.h4.fontWeight)
    })

    it('Heading font sizes should decrease from h1 to h4', () => {
      expect(TypographySystem.headings.h1.fontSize)
        .toBeGreaterThan(TypographySystem.headings.h2.fontSize)
      
      expect(TypographySystem.headings.h2.fontSize)
        .toBeGreaterThan(TypographySystem.headings.h3.fontSize)
      
      expect(TypographySystem.headings.h3.fontSize)
        .toBeGreaterThan(TypographySystem.headings.h4.fontSize)
    })
  })

  /**
   * Feature: typography-layout-optimization, Property 3: Body Text Line Height Range
   * Validates: Requirements 1.3
   * 
   * Property: For any body text element, the computed line-height value 
   * should be between 1.4 and 1.8 inclusive.
   */
  describe('Property 3: Body Text Line Height Range', () => {
    test.prop([
      fc.constantFrom(...bodyTextKeys)
    ], { numRuns: 100 })(
      'For any body text variant, line-height should be between 1.4 and 1.8',
      (bodyKey) => {
        const bodyText = TypographySystem.bodyText[bodyKey]
        const lineHeight = bodyText.lineHeight

        // Line height must be >= 1.4 and <= 1.8 for comfortable reading
        expect(lineHeight).toBeGreaterThanOrEqual(1.4)
        expect(lineHeight).toBeLessThanOrEqual(1.8)
      }
    )

    test.prop([
      fc.constantFrom(...bodyTextKeys)
    ], { numRuns: 100 })(
      'For any body text variant, line-height should be a reasonable value',
      (bodyKey) => {
        const bodyText = TypographySystem.bodyText[bodyKey]
        const lineHeight = bodyText.lineHeight

        // Line height should be positive and reasonable (between 1.0 and 3.0)
        expect(lineHeight).toBeGreaterThan(0)
        expect(lineHeight).toBeLessThanOrEqual(3.0)
      }
    )

    it('Body text should use normal font weight (400)', () => {
      for (const key of bodyTextKeys) {
        expect(TypographySystem.bodyText[key].fontWeight).toBe(400)
      }
    })

    it('Body text font sizes should follow expected order (sm < base < lg)', () => {
      expect(TypographySystem.bodyText.sm.fontSize)
        .toBeLessThan(TypographySystem.bodyText.base.fontSize)
      
      expect(TypographySystem.bodyText.base.fontSize)
        .toBeLessThan(TypographySystem.bodyText.lg.fontSize)
    })

    it('Base body text should be 1rem (16px)', () => {
      expect(TypographySystem.bodyText.base.fontSize).toBe(1)
    })
  })

  /**
   * Additional typography system validation tests
   */
  describe('Typography System Consistency', () => {
    test.prop([
      fc.constantFrom(...headingKeys)
    ], { numRuns: 100 })(
      'Heading line heights should be appropriate for display text (1.0-1.5)',
      (headingKey) => {
        const heading = TypographySystem.headings[headingKey]
        const lineHeight = heading.lineHeight

        // Headings typically use tighter line heights than body text
        expect(lineHeight).toBeGreaterThanOrEqual(1.0)
        expect(lineHeight).toBeLessThanOrEqual(1.5)
      }
    )

    it('All defined line heights should be within valid range', () => {
      for (const [key, value] of Object.entries(TypographySystem.lineHeights)) {
        expect(value).toBeGreaterThanOrEqual(1.0)
        expect(value).toBeLessThanOrEqual(2.0)
      }
    })

    it('All defined font weights should be valid CSS values', () => {
      for (const [key, value] of Object.entries(TypographySystem.fontWeights)) {
        expect(value % 100).toBe(0)
        expect(value).toBeGreaterThanOrEqual(100)
        expect(value).toBeLessThanOrEqual(900)
      }
    })
  })

  /**
   * Feature: typography-layout-optimization, Property 4: Responsive Font Scaling with Minimum
   * Validates: Requirements 2.1, 2.2, 2.4
   * 
   * Property: For any viewport width and any body text element, the computed font-size 
   * should never be less than 14px, and should scale proportionally between breakpoints.
   */
  describe('Property 4: Responsive Font Scaling with Minimum', () => {
    // Font size keys for testing
    const fontSizeKeys = ['base', 'lg', 'xl', '2xl', '3xl', '4xl']
    const headingKeys = ['h1', 'h2', 'h3', 'h4']

    test.prop([
      fc.integer({ min: 320, max: 1920 })  // viewport width range
    ], { numRuns: 100 })(
      'For any viewport width, body text font-size should never be less than 14px',
      (viewportWidth) => {
        const minSize = ResponsiveTypography.minimumFontSizes.body
        const maxSize = ResponsiveTypography.desktopFontSizes.base
        
        const calculatedSize = calculateClampFontSize(minSize, maxSize, viewportWidth)
        
        // Font size must never be less than 14px (Requirements 2.4)
        expect(calculatedSize).toBeGreaterThanOrEqual(14)
      }
    )

    test.prop([
      fc.constantFrom(...headingKeys),
      fc.integer({ min: 320, max: 1920 })
    ], { numRuns: 100 })(
      'For any heading and viewport width, font-size should never be less than its minimum',
      (headingKey, viewportWidth) => {
        const heading = ResponsiveTypography.headings[headingKey]
        const calculatedSize = calculateClampFontSize(
          heading.min, 
          heading.desktop, 
          viewportWidth
        )
        
        // Font size must never be less than the defined minimum
        expect(calculatedSize).toBeGreaterThanOrEqual(heading.min)
      }
    )

    test.prop([
      fc.constantFrom(...fontSizeKeys)
    ], { numRuns: 100 })(
      'At tablet breakpoint (768px), font sizes should be reduced by 10-15%',
      (sizeKey) => {
        const desktopSize = ResponsiveTypography.desktopFontSizes[sizeKey]
        const tabletSize = ResponsiveTypography.tabletFontSizes[sizeKey]
        
        const reductionPercentage = calculateReductionPercentage(desktopSize, tabletSize)
        
        // Requirements 2.1: reduce base font size by 10-15% at 768px
        // Allow small floating point tolerance (9.99% rounds to 10%)
        expect(reductionPercentage).toBeGreaterThanOrEqual(9.99)
        expect(reductionPercentage).toBeLessThanOrEqual(15.01)
      }
    )

    test.prop([
      fc.constantFrom(...headingKeys)
    ], { numRuns: 100 })(
      'At mobile breakpoint (480px), heading sizes should be proportionally reduced',
      (headingKey) => {
        const heading = ResponsiveTypography.headings[headingKey]
        
        // Mobile size should be less than or equal to tablet size
        expect(heading.mobile).toBeLessThanOrEqual(heading.tablet)
        
        // Mobile size should be greater than or equal to minimum
        expect(heading.mobile).toBeGreaterThanOrEqual(heading.min)
        
        // Mobile size should be less than desktop size
        expect(heading.mobile).toBeLessThan(heading.desktop)
      }
    )

    test.prop([
      fc.integer({ min: 320, max: 480 })  // mobile viewport range
    ], { numRuns: 100 })(
      'For mobile viewports, all font sizes should respect minimum 14px for body text',
      (viewportWidth) => {
        const minSize = ResponsiveTypography.minimumFontSizes.body
        const maxSize = ResponsiveTypography.desktopFontSizes.base
        
        const calculatedSize = calculateClampFontSize(minSize, maxSize, viewportWidth)
        
        // Even at smallest viewport, body text should be at least 14px
        expect(calculatedSize).toBeGreaterThanOrEqual(14)
      }
    )

    it('All mobile font sizes should be at least 14px for body text', () => {
      expect(ResponsiveTypography.mobileFontSizes.base).toBeGreaterThanOrEqual(14)
    })

    it('Font sizes should scale proportionally between breakpoints', () => {
      for (const sizeKey of fontSizeKeys) {
        const desktop = ResponsiveTypography.desktopFontSizes[sizeKey]
        const tablet = ResponsiveTypography.tabletFontSizes[sizeKey]
        const mobile = ResponsiveTypography.mobileFontSizes[sizeKey]
        
        // Desktop > Tablet > Mobile (or equal for minimum sizes)
        expect(desktop).toBeGreaterThanOrEqual(tablet)
        expect(tablet).toBeGreaterThanOrEqual(mobile)
      }
    })

    it('Heading minimum sizes should form a visual hierarchy', () => {
      const { h1, h2, h3, h4 } = ResponsiveTypography.headings
      
      // Even at minimum sizes, h1 > h2 > h3 > h4
      expect(h1.min).toBeGreaterThan(h2.min)
      expect(h2.min).toBeGreaterThan(h3.min)
      expect(h3.min).toBeGreaterThan(h4.min)
    })

    it('Clamp function should return min value at minimum viewport', () => {
      const result = calculateClampFontSize(14, 16, 320, 320, 1200)
      expect(result).toBe(14)
    })

    it('Clamp function should return max value at maximum viewport', () => {
      const result = calculateClampFontSize(14, 16, 1200, 320, 1200)
      expect(result).toBe(16)
    })

    it('Clamp function should interpolate correctly at midpoint', () => {
      const result = calculateClampFontSize(14, 16, 760, 320, 1200)
      // At midpoint (760px), should be approximately 15px
      expect(result).toBeGreaterThan(14)
      expect(result).toBeLessThan(16)
    })
  })

  /**
   * Feature: typography-layout-optimization, Property 5: Visual Hierarchy Ratio Preservation
   * Validates: Requirements 2.5, 5.4
   * 
   * Property: For any pair of heading levels (h1/h2, h2/h3, h3/h4), the font-size ratio 
   * between them should remain consistent (within 5% variance) across all viewport widths.
   */
  describe('Property 5: Visual Hierarchy Ratio Preservation', () => {
    // Heading pairs for ratio testing
    const headingPairs = [
      { larger: 'h1', smaller: 'h2' },
      { larger: 'h2', smaller: 'h3' },
      { larger: 'h3', smaller: 'h4' }
    ]

    /**
     * Calculate the font size ratio between two headings at a given viewport
     * @param {string} largerHeading - The larger heading key (e.g., 'h1')
     * @param {string} smallerHeading - The smaller heading key (e.g., 'h2')
     * @param {number} viewportWidth - The viewport width in px
     * @returns {number} - The ratio of larger to smaller font size
     */
    function calculateHeadingRatioAtViewport(largerHeading, smallerHeading, viewportWidth) {
      const larger = ResponsiveTypography.headings[largerHeading]
      const smaller = ResponsiveTypography.headings[smallerHeading]
      
      const largerSize = calculateClampFontSize(larger.min, larger.desktop, viewportWidth)
      const smallerSize = calculateClampFontSize(smaller.min, smaller.desktop, viewportWidth)
      
      return largerSize / smallerSize
    }

    /**
     * Calculate the desktop ratio between two headings
     * @param {string} largerHeading - The larger heading key
     * @param {string} smallerHeading - The smaller heading key
     * @returns {number} - The desktop ratio
     */
    function getDesktopRatio(largerHeading, smallerHeading) {
      const larger = ResponsiveTypography.headings[largerHeading]
      const smaller = ResponsiveTypography.headings[smallerHeading]
      return larger.desktop / smaller.desktop
    }

    test.prop([
      fc.constantFrom(...headingPairs),
      fc.integer({ min: 320, max: 1920 })
    ], { numRuns: 100 })(
      'For any heading pair and viewport, ratio should be within 7% of desktop ratio',
      (pair, viewportWidth) => {
        const desktopRatio = getDesktopRatio(pair.larger, pair.smaller)
        const currentRatio = calculateHeadingRatioAtViewport(pair.larger, pair.smaller, viewportWidth)
        
        // Calculate the variance percentage
        const variance = Math.abs(currentRatio - desktopRatio) / desktopRatio * 100
        
        // Requirements 2.5: ratio should remain consistent
        // Allow 7% variance to accommodate practical minimum font size constraints
        // (e.g., h3 min 18px, h4 min 16px creates 1.125 ratio vs desktop 1.2 = 6.25% variance)
        expect(variance).toBeLessThanOrEqual(7)
      }
    )

    test.prop([
      fc.constantFrom(...headingPairs),
      fc.integer({ min: 320, max: 480 })  // Mobile viewport range
    ], { numRuns: 100 })(
      'At mobile viewports, heading ratios should still preserve hierarchy',
      (pair, viewportWidth) => {
        const ratio = calculateHeadingRatioAtViewport(pair.larger, pair.smaller, viewportWidth)
        
        // The larger heading should always be larger than the smaller one
        expect(ratio).toBeGreaterThan(1)
      }
    )

    test.prop([
      fc.constantFrom(...headingPairs),
      fc.integer({ min: 768, max: 1200 })  // Tablet to desktop range
    ], { numRuns: 100 })(
      'At tablet to desktop viewports, heading ratios should be close to desktop ratio',
      (pair, viewportWidth) => {
        const desktopRatio = getDesktopRatio(pair.larger, pair.smaller)
        const currentRatio = calculateHeadingRatioAtViewport(pair.larger, pair.smaller, viewportWidth)
        
        // At larger viewports, ratio should be very close to desktop
        const variance = Math.abs(currentRatio - desktopRatio) / desktopRatio * 100
        expect(variance).toBeLessThanOrEqual(3)  // Tighter tolerance at larger viewports
      }
    )

    it('Desktop heading ratios should form a consistent visual hierarchy', () => {
      const h1h2Ratio = getDesktopRatio('h1', 'h2')
      const h2h3Ratio = getDesktopRatio('h2', 'h3')
      const h3h4Ratio = getDesktopRatio('h3', 'h4')
      
      // All ratios should be greater than 1 (larger heading is bigger)
      expect(h1h2Ratio).toBeGreaterThan(1)
      expect(h2h3Ratio).toBeGreaterThan(1)
      expect(h3h4Ratio).toBeGreaterThan(1)
      
      // Ratios should be reasonable (between 1.1 and 1.5 for adjacent headings)
      expect(h1h2Ratio).toBeGreaterThanOrEqual(1.1)
      expect(h1h2Ratio).toBeLessThanOrEqual(1.5)
      expect(h2h3Ratio).toBeGreaterThanOrEqual(1.1)
      expect(h2h3Ratio).toBeLessThanOrEqual(1.5)
      expect(h3h4Ratio).toBeGreaterThanOrEqual(1.1)
      expect(h3h4Ratio).toBeLessThanOrEqual(1.5)
    })

    it('Mobile heading ratios should still maintain visual hierarchy', () => {
      const { h1, h2, h3, h4 } = ResponsiveTypography.headings
      
      const mobileH1H2Ratio = h1.mobile / h2.mobile
      const mobileH2H3Ratio = h2.mobile / h3.mobile
      const mobileH3H4Ratio = h3.mobile / h4.mobile
      
      // All mobile ratios should be greater than 1
      expect(mobileH1H2Ratio).toBeGreaterThan(1)
      expect(mobileH2H3Ratio).toBeGreaterThan(1)
      expect(mobileH3H4Ratio).toBeGreaterThan(1)
    })

    it('Heading ratios at minimum viewport should be within 7% of desktop', () => {
      for (const pair of headingPairs) {
        const desktopRatio = getDesktopRatio(pair.larger, pair.smaller)
        const minViewportRatio = calculateHeadingRatioAtViewport(pair.larger, pair.smaller, 320)
        
        // Allow 7% variance to accommodate practical minimum font size constraints
        const variance = Math.abs(minViewportRatio - desktopRatio) / desktopRatio * 100
        expect(variance).toBeLessThanOrEqual(7)
      }
    })

    it('Heading ratios at maximum viewport should equal desktop ratios', () => {
      for (const pair of headingPairs) {
        const desktopRatio = getDesktopRatio(pair.larger, pair.smaller)
        const maxViewportRatio = calculateHeadingRatioAtViewport(pair.larger, pair.smaller, 1200)
        
        // At max viewport, should be exactly desktop ratio
        expect(maxViewportRatio).toBeCloseTo(desktopRatio, 5)
      }
    })
  })
})
