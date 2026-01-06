# 健身管理系统前端停止脚本
# 此脚本将停止用户端前端和管理端前端服务

Write-Host "========================================" -ForegroundColor Red
Write-Host "     停止健身管理系统前端服务" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red

# 停止前端服务 (Node.js/Vite进程)
Write-Host "正在停止前端服务..." -ForegroundColor Yellow

# 查找并停止所有Vite开发服务器进程
try {
    $viteProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
        try {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)" -ErrorAction SilentlyContinue).CommandLine
            $cmdLine -like "*vite*"
        } catch {
            $false
        }
    }
    
    if ($viteProcesses) {
        foreach ($process in $viteProcesses) {
            Write-Host "停止Node.js/Vite进程 (PID: $($process.Id))" -ForegroundColor Yellow
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
        Write-Host "前端服务已停止" -ForegroundColor Green
    } else {
        Write-Host "未找到运行中的前端服务" -ForegroundColor Yellow
    }
} catch {
    Write-Host "停止前端服务时出错: $_" -ForegroundColor Red
}

# 检查端口占用情况
Write-Host "检查端口占用情况..." -ForegroundColor Yellow

# 检查3001端口 (用户端前端)
try {
    $port3001 = Get-NetTCPConnection -LocalPort 3001 -ErrorAction SilentlyContinue
    if ($port3001) {
        Write-Host "警告: 端口3001仍被占用" -ForegroundColor Red
        $port3001 | ForEach-Object {
            $process = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "进程: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Red
                Write-Host "正在强制停止..." -ForegroundColor Yellow
                Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            }
        }
    } else {
        Write-Host "端口3001已释放 (用户端前端)" -ForegroundColor Green
    }
} catch {
    Write-Host "检查端口3001时出错: $_" -ForegroundColor Red
}

# 检查3002端口 (管理端前端)
try {
    $port3002 = Get-NetTCPConnection -LocalPort 3002 -ErrorAction SilentlyContinue
    if ($port3002) {
        Write-Host "警告: 端口3002仍被占用" -ForegroundColor Red
        $port3002 | ForEach-Object {
            $process = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "进程: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Red
                Write-Host "正在强制停止..." -ForegroundColor Yellow
                Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            }
        }
    } else {
        Write-Host "端口3002已释放 (管理端前端)" -ForegroundColor Green
    }
} catch {
    Write-Host "检查端口3002时出错: $_" -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "前端服务停止完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
