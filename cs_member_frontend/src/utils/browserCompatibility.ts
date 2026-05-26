/**
 * 浏览器兼容性检测工具
 * 检测浏览器特性支持情况并提供兼容性建议
 */

export interface BrowserInfo {
  name: string
  version: string
  engine: string
  platform: string
  mobile: boolean
  tablet: boolean
}

export interface FeatureSupport {
  cssVariables: boolean
  grid: boolean
  flexbox: boolean
  backdropFilter: boolean
  filter: boolean
  mixBlendMode: boolean
  transform3d: boolean
  webp: boolean
  avif: boolean
  intersectionObserver: boolean
  resizeObserver: boolean
  customElements: boolean
  shadowDom: boolean
  es6: boolean
  wasm: boolean
  webgl: boolean
  webgl2: boolean
}

export interface CompatibilityReport {
  browser: BrowserInfo
  features: FeatureSupport
  performance: PerformanceInfo
  recommendations: string[]
  compatible: boolean
  warnings: string[]
  errors: string[]
}

export interface PerformanceInfo {
  memory: number | null
  cores: number
  connection: NetworkInformation | null
  devicePixelRatio: number
  touchSupport: boolean
  gpuAcceleration: boolean
}

export class BrowserCompatibility {
  private static instance: BrowserCompatibility
  private cachedReport: CompatibilityReport | null = null

  public static getInstance(): BrowserCompatibility {
    if (!BrowserCompatibility.instance) {
      BrowserCompatibility.instance = new BrowserCompatibility()
    }
    return BrowserCompatibility.instance
  }

  /**
   * 获取完整的兼容性报告
   */
  public async getCompatibilityReport(): Promise<CompatibilityReport> {
    if (this.cachedReport) {
      return this.cachedReport
    }

    const browser = this.getBrowserInfo()
    const features = await this.detectFeatures()
    const performance = this.getPerformanceInfo()
    const recommendations = this.generateRecommendations(browser, features, performance)
    const { warnings, errors } = this.generateIssues(browser, features)
    const compatible = errors.length === 0

    this.cachedReport = {
      browser,
      features,
      performance,
      recommendations,
      compatible,
      warnings,
      errors
    }

    return this.cachedReport
  }

  /**
   * 获取浏览器信息
   */
  private getBrowserInfo(): BrowserInfo {
    const ua = navigator.userAgent
    const platform = navigator.platform

    // 检测浏览器名称和版本
    let name = 'Unknown'
    let version = ''
    let engine = ''

    // Chrome/Edge (Blink)
    if (ua.includes('Chrome/') && !ua.includes('Edg/')) {
      name = 'Chrome'
      version = ua.match(/Chrome\/(\d+\.\d+)/)?.[1] || ''
      engine = 'Blink'
    } else if (ua.includes('Edg/')) {
      name = 'Edge'
      version = ua.match(/Edg\/(\d+\.\d+)/)?.[1] || ''
      engine = 'Blink'
    }
    // Firefox (Gecko)
    else if (ua.includes('Firefox/')) {
      name = 'Firefox'
      version = ua.match(/Firefox\/(\d+\.\d+)/)?.[1] || ''
      engine = 'Gecko'
    }
    // Safari (WebKit)
    else if (ua.includes('Safari/') && !ua.includes('Chrome')) {
      name = 'Safari'
      version = ua.match(/Version\/(\d+\.\d+)/)?.[1] || ''
      engine = 'WebKit'
    }

    // 检测设备类型
    const mobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua)
    const tablet = /iPad|Android(?!.*Mobile)/i.test(ua)

    return {
      name,
      version,
      engine,
      platform,
      mobile,
      tablet
    }
  }

  /**
   * 检测特性支持
   */
  private async detectFeatures(): Promise<FeatureSupport> {
    return {
      cssVariables: this.testCSSVariables(),
      grid: this.testCSSGrid(),
      flexbox: this.testFlexbox(),
      backdropFilter: await this.testBackdropFilter(),
      filter: await this.testFilter(),
      mixBlendMode: this.testMixBlendMode(),
      transform3d: this.testTransform3d(),
      webp: await this.testWebPSupport(),
      avif: await this.testAVIFSupport(),
      intersectionObserver: this.testIntersectionObserver(),
      resizeObserver: this.testResizeObserver(),
      customElements: this.testCustomElements(),
      shadowDom: this.testShadowDOM(),
      es6: this.testES6(),
      wasm: this.testWASM(),
      webgl: this.testWebGL(),
      webgl2: this.testWebGL2()
    }
  }

  /**
   * 获取性能信息
   */
  private getPerformanceInfo(): PerformanceInfo {
    return {
      memory: (navigator as any).deviceMemory || null,
      cores: navigator.hardwareConcurrency || 4,
      connection: (navigator as any).connection || null,
      devicePixelRatio: window.devicePixelRatio || 1,
      touchSupport: 'ontouchstart' in window,
      gpuAcceleration: this.testGPUAcceleration()
    }
  }

  /**
   * 生成兼容性建议
   */
  private generateRecommendations(
    browser: BrowserInfo,
    features: FeatureSupport,
    performance: PerformanceInfo
  ): string[] {
    const recommendations: string[] = []

    // 基于浏览器版本的建议
    if (browser.name === 'Chrome' && parseFloat(browser.version) < 88) {
      recommendations.push('建议升级Chrome到88+版本以获得更好的背景效果支持')
    }

    if (browser.name === 'Firefox' && parseFloat(browser.version) < 70) {
      recommendations.push('建议升级Firefox到70+版本以支持更多现代CSS特性')
    }

    if (browser.name === 'Safari' && parseFloat(browser.version) < 14) {
      recommendations.push('建议升级Safari到14+版本以支持backdrop-filter等特性')
    }

    // 基于特性支持的建议
    if (!features.backdropFilter) {
      recommendations.push('浏览器不支持backdrop-filter，将使用半透明背景作为回退')
    }

    if (!features.cssVariables) {
      recommendations.push('浏览器不支持CSS变量，将使用预设颜色值')
    }

    if (!features.grid) {
      recommendations.push('浏览器不支持CSS Grid，布局将使用Flexbox回退方案')
    }

    if (!features.webp && !features.avif) {
      recommendations.push('建议使用JPEG/PNG格式图片以确保兼容性')
    }

    // 基于性能的建议
    if (performance.memory && performance.memory < 4) {
      recommendations.push('设备内存较低，建议启用简化模式以提升性能')
    }

    if (performance.cores < 4) {
      recommendations.push('CPU核心数较少，建议减少动画复杂度')
    }

    if (performance.connection?.effectiveType === 'slow-2g' ||
        performance.connection?.effectiveType === '2g') {
      recommendations.push('网络连接较慢，建议启用数据节省模式')
    }

    return recommendations
  }

  /**
   * 生成警告和错误
   */
  private generateIssues(browser: BrowserInfo, features: FeatureSupport): {
    warnings: string[]
    errors: string[]
  } {
    const warnings: string[] = []
    const errors: string[] = []

    // 检测过时的浏览器
    if (browser.name === 'Internet Explorer') {
      errors.push('Internet Explorer不再受支持，请使用现代浏览器')
    }

    if (browser.name === 'Chrome' && parseFloat(browser.version) < 70) {
      errors.push('Chrome版本过低，请升级到70+版本')
    }

    if (browser.name === 'Firefox' && parseFloat(browser.version) < 60) {
      errors.push('Firefox版本过低，请升级到60+版本')
    }

    if (browser.name === 'Safari' && parseFloat(browser.version) < 12) {
      errors.push('Safari版本过低，请升级到12+版本')
    }

    // 检测关键特性缺失
    if (!features.cssVariables) {
      warnings.push('CSS变量不支持，部分视觉效果可能缺失')
    }

    if (!features.flexbox) {
      errors.push('Flexbox不支持，布局可能异常')
    }

    if (!features.transform3d) {
      warnings.push('3D Transform不支持，动画效果将简化')
    }

    return { warnings, errors }
  }

  /**
   * CSS变量测试
   */
  private testCSSVariables(): boolean {
    return CSS.supports('color', 'var(--test)')
  }

  /**
   * CSS Grid测试
   */
  private testCSSGrid(): boolean {
    return CSS.supports('display', 'grid')
  }

  /**
   * Flexbox测试
   */
  private testFlexbox(): boolean {
    return CSS.supports('display', 'flex')
  }

  /**
   * backdrop-filter测试
   */
  private async testBackdropFilter(): Promise<boolean> {
    // 创建测试元素
    const testElement = document.createElement('div')
    testElement.style.backdropFilter = 'blur(10px)'
    testElement.style.webkitBackdropFilter = 'blur(10px)'

    document.body.appendChild(testElement)

    // 获取计算样式
    const computedStyle = window.getComputedStyle(testElement)
    const backdropFilter = computedStyle.backdropFilter ||
                         computedStyle.webkitBackdropFilter

    document.body.removeChild(testElement)

    return backdropFilter !== 'none' && backdropFilter !== ''
  }

  /**
   * Filter测试
   */
  private async testFilter(): Promise<boolean> {
    return CSS.supports('filter', 'blur(10px)')
  }

  /**
   * Mix Blend Mode测试
   */
  private testMixBlendMode(): boolean {
    return CSS.supports('mix-blend-mode', 'multiply')
  }

  /**
   * 3D Transform测试
   */
  private testTransform3d(): boolean {
    return CSS.supports('transform', 'translate3d(0,0,0)')
  }

  /**
   * WebP支持测试
   */
  private async testWebPSupport(): Promise<boolean> {
    return new Promise((resolve) => {
      const webP = new Image()
      webP.onload = () => resolve(webP.width > 0 && webP.height > 0)
      webP.onerror = () => resolve(false)
      webP.src = 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA'
    })
  }

  /**
   * AVIF支持测试
   */
  private async testAVIFSupport(): Promise<boolean> {
    return new Promise((resolve) => {
      const avif = new Image()
      avif.onload = () => resolve(avif.width > 0 && avif.height > 0)
      avif.onerror = () => resolve(false)
      avif.src = 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAEAAAABAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgANogQEAwgMg8f8D///8WfhwB8+ErK42A='
    })
  }

  /**
   * Intersection Observer测试
   */
  private testIntersectionObserver(): boolean {
    return 'IntersectionObserver' in window
  }

  /**
   * Resize Observer测试
   */
  private testResizeObserver(): boolean {
    return 'ResizeObserver' in window
  }

  /**
   * Custom Elements测试
   */
  private testCustomElements(): boolean {
    return 'customElements' in window
  }

  /**
   * Shadow DOM测试
   */
  private testShadowDOM(): boolean {
    return 'attachShadow' in Element.prototype
  }

  /**
   * ES6支持测试
   */
  private testES6(): boolean {
    try {
      new Function('(a = 0) => a')
      return true
    } catch {
      return false
    }
  }

  /**
   * WebAssembly支持测试
   */
  private testWASM(): boolean {
    return typeof WebAssembly === 'object'
  }

  /**
   * WebGL支持测试
   */
  private testWebGL(): boolean {
    try {
      const canvas = document.createElement('canvas')
      return !!(canvas.getContext('webgl') || canvas.getContext('experimental-webgl'))
    } catch {
      return false
    }
  }

  /**
   * WebGL2支持测试
   */
  private testWebGL2(): boolean {
    try {
      const canvas = document.createElement('canvas')
      return !!canvas.getContext('webgl2')
    } catch {
      return false
    }
  }

  /**
   * GPU加速测试
   */
  private testGPUAcceleration(): boolean {
    try {
      const canvas = document.createElement('canvas')
      const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
      if (!gl) return false

      const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
      return !!debugInfo
    } catch {
      return false
    }
  }

  /**
   * 应用兼容性修复
   */
  public applyCompatibilityFixes(): void {
    const html = document.documentElement

    // 添加浏览器类名
    const browser = this.getBrowserInfo()
    html.classList.add(`browser-${browser.name.toLowerCase()}`)
    html.classList.add(`engine-${browser.engine.toLowerCase()}`)
    html.classList.add(browser.mobile ? 'mobile' : 'desktop')
    if (browser.tablet) html.classList.add('tablet')

    // 添加特性支持类名
    this.detectFeatures().then(features => {
      Object.entries(features).forEach(([feature, supported]) => {
        const className = supported ? `has-${feature}` : `no-${feature}`
        html.classList.add(className)
      })
    })

    // 应用性能优化类
    const performance = this.getPerformanceInfo()
    if (performance.memory && performance.memory < 4) {
      html.classList.add('low-memory')
    }
    if (performance.cores < 4) {
      html.classList.add('low-cpu')
    }
    if (performance.connection?.effectiveType === 'slow-2g' ||
        performance.connection?.effectiveType === '2g') {
      html.classList.add('slow-network')
    }
  }

  /**
   * 获取简化的兼容性评分
   */
  public async getCompatibilityScore(): Promise<number> {
    const report = await this.getCompatibilityReport()
    const totalFeatures = Object.keys(report.features).length
    const supportedFeatures = Object.values(report.features).filter(Boolean).length

    let score = (supportedFeatures / totalFeatures) * 100

    // 根据浏览器版本调整分数
    const majorVersion = parseInt(report.browser.version.split('.')[0])
    if (report.browser.name === 'Chrome' && majorVersion >= 88) score += 10
    if (report.browser.name === 'Firefox' && majorVersion >= 70) score += 10
    if (report.browser.name === 'Safari' && majorVersion >= 14) score += 10

    // 根据错误数量调整分数
    score -= report.errors.length * 20
    score -= report.warnings.length * 5

    return Math.max(0, Math.min(100, score))
  }
}

// 导出单例实例
export const browserCompatibility = BrowserCompatibility.getInstance()

// 自动应用兼容性修复（如果在浏览器环境中）
if (typeof window !== 'undefined') {
  browserCompatibility.applyCompatibilityFixes()
}