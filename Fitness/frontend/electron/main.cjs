const { app, BrowserWindow, Menu, shell } = require('electron')
const path = require('path')

// 开发模式检测
const isDev = process.env.NODE_ENV === 'development'

let mainWindow

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

app.whenReady().then(() => {
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
