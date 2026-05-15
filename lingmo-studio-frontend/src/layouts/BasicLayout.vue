<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: none;
  min-height: 100vh;
}

.main-content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
  display: flex;
  flex-direction: column;
}

/* 如果有需要全局约束的主体宽度，可以在这里通过媒体查询处理 */
@media screen and (max-width: 768px) {
  .main-content {
    /* 移动端确保没有多余的外边距导致出现横向滚动条 */
    overflow-x: hidden;
  }
}
</style>
