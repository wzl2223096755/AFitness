# =====================================================
# AFitness 健身管理系统 - H2 内存数据库模式启动脚本
# 无需安装 MySQL，适合本地开发测试
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AFitness 健身管理系统 - H2 模式" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 JAR 文件
Write-Host "[1/3] 检查 JAR 文件..." -ForegroundColor Yellow

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
Write-Host "[2/3] 启动后端服务..." -ForegroundColor Yellow
Write-Host "  配置: H2 内存数据库" -ForegroundColor Gray
Write-Host "  端口: 8080" -ForegroundColor Gray
Write-Host ""

Start-Process -FilePath "java" -ArgumentList "-jar", $jarFile, "--spring.profiles.active=h2" -NoNewWindow

# 等待服务启动
Write-Host "[3/3] 等待服务启动..." -ForegroundColor Yellow
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
    Write-Host "H2 控制台:    http://localhost:8080/h2-console" -ForegroundColor White
    Write-Host ""
    Write-Host "默认管理员账户:" -ForegroundColor Yellow
    Write-Host "  用户名: admin" -ForegroundColor White
    Write-Host "  密码:   Test123!" -ForegroundColor White
    Write-Host ""
    Write-Host "注意: H2 是内存数据库，重启后数据会丢失" -ForegroundColor Yellow
} else {
    Write-Host "  ✗ 服务启动超时，请检查日志" -ForegroundColor Red
}
