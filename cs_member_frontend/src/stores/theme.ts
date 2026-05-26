import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export type ThemeType = 'light' | 'dark'

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref<ThemeType>('light')

  // 从本地存储加载主题设置
  const loadThemeFromStorage = (): ThemeType => {
    const stored = localStorage.getItem('app-theme')
    if (stored && ['light', 'dark'].includes(stored)) {
      return stored as ThemeType
    }
    // 检测系统主题偏好
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }

  // 保存主题到本地存储
  const saveThemeToStorage = (theme: ThemeType) => {
    localStorage.setItem('app-theme', theme)
  }

  // 设置主题
  const setTheme = (theme: ThemeType) => {
    currentTheme.value = theme
    saveThemeToStorage(theme)
    applyThemeToDocument(theme)
  }

  // 切换主题
  const toggleTheme = () => {
    setTheme(currentTheme.value === 'light' ? 'dark' : 'light')
  }

  // 应用主题到文档
  const applyThemeToDocument = (theme: ThemeType) => {
    const body = document.body
    const html = document.documentElement

    // 检测是否为会员登录页面
    const isMemberLoginPage = window.location.pathname === '/login' ||
                             document.querySelector('.login-container .particle-system')

    // 添加过渡动画类
    body.classList.add('theme-transitioning')
    html.classList.add('theme-transitioning')

    // 如果是会员页面，临时隐藏动态元素以减少闪烁
    let tempHiddenElements = []
    if (isMemberLoginPage) {
      const particles = document.querySelectorAll('.particle, .star, .connection-path')
      particles.forEach(el => {
        el.style.transition = 'opacity 0.2s ease-in-out'
        el.style.opacity = '0'
        tempHiddenElements.push(el)
      })
    }

    // 使用 requestAnimationFrame 确保平滑过渡
    requestAnimationFrame(() => {
      // 移除所有主题类
      body.classList.remove('theme-light', 'theme-dark')
      html.classList.remove('theme-light', 'theme-dark')

      // 强制重绘以确保过渡效果
      void body.offsetWidth

      // 添加当前主题类
      body.classList.add(`theme-${theme}`)
      html.classList.add(`theme-${theme}`)

      // 设置主题数据属性
      body.setAttribute('data-theme', theme)
      html.setAttribute('data-theme', theme)

      // 更新CSS变量（这会触发背景图片的平滑过渡）
      updateThemeVariables(theme)

      // 恢复动态元素
      if (isMemberLoginPage && tempHiddenElements.length > 0) {
        setTimeout(() => {
          tempHiddenElements.forEach(el => {
            el.style.opacity = '1'
          })
        }, 100)
      }

      // 移除过渡动画类
      setTimeout(() => {
        body.classList.remove('theme-transitioning')
        html.classList.remove('theme-transitioning')
      }, 300)
    })
  }

  // 更新主题CSS变量
  const updateThemeVariables = (theme: ThemeType) => {
    const root = document.documentElement

    const themes = {
      light: {
        // 主要颜色
        '--primary-color': '#4f46e5',
        '--primary-light': '#6366f1',
        '--primary-dark': '#4338ca',
        '--primary-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        '--secondary-color': '#f59e0b',
        '--accent-color': '#ec4899',

        // 背景颜色
        '--bg-primary': '#ffffff',
        '--bg-secondary': '#f8fafc',
        '--bg-tertiary': '#f1f5f9',
        '--bg-gradient': 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
        '--bg-image': 'url("/src/assets/bg1.jpg")',

        // 卡片背景
        '--card-bg': 'rgba(255, 255, 255, 0.9)',
        '--card-border': 'rgba(255, 255, 255, 0.5)',
        '--card-shadow': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',

        // 文本颜色
        '--text-primary': '#1e293b',
        '--text-secondary': '#475569',
        '--text-muted': '#64748b',
        '--text-inverse': '#ffffff',

        // 边框颜色
        '--border-primary': '#e2e8f0',
        '--border-secondary': '#cbd5e1',
        '--border-focus': '#4f46e5',

        // 阴影颜色
        '--shadow-sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        '--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        '--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        '--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        '--shadow-glow': '0 0 40px rgba(79, 70, 229, 0.15)',

        // 装饰元素颜色
        '--decoration-primary': 'rgba(79, 70, 229, 0.1)',
        '--decoration-secondary': 'rgba(236, 72, 153, 0.08)',
        '--decoration-tertiary': 'rgba(245, 158, 11, 0.06)',

        // 按钮样式
        '--button-shadow': '0 10px 20px rgba(79, 70, 229, 0.25)',
        '--button-hover-shadow': '0 20px 40px rgba(79, 70, 229, 0.35)',
        '--button-transform': 'translateY(-2px) scale(1.02)',

        // 输入框样式
        '--input-shadow': '0 4px 12px rgba(0, 0, 0, 0.05)',
        '--input-focus-shadow': '0 0 0 3px rgba(79, 70, 229, 0.1), 0 4px 12px rgba(79, 70, 229, 0.15)',

        // 动画缓动函数
        '--ease-out-back': 'cubic-bezier(0.34, 1.56, 0.64, 1)',
        '--ease-in-out-cubic': 'cubic-bezier(0.4, 0, 0.2, 1)',
        '--ease-spring': 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
      },
      dark: {
        // 主要颜色
        '--primary-color': '#6366f1',
        '--primary-light': '#818cf8',
        '--primary-dark': '#4f46e5',
        '--primary-gradient': 'linear-gradient(135deg, #8b5cf6 0%, #3b82f6 100%)',
        '--secondary-color': '#fbbf24',
        '--accent-color': '#f472b6',

        // 背景颜色
        '--bg-primary': '#0f172a',
        '--bg-secondary': '#1e293b',
        '--bg-tertiary': '#334155',
        '--bg-gradient': 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
        '--bg-image': 'url("/src/assets/bg2.png")',

        // 卡片背景
        '--card-bg': 'rgba(30, 41, 59, 0.95)',
        '--card-border': 'rgba(71, 85, 105, 0.5)',
        '--card-shadow': '0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.05)',

        // 文本颜色
        '--text-primary': '#f8fafc',
        '--text-secondary': '#cbd5e1',
        '--text-muted': '#94a3b8',
        '--text-inverse': '#0f172a',

        // 边框颜色
        '--border-primary': '#334155',
        '--border-secondary': '#475569',
        '--border-focus': '#6366f1',

        // 阴影颜色
        '--shadow-sm': '0 1px 2px 0 rgba(0, 0, 0, 0.3)',
        '--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.4), 0 2px 4px -1px rgba(0, 0, 0, 0.3)',
        '--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.4), 0 4px 6px -2px rgba(0, 0, 0, 0.3)',
        '--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.4), 0 10px 10px -5px rgba(0, 0, 0, 0.3)',
        '--shadow-glow': '0 0 60px rgba(99, 102, 241, 0.3)',

        // 装饰元素颜色
        '--decoration-primary': 'rgba(139, 92, 246, 0.15)',
        '--decoration-secondary': 'rgba(244, 114, 182, 0.12)',
        '--decoration-tertiary': 'rgba(251, 191, 36, 0.08)',

        // 按钮样式
        '--button-shadow': '0 15px 30px rgba(99, 102, 241, 0.4)',
        '--button-hover-shadow': '0 25px 50px rgba(99, 102, 241, 0.6)',
        '--button-transform': 'translateY(-3px) scale(1.03)',

        // 输入框样式
        '--input-shadow': '0 4px 12px rgba(0, 0, 0, 0.2)',
        '--input-focus-shadow': '0 0 0 3px rgba(99, 102, 241, 0.2), 0 4px 12px rgba(99, 102, 241, 0.3)',

        // 动画缓动函数
        '--ease-out-back': 'cubic-bezier(0.34, 1.56, 0.64, 1)',
        '--ease-in-out-cubic': 'cubic-bezier(0.4, 0, 0.2, 1)',
        '--ease-spring': 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
      }
    }

    const variables = themes[theme]
    Object.entries(variables).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })
  }

  // 主题相关的计算属性
  const isLight = computed(() => currentTheme.value === 'light')
  const isDark = computed(() => currentTheme.value === 'dark')

  // 主题信息
  const themeInfo = computed(() => {
    const infoMap = {
      light: {
        name: '白天模式',
        description: '清新明亮，适合日间使用',
        icon: '☀️'
      },
      dark: {
        name: '黑夜模式',
        description: '深邃护眼，适合夜间使用',
        icon: '🌙'
      }
    }
    return infoMap[currentTheme.value]
  })

  // 预加载背景图片
  const preloadThemeImages = () => {
    const lightImg = new Image()
    const darkImg = new Image()

    lightImg.src = '/src/assets/bg1.jpg'
    darkImg.src = '/src/assets/bg2.png'

    // 预加载完成后添加到缓存
    Promise.all([lightImg.decode(), darkImg.decode()]).catch(err => {
    })
  }

  // 初始化主题
  const initTheme = () => {
    const savedTheme = loadThemeFromStorage()
    currentTheme.value = savedTheme
    preloadThemeImages() // 预加载图片
    applyThemeToDocument(savedTheme)
  }

  // 监听系统主题变化
  const watchSystemTheme = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      // 只有在没有手动设置主题时才跟随系统
      if (!localStorage.getItem('app-theme')) {
        setTheme(e.matches ? 'dark' : 'light')
      }
    })
  }

  // 监听主题变化
  watch(currentTheme, (newTheme) => {
    saveThemeToStorage(newTheme)
    applyThemeToDocument(newTheme)
  })

  return {
    // 状态
    currentTheme,

    // 计算属性
    isLight,
    isDark,
    themeInfo,

    // 方法
    setTheme,
    toggleTheme,
    initTheme,
    loadThemeFromStorage,
    saveThemeToStorage,
    watchSystemTheme
  }
})