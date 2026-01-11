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

### 3. 一键打包（推荐）
```powershell
# 在 Fitness 目录运行
.\build-desktop.ps1
```

### 4. 手动打包 Windows .exe

#### 便携版（免安装，推荐）
```bash
npm run electron:build:portable
```
输出文件：`release/AFitness-1.0.0-Portable.exe`（约 170MB，含后端）

#### 安装版（带安装向导）
```bash
npm run electron:build:win
```
输出文件：`release/AFitness-1.0.0-x64.exe`

## 打包输出

打包完成后，文件位于 `Fitness/frontend/release/` 目录：

| 文件 | 说明 | 大小 |
|------|------|------|
| `AFitness-1.0.0-Portable.exe` | 便携版（免安装，双击运行） | ~170MB |
| `AFitness-1.0.0-x64.exe` | Windows 安装程序 | ~170MB |
| `win-unpacked/` | 解压版目录 | ~500MB |

## 内嵌后端说明

桌面应用已内嵌 Spring Boot 后端 JAR：
- 启动 exe 时自动启动后端服务
- 关闭 exe 时自动停止后端服务
- 使用 H2 内存数据库（无需安装 MySQL）
- 数据存储在用户目录

## 系统要求

- **Java 17 或更高版本**（必须）
- Windows 10/11 64位
- 至少 4GB 内存

## 技术栈

- Electron 28.x
- Vue 3 + Vite
- Spring Boot 3.2.5
- H2 Database
- electron-builder 24.x

## 常见问题

### Q: 启动时提示"找不到 Java"？
A: 请安装 Java 17 或更高版本：
- 下载地址：https://adoptium.net/
- 安装后确保 `java` 命令可用

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
A: 
1. 检查是否安装了 Java 17+
2. 等待后端启动（约 10-20 秒）
3. 查看启动画面是否显示"正在启动服务..."

### Q: 端口 8080 被占用？
A: 关闭占用 8080 端口的程序，或修改 `electron/main.cjs` 中的 `BACKEND_PORT` 变量
