const { app, BrowserWindow, Menu, shell, dialog } = require('electron')
const path = require('path')
const { spawn } = require('child_process')
const http = require('http')

// 开发模式检测
const isDev = process.env.NODE_ENV === 'development'

let mainWindow
let backendProcess = null
const BACKEND_PORT = 8080
const BACKEND_URL = `http://localhost:${BACKEND_PORT}`

// 获取后端 JAR 路径
function getBackendJarPath() {
  if (isDev) {
    // 开发模式：使用项目根目录的 JAR
    return path.join(__dirname, '../../target/fitness-0.0.1-SNAPSHOT.jar')
  } else {
    // 生产模式：JAR 在 resources/backend 目录
    return path.join(process.resourcesPath, 'backend', 'fitness.jar')
  }
}

// 检查后端是否已启动
function checkBackendHealth() {
  return new Promise((resolve) => {
    const req = http.get(`${BACKEND_URL}/api/health`, (res) => {
      resolve(res.statusCode === 200)
    })
    req.on('error', () => resolve(false))
    req.setTimeout(2000, () => {
      req.destroy()
      resolve(false)
    })
  })
}

// 等待后端启动
async function waitForBackend(maxAttempts = 30) {
  for (let i = 0; i < maxAttempts; i++) {
    const isHealthy = await checkBackendHealth()
    if (isHealthy) {
      console.log('Backend is ready!')
      return true
    }
    console.log(`Waiting for backend... (${i + 1}/${maxAttempts})`)
    await new Promise(resolve => setTimeout(resolve, 1000))
  }
  return false
}

// 启动后端服务
async function startBackend() {
  // 先检查后端是否已经在运行
  const alreadyRunning = await checkBackendHealth()
  if (alreadyRunning) {
    console.log('Backend already running')
    return true
  }

  const jarPath = getBackendJarPath()
  console.log('Starting backend from:', jarPath)

  // 检查 JAR 文件是否存在
  const fs = require('fs')
  if (!fs.existsSync(jarPath)) {
    console.error('Backend JAR not found:', jarPath)
    dialog.showErrorBox('启动错误', `找不到后端服务文件: ${jarPath}`)
    return false
  }

  // 启动 Java 进程
  backendProcess = spawn('java', [
    '-jar',
    jarPath,
    '--spring.profiles.active=h2',
    '--server.port=' + BACKEND_PORT
  ], {
    detached: false,
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true
  })

  backendProcess.stdout.on('data', (data) => {
    console.log(`[Backend] ${data}`)
  })

  backendProcess.stderr.on('data', (data) => {
    console.error(`[Backend Error] ${data}`)
  })

  backendProcess.on('error', (err) => {
    console.error('Failed to start backend:', err)
    dialog.showErrorBox('启动错误', `无法启动后端服务: ${err.message}\n\n请确保已安装 Java 17 或更高版本`)
  })

  backendProcess.on('exit', (code) => {
    console.log(`Backend exited with code ${code}`)
    backendProcess = null
  })

  // 等待后端启动
  return await waitForBackend()
}

// 停止后端服务
function stopBackend() {
  if (backendProcess) {
    console.log('Stopping backend...')
    if (process.platform === 'win32') {
      // Windows: 使用 taskkill 强制终止进程树
      spawn('taskkill', ['/pid', backendProcess.pid, '/f', '/t'])
    } else {
      backendProcess.kill('SIGTERM')
    }
    backendProcess = null
  }
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1024,
    minHeight: 768,
    icon: path.join(__dirname, '../public/favicon.ico'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.cjs')
    },
    titleBarStyle: 'default',
    show: false
  })

  // 窗口准备好后显示，避免白屏
  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  // 加载应用
  if (isDev) {
    // 开发模式：连接 Vite 开发服务器
    mainWindow.loadURL('http://localhost:3001')
    mainWindow.webContents.openDevTools()
  } else {
    // 生产模式：加载打包后的文件
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  // 外部链接在默认浏览器打开
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// 创建菜单
function createMenu() {
  const template = [
    {
      label: '文件',
      submenu: [
        { label: '刷新', accelerator: 'CmdOrCtrl+R', click: () => mainWindow?.reload() },
        { type: 'separator' },
        { label: '退出', accelerator: 'CmdOrCtrl+Q', click: () => app.quit() }
      ]
    },
    {
      label: '视图',
      submenu: [
        { label: '全屏', accelerator: 'F11', click: () => mainWindow?.setFullScreen(!mainWindow.isFullScreen()) },
        { label: '开发者工具', accelerator: 'F12', click: () => mainWindow?.webContents.toggleDevTools() }
      ]
    },
    {
      label: '帮助',
      submenu: [
        { label: '关于 AFitness', click: () => shell.openExternal('https://github.com/wzl2223096755/AFitness') }
      ]
    }
  ]

  const menu = Menu.buildFromTemplate(template)
  Menu.setApplicationMenu(menu)
}

// 显示启动画面
function showSplashScreen() {
  const splash = new BrowserWindow({
    width: 400,
    height: 300,
    frame: false,
    transparent: true,
    alwaysOnTop: true,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    }
  })

  splash.loadURL(`data:text/html;charset=utf-8,
    <html>
    <head>
      <style>
        body {
          margin: 0;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          height: 100vh;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          border-radius: 12px;
        }
        h1 { margin: 0 0 20px 0; font-size: 32px; }
        .loader {
          width: 50px;
          height: 50px;
          border: 4px solid rgba(255,255,255,0.3);
          border-top-color: white;
          border-radius: 50%;
          animation: spin 1s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        p { margin-top: 20px; font-size: 14px; opacity: 0.9; }
      </style>
    </head>
    <body>
      <h1>🏋️ AFitness</h1>
      <div class="loader"></div>
      <p>正在启动服务...</p>
    </body>
    </html>
  `)

  return splash
}

app.whenReady().then(async () => {
  // 显示启动画面
  const splash = showSplashScreen()

  // 启动后端
  const backendStarted = await startBackend()
  
  if (!backendStarted) {
    splash.close()
    const choice = dialog.showMessageBoxSync({
      type: 'error',
      title: '启动失败',
      message: '后端服务启动失败',
      detail: '请确保已安装 Java 17 或更高版本，并且端口 8080 未被占用。',
      buttons: ['重试', '退出'],
      defaultId: 0
    })
    
    if (choice === 0) {
      app.relaunch()
    }
    app.quit()
    return
  }

  // 关闭启动画面，显示主窗口
  splash.close()
  createWindow()
  createMenu()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  stopBackend()
})

app.on('quit', () => {
  stopBackend()
})
