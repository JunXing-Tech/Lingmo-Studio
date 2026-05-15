<template>
  <div class="article-detail-page">
    <div class="page-header">
      <div class="header-container">
        <div class="header-actions">
          <a-button @click="goBack" class="back-btn">
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            返回
          </a-button>
          <a-button type="primary" @click="exportMarkdown" class="export-btn">
            <template #icon>
              <DownloadOutlined />
            </template>
            导出 Markdown
          </a-button>
        </div>
      </div>
    </div>

    <div class="container">
      <a-spin :spinning="loading" tip="加载中...">
        <a-card :bordered="false" v-if="article" class="article-card">
          <!-- 标题 -->
          <div class="title-section">
            <h1 class="main-title">{{ article.mainTitle }}</h1>
            <p class="sub-title">{{ article.subTitle }}</p>
            <div class="meta-info">
              <a-tag :color="getStatusColor(article.status)" class="status-tag">
                {{ getStatusText(article.status) }}
              </a-tag>
              <span class="time">创建于 {{ formatDate(article.createTime) }}</span>
            </div>
          </div>

          <a-divider />

          <!-- 大纲 -->
          <div v-if="article.outline && article.outline.length > 0" class="outline-section">
            <h2 class="section-title">
              <OrderedListOutlined class="section-icon" />
              文章大纲
            </h2>
            <div class="outline-list">
              <div v-for="item in article.outline" :key="item.section" class="outline-item">
                <div class="outline-title">{{ item.section }}. {{ item.title }}</div>
                <ul class="outline-points">
                  <li v-for="(point, idx) in item.points" :key="idx">{{ point }}</li>
                </ul>
              </div>
            </div>
          </div>

          <a-divider v-if="article.outline && article.outline.length > 0" />

          <!-- 完整图文（优先展示） -->
          <div v-if="article.fullContent" class="content-section">
            <h2 class="section-title">
              <FileTextOutlined class="section-icon" />
              完整图文
            </h2>
            <div v-html="markdownToHtml(article.fullContent)" class="markdown-content"></div>
          </div>

          <!-- 普通正文（无 fullContent 时展示） -->
          <div v-else-if="article.content" class="content-section">
            <h2 class="section-title">
              <FileTextOutlined class="section-icon" />
              文章正文
            </h2>
            <div v-html="markdownToHtml(article.content)" class="markdown-content"></div>
          </div>

          <!-- 配图（仅在没有 fullContent 时单独展示） -->
          <div v-if="!article.fullContent && article.images && article.images.length > 0" class="images-section">
            <h2 class="section-title">
              <PictureOutlined class="section-icon" />
              文章配图
            </h2>
            <div class="images-grid">
              <div v-for="image in article.images" :key="image.position" class="image-item">
                <img :src="image.url" :alt="image.description" />
                <div class="image-info">
                  <span class="badge">{{ image.method }}</span>
                  <span class="keywords">{{ image.keywords }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, DownloadOutlined, OrderedListOutlined, FileTextOutlined, PictureOutlined } from '@ant-design/icons-vue'
import { getArticle } from '@/api/articleController'
import { marked } from 'marked'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const article = ref<API.ArticleVO | null>(null)

// Markdown 转 HTML
const markdownToHtml = (markdown: string) => {
  return marked(markdown)
}

// 加载文章
const loadArticle = async () => {
  const taskId = route.params.taskId as string
  if (!taskId) {
    message.error('文章ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getArticle({ taskId })
    article.value = res.data.data
  } catch (error: any) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 导出 Markdown
const exportMarkdown = () => {
  if (!article.value) return

  let markdown = `# ${article.value.mainTitle}\n\n`
  markdown += `> ${article.value.subTitle}\n\n`

  // 优先使用完整图文
  if (article.value.fullContent) {
    markdown += article.value.fullContent
  } else {
    if (article.value.outline && article.value.outline.length > 0) {
      markdown += `## 目录\n\n`
      article.value.outline.forEach(item => {
        markdown += `${item.section}. ${item.title}\n`
      })
      markdown += `\n---\n\n`
    }

    markdown += article.value.content || ''

    if (article.value.images && article.value.images.length > 0) {
      markdown += `\n\n## 配图\n\n`
      article.value.images.forEach(image => {
        markdown += `![${image.description}](${image.url})\n\n`
      })
    }
  }

  const blob = new Blob([markdown], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${article.value.mainTitle}.md`
  a.click()
  URL.revokeObjectURL(url)

  message.success('导出成功')
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    PENDING: 'default',
    PROCESSING: 'processing',
    COMPLETED: 'success',
    FAILED: 'error',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    PENDING: '等待中',
    PROCESSING: '生成中',
    COMPLETED: '已完成',
    FAILED: '失败',
  }
  return textMap[status] || status
}

onMounted(() => {
  loadArticle()
})
</script>

<style scoped lang="scss">
.article-detail-page {
  background: var(--color-background-secondary);
  min-height: 100vh;
  padding-bottom: 60px;

  .page-header {
    background: var(--gradient-hero);
    padding: 20px;
    margin-bottom: 24px;
  }

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .back-btn {
    background: white;
    border: 1px solid var(--color-border);
    color: var(--color-text);
    font-size: 13px;
    transition: all var(--transition-fast);
    border-radius: var(--radius-md);

    &:hover {
      background: var(--color-background-secondary);
      border-color: var(--color-border);
      color: var(--color-text);
    }
  }

  .export-btn {
    background: var(--gradient-primary);
    color: white;
    border: none;
    font-weight: 600;
    font-size: 13px;
    transition: all var(--transition-fast);
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-green);

    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }
  }

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .article-card {
    background: var(--glass-bg);
    backdrop-filter: var(--glass-blur);
    -webkit-backdrop-filter: var(--glass-blur);
    border: var(--glass-border);
    border-radius: var(--radius-2xl);
    box-shadow: var(--glass-shadow);
    position: relative;
    overflow: hidden;

    :deep(.ant-card-body) {
      padding: 40px;
    }
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

  .title-section {
    margin-bottom: 28px;
    text-align: center;

    .main-title {
      font-family: var(--font-serif);
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 16px;
      color: var(--color-xuanqing);
      line-height: 1.3;
    }

    .sub-title {
      font-size: 16px;
      color: var(--color-text-secondary);
      margin: 0 0 20px;
    }

    .meta-info {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: var(--color-text-muted);
      font-size: 13px;
    }

    .status-tag {
      border-radius: var(--radius-full);
      font-size: 12px;
      padding: 2px 12px;
    }
  }

  .section-title {
    font-family: var(--font-serif);
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 24px;
    color: var(--color-xuanqing);
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .section-icon {
    font-size: 18px;
    color: var(--color-text-secondary);
  }

  .outline-section {
    margin-bottom: 28px;

    .outline-list {
      .outline-item {
        margin-bottom: 12px;
        padding: 16px;
        background: var(--color-background-secondary);
        border-radius: var(--radius-md);
        border: 1px solid var(--color-border-light);
        transition: all var(--transition-fast);

        &:hover {
          border-color: var(--color-border);
        }

        .outline-title {
          font-family: var(--font-serif);
          font-size: 15px;
          font-weight: 700;
          color: var(--color-xuanqing);
          margin-bottom: 8px;
        }

        .outline-points {
          margin: 0;
          padding-left: 18px;

          li {
            margin-bottom: 4px;
            color: var(--color-text-secondary);
            line-height: 1.6;
            font-size: 13px;
          }
        }
      }
    }
  }

  .content-section {
    margin-bottom: 28px;

    /* Markdown 样式 */
    .markdown-content {
      font-size: 16px;
      line-height: 1.8;
      color: var(--color-text);

      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4) {
        margin-top: 1.5em;
        margin-bottom: 0.5em;
        color: var(--color-xuanqing);
      }

      :deep(p) {
        margin-bottom: 1.2em;
      }

      :deep(ul),
      :deep(ol) {
        margin-bottom: 1.2em;
        padding-left: 24px;
      }

      :deep(li) {
        margin-bottom: 0.5em;
      }

      :deep(blockquote) {
        margin: 1.5em 0;
        padding: 16px 20px;
        border-left: 4px solid var(--color-primary);
        background: var(--color-background-secondary);
        border-radius: 0 var(--radius-md) var(--radius-md) 0;
        color: var(--color-text-secondary);
      }

      :deep(pre) {
        background: #f5f5f5;
        padding: 16px;
        border-radius: var(--radius-md);
        overflow-x: auto;
        margin: 1.5em 0;
        max-width: 100%;
      }

      :deep(code) {
        font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
        font-size: 14px;
      }

      :deep(img) {
        max-width: 100%;
        height: auto;
        border-radius: var(--radius-md);
        margin: 1em 0;
      }

      :deep(video) {
        max-width: 100%;
        height: auto;
      }
    }
  }

  .images-section {
    .images-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 16px;

      .image-item {
        border-radius: var(--radius-md);
        overflow: hidden;
        border: 1px solid var(--color-border);
        transition: all var(--transition-normal);
        cursor: pointer;

        &:hover {
          border-color: var(--color-text-muted);
          box-shadow: var(--shadow-md);
        }

        img {
          width: 100%;
          height: 160px;
          object-fit: cover;
        }

        .image-info {
          padding: 12px;
          background: white;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .badge {
            padding: 3px 10px;
            background: var(--color-text);
            color: white;
            border-radius: var(--radius-md);
            font-size: 11px;
            font-weight: 500;
          }

          .keywords {
            font-size: 11px;
            color: var(--color-text-muted);
          }
        }
      }
    }
  }
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .header-container {
    padding: 12px 16px;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .container {
    padding: 16px;
  }

  .article-card {
    :deep(.ant-card-body) {
      padding: 16px;
    }
  }

  .main-title {
    font-size: 24px;
  }

  .sub-title {
    font-size: 16px;
  }

  .markdown-content {
    font-size: 15px; /* 移动端字号稍微调小，但也保持可读性 */
  }

  .images-grid {
    grid-template-columns: 1fr;
  }
}
</style>
