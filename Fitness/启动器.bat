@echo off
chcp 65001 >nul
title 健身管理系统启动器

echo ========================================
echo      健身管理系统启动器
echo ========================================
echo.
echo 1. 启动系统
echo 2. 停止系统
echo 3. 退出
echo.
set /p choice=请选择操作 (1-3): 

if "%choice%"=="1" (
    echo 正在启动健身管理系统...
    powershell -ExecutionPolicy Bypass -File "%~dp0start-fitness-system.ps1"
) else if "%choice%"=="2" (
    echo 正在停止健身管理系统...
    powershell -ExecutionPolicy Bypass -File "%~dp0stop-fitness-system.ps1"
) else if "%choice%"=="3" (
    echo 退出程序
    exit /b
) else (
    echo 无效选择，请重新运行程序
    pause
)

pause