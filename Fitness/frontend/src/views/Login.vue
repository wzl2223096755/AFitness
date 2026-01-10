<!--
  登录页面 - 向后兼容重定向
  Login page - backward compatibility redirect
  
  此文件保留用于向后兼容，实际实现已迁移到 modules/auth/views/LoginView.vue
  This file is kept for backward compatibility, actual implementation moved to modules/auth/views/LoginView.vue
-->
<script>
// 从认证模块导入登录视图
import { LoginView } from '@/modules/auth'

// 直接导出认证模块的登录视图
export default LoginView
</script>
