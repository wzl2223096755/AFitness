# Implementation Plan: Admin/User Frontend Separation

## Overview

将健身管理系统前端分离为用户端和管理端两套独立应用，共享公共代码。

## Tasks

- [x] 1. 创建共享代码目录结构
  - [x] 1.1 创建 `Fitness/shared/` 目录
    - 创建 api/, utils/, styles/ 子目录
    - _Requirements: 3.1, 3.2, 3.3, 5.1_
  
  - [x] 1.2 迁移共享API模块到 shared/api/
    - 移动 request.js, auth.js, fitness.js
    - 创建 admin.js API模块
    - _Requirements: 3.1_
  
  - [x] 1.3 迁移共享工具函数到 shared/utils/
    - 移动 message.js, errorHandler.js, performance.js
    - _Requirements: 3.2_
  
  - [x] 1.4 迁移共享样式到 shared/styles/
    - 移动 variables.scss, mixins.scss, base.scss
    - _Requirements: 3.3_

- [x] 2. 重构用户端前端
  - [x] 2.1 更新用户端路由配置
    - 移除管理员相关路由 (Users.vue)
    - 添加管理员角色重定向逻辑
    - _Requirements: 1.3, 1.4, 1.5_
  
  - [x] 2.2 更新用户端导入路径
    - 更新API导入指向 shared/api/
    - 更新工具函数导入指向 shared/utils/
    - 更新样式导入指向 shared/styles/
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [x] 2.3 更新用户端 vite.config.js
    - 配置端口为 3001
    - 配置 alias 指向 shared 目录
    - _Requirements: 1.1_

- [x] 3. 创建管理端前端应用
  - [x] 3.1 初始化管理端项目结构
    - 创建 `Fitness/admin/` 目录
    - 创建 package.json (复制并修改)
    - 创建 vite.config.js (端口 3002)
    - 创建 index.html
    - _Requirements: 2.1, 5.1_
  
  - [x] 3.2 创建管理端入口文件
    - 创建 src/main.js
    - 创建 src/App.vue
    - _Requirements: 2.2_
  
  - [x] 3.3 创建管理端路由配置
    - 创建 src/router/index.js
    - 配置管理端路由
    - 添加非管理员拒绝逻辑
    - _Requirements: 2.3, 2.4_
  
  - [x] 3.4 创建管理端登录页面
    - 创建 src/views/AdminLogin.vue
    - 实现管理员专用登录界面
    - 添加角色验证逻辑
    - _Requirements: 2.2, 2.4_
  
  - [x] 3.5 创建管理仪表盘页面
    - 创建 src/views/AdminDashboard.vue
    - 显示系统概览统计
    - _Requirements: 2.3_
  
  - [x] 3.6 创建用户管理页面
    - 创建 src/views/UserManagement.vue
    - 实现用户列表、搜索、编辑、删除功能
    - _Requirements: 2.3_
  
  - [x] 3.7 创建系统统计页面
    - 创建 src/views/SystemStats.vue
    - 显示系统使用统计图表
    - _Requirements: 2.3_
  
  - [x] 3.8 创建审计日志页面
    - 创建 src/views/AuditLogs.vue
    - 显示操作日志列表
    - _Requirements: 2.3_
  
  - [x] 3.9 创建系统设置页面
    - 创建 src/views/SystemSettings.vue
    - 实现系统配置管理
    - _Requirements: 2.3_

- [x] 4. 更新后端CORS配置
  - [x] 4.1 更新 SecurityConfig.java
    - 添加 localhost:3002 到允许的源
    - _Requirements: 4.1, 4.2_
  
  - [x] 4.2 更新 application.properties
    - 添加 3002 端口到 CORS 配置
    - _Requirements: 4.1, 4.2_

- [x] 5. 创建管理端API接口
  - [x] 5.1 创建 shared/api/admin.js
    - 实现用户管理API
    - 实现系统统计API
    - 实现审计日志API
    - 实现系统设置API
    - _Requirements: 2.3_

- [x] 6. Checkpoint - 验证基础功能
  - 确保用户端在 3001 端口正常运行
  - 确保管理端在 3002 端口正常运行
  - 确保共享代码正确导入
  - 确保后端CORS配置正确

- [ ]* 7. 编写测试
  - [ ]* 7.1 编写角色重定向测试
    - **Property 1: Admin Role Redirect**
    - **Validates: Requirements 1.5**
  
  - [ ]* 7.2 编写非管理员拒绝测试
    - **Property 2: Non-Admin Access Rejection**
    - **Validates: Requirements 2.4**

- [x] 8. 创建启动脚本
  - [x] 8.1 创建同时启动两个前端的脚本
    - 创建 start-all.ps1 脚本
    - _Requirements: 5.2_

- [x] 9. Final Checkpoint
  - 确保所有功能正常工作
  - 确保用户端和管理端完全分离
  - 确保共享代码正确工作

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
