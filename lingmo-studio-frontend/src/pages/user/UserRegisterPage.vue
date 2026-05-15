<template>
  <div id="userRegisterPage">
    <div class="auth-container">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
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
      
      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-card">
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">注册开启您的 AI 创作之旅</p>
          
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" class="register-form">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input 
                v-model:value="formState.userAccount" 
                placeholder="请输入账号" 
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码不能小于 8 位' },
              ]"
            >
              <a-input-password 
                v-model:value="formState.userPassword" 
                placeholder="请输入密码" 
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { min: 8, message: '密码不能小于 8 位' },
                { validator: validateCheckPassword },
              ]"
            >
              <a-input-password 
                v-model:value="formState.checkPassword" 
                placeholder="请确认密码" 
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <SafetyOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block class="submit-btn">
                注册
              </a-button>
            </a-form-item>
          </a-form>
          
          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { UserOutlined, LockOutlined, SafetyOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: calc(100vh - 64px);
  background: var(--color-background-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 580px;
  background: white;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

/* 左侧品牌区域 */
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
  background: linear-gradient(135deg, #22C55E 0%, #16A34A 50%, #15803D 100%);
}

.brand-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: pulse-bg 8s ease-in-out infinite;
}

@keyframes pulse-bg {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.3; }
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
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0 0 36px;
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

/* 右侧表单区域 */
.form-section {
  flex: 1;
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.form-subtitle {
  font-size: 15px;
  color: var(--color-text-muted);
  margin: 0 0 32px;
}

/* 覆盖 Antd 输入框样式 */
.form-input {
  border-radius: var(--radius-md);
  padding: 10px 16px;
  background: var(--color-zhibai);
  border: 1px solid var(--color-border-light);
  transition: all var(--transition-fast);
}

.form-input:hover, .form-input:focus {
  background: white;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.1);
}

.input-icon {
  color: var(--color-text-muted);
  font-size: 16px;
  margin-right: 8px;
}

.submit-btn {
  margin-top: 8px;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 500;
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-green);
  transition: all var(--transition-fast);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.3);
}

.submit-btn:active {
  transform: translateY(1px);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.footer-text {
  color: var(--color-text-muted);
}

.login-link {
  color: var(--color-primary);
  font-weight: 500;
  margin-left: 8px;
  transition: color var(--transition-fast);
}

.login-link:hover {
  color: var(--color-primary-dark);
}

/* 移动端响应式样式 */
@media screen and (max-width: 768px) {
  #userRegisterPage {
    padding: 0;
    align-items: flex-start; /* 移动端靠上对齐 */
    background: white; /* 移动端背景设为纯白 */
  }

  .auth-container {
    box-shadow: none; /* 移除外层阴影 */
    border-radius: 0;
  }

  .brand-section {
    display: none; /* 移动端隐藏左侧品牌宣传区 */
  }

  .form-section {
    padding: 32px 24px;
    align-items: flex-start; /* 表单靠上对齐 */
    margin-top: 20px;
  }

  .form-card {
    max-width: 100%; /* 撑满屏幕宽度 */
  }
}
</style>
