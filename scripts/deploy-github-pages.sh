#!/bin/bash

# GitHub Pages 部署脚本
# 用于本地测试和预览 GitHub Pages 构建结果

set -e

echo "🚀 开始 GitHub Pages 部署预览..."

# 设置环境变量
export GITHUB_PAGES=true
export GITHUB_REPOSITORY=wzl2223096755/AFitness
export NODE_ENV=production

echo "📦 安装依赖..."
cd Fitness/frontend
npm ci --prefer-offline --no-audit

echo "🔨 构建项目..."
npm run build

echo "📁 检查构建结果..."
if [ -f "dist/index.html" ]; then
    echo "✅ 构建成功！"
    echo "📊 构建统计："
    echo "- 总文件数: $(find dist -type f | wc -l)"
    echo "- 总大小: $(du -sh dist | cut -f1)"
    echo "- 主要文件:"
    ls -la dist/ | head -10
else
    echo "❌ 构建失败：未找到 index.html"
    exit 1
fi

echo "🌐 启动预览服务器..."
# 使用 Python 简单服务器预览
if command -v python3 &> /dev/null; then
    cd dist
    echo "访问: http://localhost:8000/AFitness/"
    echo "按 Ctrl+C 停止服务器"
    python3 -m http.server 8000
elif command -v python &> /dev/null; then
    cd dist
    echo "访问: http://localhost:8000/AFitness/"
    echo "按 Ctrl+C 停止服务器"
    python -m SimpleHTTPServer 8000
else
    echo "❌ 未找到 Python，无法启动预览服务器"
    echo "请手动查看 dist/ 目录内容"
fi
