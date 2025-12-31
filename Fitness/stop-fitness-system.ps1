# 健身管理系统停止脚本
# 此脚本将停止所有与健身管理系统相关的进程

Write-Host "========================================" -ForegroundColor Red
Write-Host "     停止健身管理系统" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red

# 停止后端服务 (Java进程)
Write-Host "正在停止后端服务..." -ForegroundColor Yellow
try {
    $backendProcesses = Get-Process | Where-Object { $_.ProcessName -eq "java" -and $_.CommandLine -like "*spring-boot:run*" }
    if ($backendProcesses) {
        foreach ($process in $backendProcesses) {
            Write-Host "停止Java进程 (PID: $($process.Id))" -ForegroundColor Yellow
            $process.Kill()
        }
        Write-Host "后端服务已停止" -ForegroundColor Green
    } else {
        Write-Host "未找到运行中的后端服务" -ForegroundColor Yellow
    }
} catch {
    Write-Host "停止后端服务时出错: $_" -ForegroundColor Red
}

# 停止前端服务 (Node.js进程)
Write-Host "正在停止前端服务..." -ForegroundColor Yellow
try {
    $frontendProcesses = Get-Process | Where-Object { $_.ProcessName -eq "node" -and $_.CommandLine -like "*vite*" }
    if ($frontendProcesses) {
        foreach ($process in $frontendProcesses) {
            Write-Host "停止Node.js进程 (PID: $($process.Id))" -ForegroundColor Yellow
            $process.Kill()
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

# 检查8080端口
try {
    $port8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
    if ($port8080) {
        Write-Host "警告: 端口8080仍被占用" -ForegroundColor Red
        $port8080 | ForEach-Object {
            $process = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "进程: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "端口8080已释放" -ForegroundColor Green
    }
} catch {
    Write-Host "检查端口8080时出错: $_" -ForegroundColor Red
}

# 检查3001端口
try {
    $port3001 = Get-NetTCPConnection -LocalPort 3001 -ErrorAction SilentlyContinue
    if ($port3001) {
        Write-Host "警告: 端口3001仍被占用" -ForegroundColor Red
        $port3001 | ForEach-Object {
            $process = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "进程: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "端口3001已释放" -ForegroundColor Green
    }
} catch {
    Write-Host "检查端口3001时出错: $_" -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "系统停止完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green