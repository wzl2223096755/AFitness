# =====================================================
# AFitness 健身管理系统 - MySQL 模式启动脚本
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AFitness 健身管理系统 - MySQL 模式" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 MySQL 连接
Write-Host "[1/4] 检查 MySQL 连接..." -ForegroundColor Yellow

$mysqlHost = "localhost"
$mysqlPort = 3306

try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect($mysqlHost, $mysqlPort)
    $tcpClient.Close()
    Write-Host "  ✓ MySQL 服务正在运行 ($mysqlHost`:$mysqlPort)" -ForegroundColor Green
} catch {
    Write-Host "  ✗ 无法连接到 MySQL ($mysqlHost`:$mysqlPort)" -ForegroundColor Red
    Write-Host ""
    Write-Host "请确保 MySQL 服务已启动，或使用以下命令启动 Docker MySQL:" -ForegroundColor Yellow
    Write-Host "  docker-compose up -d mysql" -ForegroundColor White
    Write-Host ""
    Write-Host "或者使用 H2 内存数据库模式:" -ForegroundColor Yellow
    Write-Host "  .\start-h2.ps1" -ForegroundColor White
    exit 1
}

# 检查 JAR 文件
Write-Host "[2/4] 检查 JAR 文件..." -ForegroundColor Yellow

$jarFile = "target/fitness-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "  JAR 文件不存在，正在构建..." -ForegroundColor Yellow
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ✗ 构建失败" -ForegroundColor Red
        exit 1
    }
}
Write-Host "  ✓ JAR 文件就绪" -ForegroundColor Green

# 启动后端服务
Write-Host "[3/4] 启动后端服务..." -ForegroundColor Yellow
Write-Host "  配置: MySQL 数据库" -ForegroundColor Gray
Write-Host "  端口: 8080" -ForegroundColor Gray
Write-Host ""

Start-Process -FilePath "java" -ArgumentList "-jar", $jarFile, "--spring.profiles.active=mysql" -NoNewWindow

# 等待服务启动
Write-Host "[4/4] 等待服务启动..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0
$started = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waited += 2
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $started = $true
            break
        }
    } catch {
        Write-Host "  等待中... ($waited 秒)" -ForegroundColor Gray
    }
}

Write-Host ""
if ($started) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  ✓ 服务启动成功!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "后端 API:     http://localhost:8080" -ForegroundColor White
    Write-Host "Swagger UI:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host "健康检查:     http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host ""
    Write-Host "默认管理员账户:" -ForegroundColor Yellow
    Write-Host "  用户名: admin" -ForegroundColor White
    Write-Host "  密码:   Test123!" -ForegroundColor White
} else {
    Write-Host "  ✗ 服务启动超时，请检查日志" -ForegroundColor Red
}
