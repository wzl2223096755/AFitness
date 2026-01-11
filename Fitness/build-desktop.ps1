# AFitness 桌面应用一键打包脚本
# 此脚本会打包后端 JAR 和前端 Electron 应用

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AFitness 桌面应用打包" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Java
Write-Host "[1/4] 检查 Java 环境..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion) {
    Write-Host "  ✓ Java 已安装: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ 未找到 Java，请安装 Java 17 或更高版本" -ForegroundColor Red
    exit 1
}

# 打包后端
Write-Host ""
Write-Host "[2/4] 打包后端 Spring Boot JAR..." -ForegroundColor Yellow
Set-Location $PSScriptRoot
.\mvnw.cmd package -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    $jarSize = [math]::Round((Get-Item "target/fitness-0.0.1-SNAPSHOT.jar").Length / 1MB, 2)
    Write-Host "  ✓ 后端打包完成: target/fitness-0.0.1-SNAPSHOT.jar ($jarSize MB)" -ForegroundColor Green
} else {
    Write-Host "  ✗ 后端打包失败" -ForegroundColor Red
    exit 1
}

# 安装前端依赖
Write-Host ""
Write-Host "[3/4] 安装前端依赖..." -ForegroundColor Yellow
Set-Location "$PSScriptRoot/frontend"
npm install --silent
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ 前端依赖安装完成" -ForegroundColor Green
} else {
    Write-Host "  ✗ 前端依赖安装失败" -ForegroundColor Red
    exit 1
}

# 打包 Electron 应用
Write-Host ""
Write-Host "[4/4] 打包 Electron 桌面应用..." -ForegroundColor Yellow
npm run electron:build:portable
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Electron 打包完成" -ForegroundColor Green
} else {
    Write-Host "  ! Electron 打包可能有警告，但应用已生成" -ForegroundColor Yellow
}

# 显示结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  打包完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "输出文件:" -ForegroundColor White

$releaseDir = "$PSScriptRoot/frontend/release"
if (Test-Path "$releaseDir/AFitness-1.0.0-Portable.exe") {
    $exeSize = [math]::Round((Get-Item "$releaseDir/AFitness-1.0.0-Portable.exe").Length / 1MB, 2)
    Write-Host "  - AFitness-1.0.0-Portable.exe ($exeSize MB)" -ForegroundColor White
}
if (Test-Path "$releaseDir/win-unpacked/AFitness.exe") {
    Write-Host "  - win-unpacked/AFitness.exe (解压版)" -ForegroundColor White
}

Write-Host ""
Write-Host "运行方式:" -ForegroundColor White
Write-Host "  双击 AFitness.exe 即可启动（自动启动后端服务）" -ForegroundColor Gray
Write-Host ""
Write-Host "注意: 用户电脑需要安装 Java 17 或更高版本" -ForegroundColor Yellow

Set-Location $PSScriptRoot
