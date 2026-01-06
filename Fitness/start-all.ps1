# 健身管理系统前端启动脚本
# 此脚本将同时启动用户端前端和管理端前端

Write-Host "========================================" -ForegroundColor Green
Write-Host "     健身管理系统前端启动脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 获取脚本所在目录
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$fitnessDir = $scriptDir

# 切换到项目目录
Write-Host "项目目录: $fitnessDir" -ForegroundColor Yellow
Set-Location $fitnessDir

# 检查Node.js和npm是否安装
Write-Host "检查环境依赖..." -ForegroundColor Yellow
try {
    $nodeVersion = node -v
    Write-Host "Node.js版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Node.js，请确保已安装Node.js并添加到PATH环境变量" -ForegroundColor Red
    exit 1
}

try {
    $npmVersion = npm -v
    Write-Host "NPM版本: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到NPM，请确保已安装NPM并添加到PATH环境变量" -ForegroundColor Red
    exit 1
}

# 检查前端目录是否存在
$frontendDir = Join-Path $fitnessDir "frontend"
$adminDir = Join-Path $fitnessDir "admin"

if (-not (Test-Path $frontendDir)) {
    Write-Host "错误: 用户端前端目录不存在: $frontendDir" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $adminDir)) {
    Write-Host "错误: 管理端前端目录不存在: $adminDir" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "正在启动用户端前端 (Port 3001)..." -ForegroundColor Yellow

# 启动用户端前端服务
$userFrontendJob = Start-Job -ScriptBlock {
    param($dir)
    Set-Location $dir
    npm run dev
} -ArgumentList $frontendDir

# 等待一段时间
Start-Sleep -Seconds 2

Write-Host "正在启动管理端前端 (Port 3002)..." -ForegroundColor Yellow

# 启动管理端前端服务
$adminFrontendJob = Start-Job -ScriptBlock {
    param($dir)
    Set-Location $dir
    npm run dev
} -ArgumentList $adminDir

# 等待服务启动
Start-Sleep -Seconds 5

Write-Host "========================================" -ForegroundColor Green
Write-Host "前端服务启动中，请稍候..." -ForegroundColor Yellow
Write-Host "用户端前端将在 http://localhost:3001 上运行" -ForegroundColor Cyan
Write-Host "管理端前端将在 http://localhost:3002 上运行" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green

# 检查用户端前端服务状态
Write-Host "检查用户端前端服务状态..." -ForegroundColor Yellow
$userFrontendReady = $false
$userCheckCount = 0
$maxChecks = 12

while (-not $userFrontendReady -and $userCheckCount -lt $maxChecks) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3001" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $userFrontendReady = $true
            Write-Host "用户端前端服务已就绪!" -ForegroundColor Green
        }
    } catch {
        $userCheckCount++
        Write-Host "等待用户端前端服务启动... ($userCheckCount/$maxChecks)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $userFrontendReady) {
    Write-Host "警告: 用户端前端服务可能未完全启动" -ForegroundColor Yellow
}

# 检查管理端前端服务状态
Write-Host "检查管理端前端服务状态..." -ForegroundColor Yellow
$adminFrontendReady = $false
$adminCheckCount = 0

while (-not $adminFrontendReady -and $adminCheckCount -lt $maxChecks) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3002" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $adminFrontendReady = $true
            Write-Host "管理端前端服务已就绪!" -ForegroundColor Green
        }
    } catch {
        $adminCheckCount++
        Write-Host "等待管理端前端服务启动... ($adminCheckCount/$maxChecks)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $adminFrontendReady) {
    Write-Host "警告: 管理端前端服务可能未完全启动" -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "前端服务启动完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "用户端前端: http://localhost:3001" -ForegroundColor White
Write-Host "管理端前端: http://localhost:3002" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host "默认账户:" -ForegroundColor Cyan
Write-Host "普通用户: user / user123" -ForegroundColor White
Write-Host "管理员: admin / admin123" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host "按任意键查看服务日志，或按Ctrl+C停止所有服务" -ForegroundColor Yellow

# 等待用户输入
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 显示服务日志
Write-Host "========================================" -ForegroundColor Green
Write-Host "显示服务日志 (按Ctrl+C停止)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 创建一个循环来显示日志，直到用户停止
try {
    while ($true) {
        Clear-Host
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "健身管理系统前端运行状态" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "用户端: http://localhost:3001" -ForegroundColor Cyan
        Write-Host "管理端: http://localhost:3002" -ForegroundColor Cyan
        Write-Host "按Ctrl+C停止所有服务" -ForegroundColor Yellow
        Write-Host ""
        
        # 显示用户端前端日志
        Write-Host "用户端前端日志:" -ForegroundColor Cyan
        Receive-Job $userFrontendJob -ErrorAction SilentlyContinue | Select-Object -Last 10
        Write-Host ""
        
        # 显示管理端前端日志
        Write-Host "管理端前端日志:" -ForegroundColor Cyan
        Receive-Job $adminFrontendJob -ErrorAction SilentlyContinue | Select-Object -Last 10
        
        Start-Sleep -Seconds 5
    }
} finally {
    # 清理作业
    Write-Host "正在停止所有前端服务..." -ForegroundColor Yellow
    Remove-Job $userFrontendJob -Force -ErrorAction SilentlyContinue
    Remove-Job $adminFrontendJob -Force -ErrorAction SilentlyContinue
    Write-Host "所有前端服务已停止" -ForegroundColor Green
}
