# AFitness 桌面应用打包指南

## 快速开始

### 1. 安装依赖
```bash
cd Fitness/frontend
npm install
```

### 2. 开发模式测试
```bash
# 先启动 Vite 开发服务器
npm run dev

# 新终端窗口，启动 Electron
npm run electron:dev
```

### 3. 打包 Windows .exe

#### 便携版（免安装，推荐）
```bash
npm run electron:build:portable
```
输出文件：`release/AFitness-1.0.0-Portable.exe`（约 80MB）

#### 安装版（带安装向导）
```bash
npm run electron:build:win
```
输出文件：`release/AFitness-1.0.0-x64.exe`

## 打包输出

打包完成后，文件位于 `Fitness/frontend/release/` 目录：

| 文件 | 说明 | 大小 |
|------|------|------|
| `AFitness-1.0.0-Portable.exe` | 便携版（免安装，双击运行） | ~80MB |
| `AFitness-1.0.0-x64.exe` | Windows 安装程序 | ~80MB |
| `win-unpacked/` | 解压版目录 | ~400MB |

## 注意事项

1. **后端服务**：桌面应用仍需要后端 API 服务运行
   - 可以连接远程服务器
   - 或者使用 H2 数据库本地运行：`.\start-h2.ps1`

2. **API 地址配置**：生产环境需要修改 API 地址
   - 编辑 `src/api/config.js` 或环境变量

3. **图标自定义**：替换 `public/favicon.ico` 为你的图标（已包含默认图标）

## 技术栈

- Electron 28.x
- Vue 3 + Vite
- electron-builder 24.x

## 常见问题

### Q: 打包失败提示下载 Electron 超时？
A: 项目已配置国内镜像源（`.npmrc` 文件），如仍有问题可手动设置：
```bash
# 创建或编辑 .npmrc 文件
electron_mirror=https://npmmirror.com/mirrors/electron/
electron_builder_binaries_mirror=https://npmmirror.com/mirrors/electron-builder-binaries/
```

### Q: 打包时出现符号链接权限错误？
A: 这是 Windows 创建符号链接需要管理员权限导致的（winCodeSign 解压时需要创建 darwin 平台的符号链接）。解决方案：
1. 直接使用 `release/win-unpacked/AFitness.exe` 运行
2. 或使用已生成的 `release/AFitness-1.0.0-Portable.exe`
3. 或以管理员身份运行打包命令

### Q: 如何打包 Mac/Linux 版本？
A: 
```bash
# Mac
npm run electron:build -- --mac

# Linux  
npm run electron:build -- --linux
```

### Q: 应用启动后白屏？
A: 确保后端服务已启动，或检查 API 地址配置是否正确。
