# GitHub Pages 部署指南

## 📋 部署概览

AFitness 前端项目已配置为自动部署到 GitHub Pages，当推送代码到 `main` 分支时会自动触发构建和部署。

## 🚀 自动部署

### 触发条件
- 推送代码到 `main` 或 `master` 分支
- 修改 `Fitness/frontend/` 目录下的文件
- 修改 `.github/workflows/pages.yml` 工作流文件

### 部署流程
1. **代码检测** → GitHub Actions 检测到推送
2. **环境准备** → 设置 Node.js 18 环境
3. **依赖安装** → 使用缓存加速 `npm ci`
4. **项目构建** → 设置 `GITHUB_PAGES=true` 环境变量
5. **文件上传** → 上传构建产物到 GitHub Pages
6. **自动部署** → 部署到 `https://wzl2223096755.github.io/AFitness/`

## 🛠️ 手动部署

### 1. 使用脚本检查
```bash
# 检查部署配置
chmod +x scripts/check-deploy.sh
./scripts/check-deploy.sh
```

### 2. 本地预览构建
```bash
# 预览 GitHub Pages 构建
chmod +x scripts/deploy-github-pages.sh
./scripts/deploy-github-pages.sh
```

### 3. 手动触发部署
```bash
# 推送代码触发部署
git add .
git commit -m "Update frontend for GitHub Pages deployment"
git push origin main
```

## ⚙️ 配置说明

### GitHub Actions 工作流
- **文件位置**: `.github/workflows/pages.yml`
- **触发条件**: 推送到 main 分支，前端文件变更
- **构建环境**: Ubuntu Latest, Node.js 18
- **部署目标**: GitHub Pages

### Vite 构建配置
- **Base路径**: 自动检测 GitHub Pages 环境
- **路由模式**: Hash模式 (`createWebHashHistory`)
- **PWA支持**: GitHub Pages 环境下自动禁用
- **代码分割**: 优化资源加载

### 环境变量
```bash
GITHUB_PAGES=true          # 启用 GitHub Pages 模式
GITHUB_REPOSITORY=owner/repo  # 仓库名称
NODE_ENV=production        # 生产环境
```

## 🔍 故障排除

### 常见问题

#### 1. 404 错误
**原因**: 路由配置问题
**解决**: 确保使用 Hash 路由模式
```javascript
// router/index.js
import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes
})
```

#### 2. 资源加载失败
**原因**: Base 路径配置错误
**解决**: 检查 Vite 配置中的 base 设置
```javascript
// vite.config.js
const base = process.env.GITHUB_PAGES === 'true' ? '/AFitness/' : '/'
```

#### 3. 构建失败
**原因**: 依赖问题或语法错误
**解决**: 
```bash
# 清理并重新安装
cd Fitness/frontend
rm -rf node_modules package-lock.json
npm install
```

#### 4. 部署卡住
**原因**: 并发部署冲突
**解决**: 取消之前的部署
```bash
# 在 GitHub Actions 页面取消运行中的工作流
```

### 调试方法

#### 1. 查看构建日志
- 访问 GitHub Actions 页面
- 查看最新的工作流运行
- 检查构建步骤的输出

#### 2. 本地构建测试
```bash
cd Fitness/frontend
export GITHUB_PAGES=true
export GITHUB_REPOSITORY=wzl2223096755/AFitness
npm run build
```

#### 3. 检查构建产物
```bash
ls -la dist/
cat dist/index.html | head -20
```

## 📊 性能优化

### 构建优化
- **代码分割**: Vue、UI库、工具库分别打包
- **资源压缩**: Terser 压缩，移除 console.log
- **缓存策略**: 文件名包含哈希值
- **Tree Shaking**: 移除未使用的代码

### 加载优化
- **预加载**: 关键字体和资源
- **CDN**: Google Fonts CDN
- **懒加载**: 路由和组件按需加载
- **PWA**: 离线缓存（开发环境）

## 🌐 访问地址

- **生产环境**: https://wzl2223096755.github.io/AFitness/
- **开发环境**: http://localhost:3001
- **API文档**: http://localhost:8080/swagger-ui.html

## 📝 更新日志

### v1.0.0 (2024-03-01)
- ✅ 配置 GitHub Actions 自动部署
- ✅ 优化 Vite 构建配置
- ✅ 添加部署检查脚本
- ✅ 修复路由和资源路径问题
- ✅ 完善错误处理和日志

---

📞 **技术支持**: 如遇问题请查看 GitHub Actions 日志或创建 Issue
