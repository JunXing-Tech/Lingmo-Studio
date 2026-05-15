<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { listArticle } from '@/api/articleController'
import dayjs from 'dayjs'
import { 
  RocketOutlined, 
  FileTextOutlined,
  OrderedListOutlined,
  EditOutlined,
  PictureOutlined,
  ThunderboltOutlined,
  ClockCircleOutlined,
  RightOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 输入框
const topic = ref('')

// 最近文章
const recentArticles = ref<API.ArticleVO[]>([])
const loadingArticles = ref(false)

const goToCreate = () => {
  if (topic.value.trim()) {
    router.push({ path: '/create', query: { topic: topic.value } })
  } else {
    router.push('/create')
  }
}

const goToList = () => {
  router.push('/article/list')
}

const viewArticle = (article: API.ArticleVO) => {
  router.push(`/article/${article.taskId}`)
}

// 加载最近文章
const loadRecentArticles = async () => {
  if (!loginUserStore.loginUser.id) return
  
  loadingArticles.value = true
  try {
    const res = await listArticle({ pageNum: 1, pageSize: 6 })
    recentArticles.value = res.data.data?.records || []
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loadingArticles.value = false
  }
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 功能卡片数据
const features = [
  {
    icon: FileTextOutlined,
    title: '智能生成标题',
    description: 'AI 深度理解选题，构思极具吸引力的爆款标题',
    color: '#22C55E'
  },
  {
    icon: OrderedListOutlined,
    title: '自动生成大纲',
    description: '智能规划文章脉络，确保逻辑承转自然、严丝合缝',
    color: '#3B82F6'
  },
  {
    icon: EditOutlined,
    title: '流式生成正文',
    description: '流式输出匠心辞章，为您呈现行云流水般的创作过程',
    color: '#8B5CF6'
  },
  {
    icon: PictureOutlined,
    title: '智能配图',
    description: '自动匹配意境图文，让您的作品视觉表现力倍增',
    color: '#F59E0B'
  },
  {
    icon: ThunderboltOutlined,
    title: '瞬息而成',
    description: '5-10分钟即可完成千字佳作，效率与品质兼得',
    color: '#EF4444'
  },
  {
    icon: ClockCircleOutlined,
    title: '历史记录',
    description: '随心管理您的每一份墨迹，支持多格式一键导出',
    color: '#06B6D4'
  }
]

onMounted(() => {
  loadRecentArticles()
})
</script>

<template>
  <div id="homePage">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="hero-decorative-text calligraphy">灵墨</div>
        <div class="hero-badge">
          <RocketOutlined />
          <span>AI 驱动，释放无限创作灵感</span>
        </div>
        <h1 class="hero-title text-ink">灵墨协同：让创作如行云流水</h1>
        <p class="hero-subtitle">集成多重 AI Agent，为您打造从选题到成文的全链路智能创作体验</p>
        
        <div class="input-wrapper">
          <a-input
            v-model:value="topic"
            placeholder="输入您想创作的文章选题，例如：2026年AI如何改变职场"
            size="large"
            class="topic-input"
            @pressEnter="goToCreate"
          >
            <template #prefix>
              <EditOutlined class="input-icon" />
            </template>
          </a-input>
          <a-button type="primary" size="large" @click="goToCreate" class="cta-btn">
            <RocketOutlined />
            开始创作
          </a-button>
        </div>
        
        <p class="hero-tips">工作总结、心得体会、演讲稿、分析报告... 一键生成</p>
      </div>
    </div>

    <!-- Features Section -->
    <div class="features-section">
      <div class="container">
        <div class="section-header">
          <div class="section-badge">核心能力</div>
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">强大的 AI 能力，让创作变得简单高效</p>
        </div>
        <div class="features-grid">
          <div 
            v-for="(feature, index) in features" 
            :key="index"
            class="feature-card"
          >
            <div class="feature-icon-wrapper" :style="{ background: `${feature.color}15` }">
              <component :is="feature.icon" class="feature-icon" :style="{ color: feature.color }" />
            </div>
            <div class="feature-content">
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-description">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Articles Section -->
    <div v-if="loginUserStore.loginUser.id && recentArticles.length > 0" class="articles-section">
      <div class="container">
        <div class="section-header-row">
          <div>
            <div class="section-badge" style="margin-bottom: 8px; font-size: 12px; padding: 4px 10px;">最近创作</div>
            <h2 class="section-title-sm">历史记录</h2>
            <p class="section-subtitle-sm">查看您最近创作的文章</p>
          </div>
          <a-button type="link" @click="goToList" class="view-all-btn">
            查看全部
            <RightOutlined />
          </a-button>
        </div>
        
        <a-spin :spinning="loadingArticles">
          <div class="articles-grid">
            <div
              v-for="article in recentArticles"
              :key="article.id"
              class="article-card"
              @click="viewArticle(article)"
            >
              <div class="article-cover">
                <img 
                  v-if="article.coverImage" 
                  :src="article.coverImage" 
                  :alt="article.mainTitle"
                />
                <div v-else class="cover-placeholder">
                  <FileTextOutlined />
                </div>
              </div>
              <div class="article-info">
                <h4 class="article-title">{{ article.mainTitle || article.topic }}</h4>
                <div class="article-meta">
                  <span class="article-time">
                    <ClockCircleOutlined />
                    {{ formatTime(article.createTime) }}
                  </span>
                  <span :class="['article-status', `status-${article.status?.toLowerCase()}`]">
                    {{ article.status === 'COMPLETED' ? '已完成' : article.status === 'PROCESSING' ? '生成中' : '等待中' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: transparent;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 100px 20px 120px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 使用新的水墨晕染渐变 */
  background: var(--gradient-hero);
  z-index: 0;
}

.hero-bg::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(to bottom, transparent, var(--color-zhibai));
}

.container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
}

.hero-decorative-text {
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 120px;
  color: rgba(16, 24, 32, 0.03);
  z-index: -1;
  user-select: none;
  pointer-events: none;
  white-space: nowrap;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(34, 197, 94, 0.08);
  border: 1px solid rgba(34, 197, 94, 0.15);
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
  color: var(--color-primary-dark);
  letter-spacing: 1px;
}

.hero-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 64px;
  font-weight: 700;
  margin: 0 0 24px;
  letter-spacing: -1px;
  line-height: 1.2;
  color: var(--color-xuanqing);
  /* 模拟水墨从浓到淡的质感 */
  background: linear-gradient(135deg, var(--color-xuanqing) 0%, var(--color-mohui) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 22px;
  margin: 0 0 48px;
  color: var(--color-text-secondary);
  font-weight: 300;
  letter-spacing: 0.5px;
}

/* 核心输入框 */
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 720px;
  margin: 0 auto 20px;
  padding: 10px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-xl);
  box-shadow: var(--glass-shadow);
  border: var(--glass-border);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.input-wrapper::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--noise-texture);
  opacity: 0.02;
  pointer-events: none;
}

.input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.1), var(--shadow-xl);
  transform: translateY(-2px);
}

.topic-input {
  flex: 1;
  border: none !important;
  box-shadow: none !important;
  font-size: 18px;
  padding: 8px 16px;
  background: transparent !important;
  color: var(--color-mohei);
}

.topic-input::placeholder {
  color: var(--color-text-muted);
  font-weight: 300;
}

.topic-input:focus {
  box-shadow: none !important;
}

.input-icon {
  color: var(--color-mohui);
  font-size: 20px;
  margin-left: 8px;
}

.cta-btn {
  height: 56px !important;
  padding: 0 36px !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-lg) !important;
  background: var(--color-xuanqing) !important; /* 使用玄青色作为主按钮，更显稳重 */
  border: none !important;
  color: var(--color-zhibai) !important;
  box-shadow: var(--shadow-md) !important;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  transition: all var(--transition-normal) !important;
}

.cta-btn:hover {
  background: var(--color-mohei) !important;
  transform: scale(1.02);
  box-shadow: var(--shadow-lg) !important;
}

.cta-btn :deep(.ant-wave) {
  display: none;
}

.hero-tips {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

/* Features Section */
.features-section {
  padding: 80px 20px;
  background: var(--color-background-secondary);
}

.features-section .container {
  max-width: 1100px;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-badge {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(34, 197, 94, 0.1);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-dark);
  margin-bottom: 16px;
}

.section-title {
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 48px;
  color: var(--color-xuanqing);
  position: relative;
  display: block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 2px;
  background: var(--color-primary);
  opacity: 0.6;
}

.section-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 32px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: var(--glass-border);
  border-radius: var(--radius-xl);
  height: 100%;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  transition: all var(--transition-normal);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--noise-texture);
  opacity: 0.02;
  pointer-events: none;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
  border-color: var(--color-primary-light);
}

.feature-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.feature-icon {
  font-size: 22px;
}

.feature-content {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
  color: var(--color-xuanqing);
}

.feature-description {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.5;
}

/* Articles Section */
.articles-section {
  padding: 60px 20px 80px;
  background: var(--color-background);
}

.articles-section .container {
  max-width: 1100px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-title-sm {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--color-text);
}

.section-subtitle-sm {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary);
  font-weight: 500;
  padding: 0;
}

.view-all-btn:hover {
  color: var(--color-primary-dark);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.article-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: var(--glass-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
  cursor: pointer;
  height: 100%;
  position: relative;
}

.article-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--noise-texture);
  opacity: 0.02;
  pointer-events: none;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-light);
}

.article-cover {
  height: 140px;
  background: var(--color-background-tertiary);
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: var(--color-text-muted);
}

.article-info {
  padding: 16px;
}

.article-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--color-xuanqing);
  line-height: 1.4;
  /* 限制两行 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.article-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.article-status.status-completed {
  background: rgba(34, 197, 94, 0.1);
  color: var(--color-primary-dark);
}

.article-status.status-processing {
  background: rgba(59, 130, 246, 0.1);
  color: #2563EB;
}

.article-status.status-pending {
  background: var(--color-background-tertiary);
  color: var(--color-text-muted);
}

/* Responsive */
@media (max-width: 992px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .articles-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .hero-section {
    padding: 60px 16px 80px;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .input-wrapper {
    flex-direction: column;
    padding: 16px;
  }
  
  .topic-input {
    width: 100%;
    border-bottom: 1px solid var(--color-border-light) !important;
    border-radius: 0;
    padding: 8px 0 16px;
    margin-bottom: 16px;
  }
  
  .cta-btn {
    width: 100%;
    justify-content: center;
  }
  
  .features-grid {
    grid-template-columns: 1fr; /* 移动端改为单列 */
  }
  
  .articles-grid {
    grid-template-columns: 1fr; /* 移动端改为单列 */
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .section-header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
