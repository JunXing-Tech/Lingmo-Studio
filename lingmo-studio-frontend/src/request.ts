import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建 Axios 实例，配置全局默认参数
const myAxios = axios.create({
  baseURL: 'http://localhost:8567/api',
  timeout: 60000,
  // 必须！携带 Cookie
  withCredentials: true,
})

// 配置全局响应拦截器：统一处理所有 HTTP 响应
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 检查业务状态码：40100 表示用户未登录或登录已过期
    if (data.code === 40100) {
      /**
       *  排除两种不需要跳转登录页的场景：、
       *  1. 当前请求本身就是获取登录状态的接口（避免无限循环）
       *  2. 用户已经在登录页面（避免重复跳转）
       */
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        // 显示 Ant Design Vue 的警告提示
        message.warning('请先登录')
        // 重定向到登录页，并携带当前页面 URL 作为 redirect 参数
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  // 错误响应处理函数：处理网络错误、超时、HTTP 状态码错误等
  function (error) {
    // 将错误继续抛出，让调用方可以捕获并自定义处理
    return Promise.reject(error)
  },
)

export default myAxios
