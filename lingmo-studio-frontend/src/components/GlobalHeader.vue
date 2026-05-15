<template>
  <a-layout-header class="header">
    <div class="header-container">
      <div class="header-left">
        <RouterLink to="/" class="logo-link">
          <div class="logo-wrapper">
            <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
            <div class="site-title-group">
              <h1 class="site-title">灵墨协同</h1>
              <span class="site-subtitle calligraphy">AI Studio</span>
            </div>
          </div>
        </RouterLink>
      </div>

      <!-- 中间：导航菜单 (PC端显示) -->
      <nav v-if="!isMobile" class="nav-center">
        <RouterLink
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          :class="['nav-item', { active: selectedKeys.includes(item.key) }]"
        >
          <component :is="item.icon" class="nav-icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <!-- 右侧：用户操作区域 & 移动端汉堡菜单 -->
      <div class="header-right">
        <!-- 移动端汉堡菜单按钮 -->
        <div v-if="isMobile" class="mobile-menu-btn" @click="mobileMenuVisible = true">
          <MenuOutlined />
        </div>

        <div v-if="loginUserStore.loginUser.id" class="user-dropdown">
          <a-dropdown>
            <a-space class="user-info">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="36" class="user-avatar" />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </a-space>
            <template #overlay>
              <a-menu class="dropdown-menu">
                <a-menu-item @click="doLogout" class="dropdown-item">
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <RouterLink to="/user/login" class="login-btn">登录</RouterLink>
        </div>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <a-drawer
      v-model:open="mobileMenuVisible"
      title="灵墨协同"
      placement="right"
      :closable="true"
      :width="250"
    >
      <div class="mobile-menu-list">
        <RouterLink
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          :class="['mobile-nav-item', { active: selectedKeys.includes(item.key) }]"
          @click="mobileMenuVisible = false"
        >
          <component :is="item.icon" class="nav-icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </div>
    </a-drawer>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { useMobile } from '@/hooks/useMobile.ts'
import {
  LogoutOutlined,
  HomeOutlined,
  EditOutlined,
  UnorderedListOutlined,
  SettingOutlined,
  MenuOutlined
} from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const { isMobile } = useMobile()
const mobileMenuVisible = ref(false)

// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: HomeOutlined,
    label: '首页',
  },
  {
    key: '/create',
    icon: EditOutlined,
    label: '创作',
  },
  {
    key: '/article/list',
    icon: UnorderedListOutlined,
    label: '历史',
  },
  {
    key: '/admin/userManage',
    icon: SettingOutlined,
    label: '管理',
    admin: true,
  },
]

// 过滤菜单项
const menuItems = computed(() => {
  return originItems.filter((item) => {
    if (item.admin) {
      const loginUser = loginUserStore.loginUser
      return loginUser && loginUser.userRole === 'admin'
    }
    return true
  })
})

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  padding: 0;
  height: 64px;
  line-height: 64px;
  border-bottom: var(--glass-border);
  transition: all var(--transition-normal);
  overflow: hidden;
  width: 100%;
  box-shadow: var(--glass-shadow);
}

.header::before {
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

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-link {
  display: block;
  transition: opacity var(--transition-fast);
}

.logo-link:hover {
  opacity: 0.8;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.site-title-group {
  display: flex;
  flex-direction: column;
  justify-content: center;
  line-height: 1;
}

.logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.site-title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-xuanqing);
  white-space: nowrap;
  letter-spacing: 1px;
}

.site-subtitle {
  font-size: 12px;
  color: var(--color-mohui);
  letter-spacing: 1px;
  margin-top: 2px;
  opacity: 0.8;
}

/* 导航菜单 */
.nav-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  text-decoration: none;
  position: relative;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--color-primary);
  transition: all var(--transition-normal);
  transform: translateX(-50%);
}

.nav-item:hover {
  color: var(--color-xuanqing);
}

.nav-item.active {
  color: var(--color-xuanqing);
  font-weight: 600;
}

.nav-item.active::after {
  width: 60%;
}

.nav-icon {
  font-size: 16px;
}

/* 用户区域 */
.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  height: 64px;
  display: flex;
  align-items: center;
}

.user-info {
  padding: 6px 12px; 
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info:hover {
  background: var(--color-background-secondary);
}

.user-avatar {
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.user-name {
  color: var(--color-xuanqing);
  font-weight: 500;
}

.login-btn {
  padding: 8px 20px;
  background: var(--color-xuanqing);
  color: var(--color-zhibai);
  border-radius: var(--radius-md);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.login-btn:hover {
  background: var(--color-mohei);
  box-shadow: var(--shadow-md);
}

.dropdown-menu {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--color-background-secondary);
}

/* 移动端汉堡菜单样式 */
.mobile-menu-btn {
  font-size: 20px;
  color: var(--color-text);
  cursor: pointer;
  margin-right: 16px;
  display: flex;
  align-items: center;
}

/* 移动端抽屉内的菜单项 */
.mobile-menu-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 10px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: var(--color-text-secondary);
  text-decoration: none;
  padding: 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.mobile-nav-item.active {
  color: var(--color-primary);
  background: var(--bg-primary-light);
  font-weight: 500;
}

/* 移动端响应式样式覆盖 */
@media screen and (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .site-title {
    font-size: 18px;
  }
  
  .site-subtitle {
    display: none; /* 移动端隐藏副标题以节省空间 */
  }
  
  .logo-img {
    height: 28px;
  }
  
  .header-right {
    flex-direction: row-reverse; /* 让汉堡包在最右侧，头像在左边一点 */
  }
  
  .user-name {
    display: none; /* 移动端隐藏用户名，只留头像 */
  }
  
  .mobile-menu-btn {
    margin-right: 0;
    margin-left: 16px;
  }
}
</style>
