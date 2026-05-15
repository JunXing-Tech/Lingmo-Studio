<template>
  <div v-if="!isMobile" class="brand-section">
    <div class="brand-bg"></div>
    <div class="brand-content">
      <div class="brand-logo">
        <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
      </div>
      <h1 class="brand-title">灵墨：让创作如行云流水</h1>
      <p class="brand-subtitle">深度协同的 AI 创作空间，释放您的灵感火花</p>
      <div class="brand-features">
        <div class="feature-item">
          <CheckCircleOutlined class="feature-check" />
          <span>智能 Agent 深度协同</span>
        </div>
        <div class="feature-item">
          <CheckCircleOutlined class="feature-check" />
          <span>流式生成匠心辞章</span>
        </div>
        <div class="feature-item">
          <CheckCircleOutlined class="feature-check" />
          <span>水墨质感全链路体验</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckCircleOutlined } from '@ant-design/icons-vue'
import { useMobile } from '@/hooks/useMobile.ts'

const { isMobile } = useMobile()
</script>

<style scoped>
.brand-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-xuanqing);
  background-image: var(--noise-texture);
}

.brand-bg::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 20%, rgba(34, 197, 94, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(34, 197, 94, 0.1) 0%, transparent 50%);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-xl);
  padding: 8px;
}

.brand-title {
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.8;
  margin: 0 0 48px;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.brand-features {
  text-align: left;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  backdrop-filter: blur(8px);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 14px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

.feature-check {
  font-size: 18px;
  color: white;
}

/* 响应式 */
@media (max-width: 768px) {
  .brand-section {
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 22px;
  }

  .brand-features {
    display: none;
  }
}
</style>
