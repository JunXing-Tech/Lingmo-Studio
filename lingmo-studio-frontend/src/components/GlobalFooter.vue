<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://github.com/JunXing-Tech/Lingmo-Studio"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          灵墨协同（Lingmo Studio）开源仓库
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: transparent;
  text-align: center;
  padding: 40px 20px;
  margin-top: 0;
  position: relative;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 1px;
  background: var(--color-border);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.copyright {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
  letter-spacing: 0.5px;
  line-height: 1.8;
}

.author-link {
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.author-link:hover {
  color: var(--color-xuanqing);
}

/* 移动端响应式样式 */
@media screen and (max-width: 768px) {
  .footer {
    padding: 24px 16px;
  }
  
  .copyright {
    font-size: 12px;
    display: flex;
    flex-direction: column; /* 将可能的多段内容改为垂直堆叠 */
    gap: 8px;
  }
}
</style>
