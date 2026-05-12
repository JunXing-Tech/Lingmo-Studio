<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 引入 Google Fonts - Geometric Modern, Serif & Calligraphy */
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&family=Noto+Serif+SC:wght@400;700&family=Zhi+Mang+Xing&family=Work+Sans:wght@300;400;500;600;700&display=swap');

/* 全局CSS变量 - 灵墨 (Lingmo) 东方美学配色方案 */
:root {
  /* ... 保持原有变量不变 ... */
  --color-primary: #22C55E;
  --color-primary-light: #4ADE80;
  --color-primary-dark: #16A34A;
  
  /* 辅助色 - 东方美学系列 */
  --color-xuanqing: #101820;
  --color-zhibai: #F7F3EE;
  --color-mohei: #1E1E1E;
  --color-mohui: #595959;
  --color-secondary: var(--color-xuanqing);
  --color-secondary-light: #334155;
  
  /* 字体族 */
  --font-sans: 'Work Sans', 'Outfit', -apple-system, sans-serif;
  --font-serif: 'Noto Serif SC', serif;
  --font-calligraphy: 'Zhi Mang Xing', cursive;
  
  /* 功能色 */
  --color-cta: var(--color-primary);
  --color-success: var(--color-primary);
  --color-warning: #EAB308;
  --color-error: #EF4444;
  --color-info: #3B82F6;
  
  /* 中性色重构 */
  --color-background: var(--color-zhibai);
  --color-background-secondary: #EFEEE9; /* 稍微深一点的纸色 */
  --color-background-tertiary: #E5E1D8;
  --color-background-dark: var(--color-xuanqing);
  --color-text: var(--color-mohei);
  --color-text-secondary: #4A4A4A;
  --color-text-muted: #8C8C8C;
  --color-border: #D1CDC7;
  --color-border-light: #E8E4DE;
  
  /* 渐变 - 模拟水墨晕染 */
  --gradient-primary: linear-gradient(135deg, #22C55E 0%, #16A34A 100%);
  /* 宣纸背景渐变：模拟中间亮四周微暗的纸张质感 */
  --gradient-paper: radial-gradient(circle at center, #FDFCFB 0%, #F7F3EE 100%);
  /* 水墨晕染渐变：从深墨色到半透明的过渡 */
  --gradient-ink: linear-gradient(180deg, rgba(16, 24, 32, 0.05) 0%, rgba(16, 24, 32, 0) 100%);
  
  --gradient-hero: linear-gradient(180deg, #E8F5E9 0%, var(--color-zhibai) 100%);
  --gradient-hero-dark: linear-gradient(180deg, #101820 0%, #1E1E1E 100%);
  
  /* 纯色背景 */
  --bg-primary: var(--color-primary);
  --bg-primary-hover: var(--color-primary-dark);
  --bg-dark: var(--color-xuanqing);
  --bg-dark-hover: #1E293B;
  --bg-success: var(--color-primary);
  
  /* 阴影 - 更加柔和，像水墨边缘 */
  --shadow-sm: 0 1px 3px 0 rgba(16, 24, 32, 0.03);
  --shadow-md: 0 4px 12px -2px rgba(16, 24, 32, 0.08);
  --shadow-lg: 0 12px 24px -4px rgba(16, 24, 32, 0.12);
  --shadow-xl: 0 24px 48px -8px rgba(16, 24, 32, 0.15);
  --shadow-card: 0 2px 8px rgba(16, 24, 32, 0.05);
  --shadow-card-hover: 0 12px 32px rgba(34, 197, 94, 0.12);
  --shadow-green: 0 4px 14px rgba(34, 197, 94, 0.2);
  
  /* 圆角 */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-2xl: 24px;
  --radius-full: 9999px;
  
  /* 过渡 */
  --transition-fast: 150ms ease-out;
  --transition-normal: 200ms ease-out;
  --transition-slow: 300ms ease-out;
  
  /* 毛玻璃 - 更加细腻 */
  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-bg-green: rgba(232, 245, 233, 0.7);
  --glass-blur: blur(20px) saturate(180%);
  --glass-border: 1px solid rgba(255, 255, 255, 0.4);
  --glass-shadow: 0 8px 32px 0 rgba(16, 24, 32, 0.05);
  
  /* 噪声纹理 - 模拟纸张或高级材质感 */
  --noise-texture: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

/* 全局样式优化 */
* {
  box-sizing: border-box;
}

/* 噪声叠加层定义 */
.noise-overlay {
  position: relative;
}

.noise-overlay::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--noise-texture);
  opacity: 0.03; /* 极其微弱的噪声，提升高级感 */
  pointer-events: none;
  z-index: 1;
  border-radius: inherit;
}

body {
  margin: 0;
  padding: 0;
  font-family: var(--font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background: var(--gradient-paper);
  background-attachment: fixed;
  color: var(--color-text);
  line-height: 1.8; /* 增加行高，提升阅读感 */
}

h1, h2, h3, h4, h5, h6, .serif-title {
  font-family: var(--font-serif);
  font-weight: 700;
  color: var(--color-xuanqing);
  letter-spacing: 0.02em;
}

.calligraphy {
  font-family: var(--font-calligraphy);
  font-weight: 400;
}

.text-ink {
  background: linear-gradient(135deg, var(--color-xuanqing) 0%, var(--color-mohui) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

html {
  overflow-x: hidden;
}

h1, h2, h3, h4, h5, h6 {
  font-family: 'Outfit', sans-serif;
}

/* 全局链接样式 */
a {
  color: var(--color-text);
  text-decoration: none;
  transition: color var(--transition-fast);
}

a:hover {
  color: var(--color-primary);
}

/* 全局按钮增强 */
.ant-btn {
  transition: all var(--transition-normal);
  font-weight: 500;
  border-radius: var(--radius-md);
}

.ant-btn-primary {
  background: var(--bg-primary);
  border-color: var(--bg-primary);
  box-shadow: var(--shadow-green);
}

.ant-btn-primary:not(:disabled):hover {
  background: var(--bg-primary-hover);
  border-color: var(--bg-primary-hover);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.35);
}

/* 全局卡片增强 */
.ant-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
}

/* 可点击元素统一cursor */
.clickable,
[role="button"],
button,
.ant-btn,
.ant-card.hoverable {
  cursor: pointer;
}

/* 选中状态 */
::selection {
  background: rgba(34, 197, 94, 0.2);
  color: var(--color-text);
}

/* Ant Design 菜单覆盖 */
.ant-menu-horizontal > .ant-menu-item:hover::after,
.ant-menu-horizontal > .ant-menu-item-active::after,
.ant-menu-horizontal > .ant-menu-item-selected::after {
  border-bottom-color: var(--color-primary) !important;
}

.ant-menu-horizontal > .ant-menu-item:hover,
.ant-menu-horizontal > .ant-menu-item-active {
  color: var(--color-text) !important;
}

/* Ant Design 按钮 disabled 状态 */
.ant-btn:disabled {
  background: var(--color-border) !important;
  color: var(--color-text-muted) !important;
  border-color: var(--color-border) !important;
  box-shadow: none !important;
}

.ant-btn-primary:disabled {
  background: var(--color-border) !important;
  color: var(--color-text-muted) !important;
}

/* Steps 组件样式覆盖 */
.ant-steps .ant-steps-item-process .ant-steps-item-icon {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish .ant-steps-item-icon {
  border-color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish .ant-steps-item-icon > .ant-steps-icon {
  color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish > .ant-steps-item-container > .ant-steps-item-tail::after {
  background-color: var(--color-primary);
}

/* 输入框聚焦状态 */
.ant-input:focus,
.ant-input-focused,
.ant-input-affix-wrapper:focus,
.ant-input-affix-wrapper-focused {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.1) !important;
}

.ant-input:hover {
  border-color: var(--color-primary-light);
}

/* 文本域 */
.ant-input-textarea-show-count::after {
  color: var(--color-text-muted);
}

/* Progress 组件 */
.ant-progress-bg {
  background: var(--gradient-primary);
}

/* Spin 组件 */
.ant-spin-dot-item {
  background-color: var(--color-primary);
}
</style>
