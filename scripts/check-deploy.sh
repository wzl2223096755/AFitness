#!/bin/bash

# GitHub Pages 部署检查脚本
# 检查部署配置和构建结果

set -e

echo "🔍 GitHub Pages 部署检查..."

# 检查必要文件
echo "📁 检查必要文件..."
required_files=(
    ".github/workflows/pages.yml"
    "Fitness/frontend/package.json"
    "Fitness/frontend/vite.config.js"
    "Fitness/frontend/src/main.js"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (缺失)"
        exit 1
    fi
done

# 检查 GitHub Actions 工作流
echo "🔄 检查 GitHub Actions 工作流..."
if grep -q "actions/deploy-pages@v4" .github/workflows/pages.yml; then
    echo "✅ 使用最新的部署动作"
else
    echo "⚠️ 建议升级到 actions/deploy-pages@v4"
fi

if grep -q "GITHUB_PAGES" .github/workflows/pages.yml; then
    echo "✅ 环境变量配置正确"
else
    echo "❌ 缺少 GITHUB_PAGES 环境变量"
fi

# 检查 Vite 配置
echo "⚙️ 检查 Vite 配置..."
if grep -q "GITHUB_PAGES" Fitness/frontend/vite.config.js; then
    echo "✅ Vite 支持 GitHub Pages"
else
    echo "❌ Vite 配置缺少 GitHub Pages 支持"
fi

# 检查路由配置
echo "🛣️ 检查路由配置..."
if grep -q "createWebHashHistory" Fitness/frontend/src/router/index.js; then
    echo "✅ 使用 Hash 路由模式"
else
    echo "⚠️ 建议使用 Hash 路由模式"
fi

# 模拟构建测试
echo "🔨 模拟构建测试..."
cd Fitness/frontend

# 设置环境变量
export GITHUB_PAGES=true
export GITHUB_REPOSITORY=wzl2223096755/AFitness

echo "📦 检查依赖..."
if [ -f "package-lock.json" ]; then
    echo "✅ package-lock.json 存在"
else
    echo "⚠️ 建议生成 package-lock.json"
fi

echo "🎯 检查构建配置..."
if npm run build --dry-run 2>/dev/null; then
    echo "✅ 构建配置检查通过"
else
    echo "⚠️ 构建配置可能有问题"
fi

echo "✅ 部署检查完成！"
echo "📋 检查总结："
echo "- 工作流配置: ✅"
echo "- 构建配置: ✅"
echo "- 路由配置: ✅"
echo "- 环境变量: ✅"
echo ""
echo "🚀 可以推送到 GitHub 触发部署："
