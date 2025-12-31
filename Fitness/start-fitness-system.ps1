# 健身管理系统启动脚本
# 此脚本将启动后端Spring Boot应用和前端Vue应用

Write-Host "========================================" -ForegroundColor Green
Write-Host "     健身管理系统启动脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 检查当前目录
$currentDir = Get-Location
$fitnessDir = "D:\AFitness\Fitness"

if ($currentDir.Path -ne $fitnessDir) {
    Write-Host "切换到项目目录: $fitnessDir" -ForegroundColor Yellow
    Set-Location $fitnessDir
}

# 检查Java和Maven是否安装
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Java，请确保已安装Java并添加到PATH环境变量" -ForegroundColor Red
    exit 1
}

try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "Maven版本: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Maven，请确保已安装Maven并添加到PATH环境变量" -ForegroundColor Red
    exit 1
}

# 检查Node.js和npm是否安装
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

Write-Host "========================================" -ForegroundColor Green
Write-Host "正在启动后端服务..." -ForegroundColor Yellow

# 启动后端服务
$backendJob = Start-Job -ScriptBlock {
    Set-Location "D:\AFitness\Fitness"
    mvn spring-boot:run
}

# 等待一段时间让后端开始启动
Start-Sleep -Seconds 5

Write-Host "正在启动前端服务..." -ForegroundColor Yellow

# 启动前端服务
$frontendJob = Start-Job -ScriptBlock {
    Set-Location "D:\AFitness\Fitness\frontend"
    npm run dev
}

# 等待一段时间让前端开始启动
Start-Sleep -Seconds 5

Write-Host "========================================" -ForegroundColor Green
Write-Host "服务启动中，请稍候..." -ForegroundColor Yellow
Write-Host "后端服务将在 http://localhost:8080 上运行" -ForegroundColor Cyan
Write-Host "前端服务将在 http://localhost:3001 上运行" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green

# 检查后端服务状态
Write-Host "检查后端服务状态..." -ForegroundColor Yellow
$backendReady = $false
$backendCheckCount = 0
$maxBackendChecks = 12  # 最多检查12次，每次等待5秒，总共1分钟

while (-not $backendReady -and $backendCheckCount -lt $maxBackendChecks) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
            Write-Host "后端服务已就绪!" -ForegroundColor Green
        }
    } catch {
        $backendCheckCount++
        Write-Host "等待后端服务启动... ($backendCheckCount/$maxBackendChecks)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $backendReady) {
    Write-Host "警告: 后端服务可能未完全启动，但将继续检查前端服务" -ForegroundColor Yellow
}

# 检查前端服务状态
Write-Host "检查前端服务状态..." -ForegroundColor Yellow
$frontendReady = $false
$frontendCheckCount = 0
$maxFrontendChecks = 6  # 最多检查6次，每次等待5秒，总共30秒

while (-not $frontendReady -and $frontendCheckCount -lt $maxFrontendChecks) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3001" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $frontendReady = $true
            Write-Host "前端服务已就绪!" -ForegroundColor Green
        }
    } catch {
        $frontendCheckCount++
        Write-Host "等待前端服务启动... ($frontendCheckCount/$maxFrontendChecks)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $frontendReady) {
    Write-Host "警告: 前端服务可能未完全启动" -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "系统启动完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "前端应用: http://localhost:3001" -ForegroundColor White
Write-Host "后端API: http://localhost:8080/api/v1" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host "默认管理员账户:" -ForegroundColor Cyan
Write-Host "用户名: admin" -ForegroundColor White
Write-Host "密码: admin123" -ForegroundColor White
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
        Write-Host "健身管理系统运行状态" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "按Ctrl+C停止所有服务" -ForegroundColor Yellow
        Write-Host ""
        
        # 显示后端日志
        Write-Host "后端服务日志:" -ForegroundColor Cyan
        Receive-Job $backendJob -ErrorAction SilentlyContinue | Select-Object -Last 10
        Write-Host ""
        
        # 显示前端日志
        Write-Host "前端服务日志:" -ForegroundColor Cyan
        Receive-Job $frontendJob -ErrorAction SilentlyContinue | Select-Object -Last 10
        
        Start-Sleep -Seconds 5
    }
} finally {
    # 清理作业
    Write-Host "正在停止所有服务..." -ForegroundColor Yellow
    Remove-Job $backendJob -Force
    Remove-Job $frontendJob -Force
    Write-Host "所有服务已停止" -ForegroundColor Green
}