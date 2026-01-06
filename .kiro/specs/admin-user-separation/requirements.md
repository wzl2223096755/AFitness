# Requirements Document

## Introduction

将健身管理系统的前端分离为两套独立的应用：用户端（普通用户使用）和管理端（管理员使用），分别运行在不同端口，提供不同的功能和界面。

## Glossary

- **User_Frontend**: 用户端前端应用，供普通健身用户使用
- **Admin_Frontend**: 管理端前端应用，供系统管理员使用
- **Backend_API**: 后端API服务，为两个前端提供数据服务

## Requirements

### Requirement 1: 用户端前端应用

**User Story:** As a fitness user, I want to access a dedicated user interface, so that I can manage my personal training data without administrative features.

#### Acceptance Criteria

1. THE User_Frontend SHALL run on port 3001
2. WHEN a user accesses the User_Frontend, THE System SHALL display login page for regular users
3. THE User_Frontend SHALL include: Dashboard, Training Data, Training Plans, Nutrition Tracking, Recovery Status, Training Suggestions, History Statistics, User Profile, Settings
4. THE User_Frontend SHALL NOT include administrative features like user management
5. WHEN a user logs in with ADMIN role, THE User_Frontend SHALL redirect to Admin_Frontend

### Requirement 2: 管理端前端应用

**User Story:** As an administrator, I want to access a dedicated admin interface, so that I can manage users and system settings.

#### Acceptance Criteria

1. THE Admin_Frontend SHALL run on port 3002
2. WHEN an admin accesses the Admin_Frontend, THE System SHALL display admin login page
3. THE Admin_Frontend SHALL include: Admin Dashboard, User Management, System Statistics, Audit Logs, System Settings
4. WHEN a non-admin user attempts to login, THE Admin_Frontend SHALL reject the login and display error message
5. THE Admin_Frontend SHALL have a distinct visual theme to differentiate from User_Frontend

### Requirement 3: 共享代码和组件

**User Story:** As a developer, I want to share common code between both frontends, so that I can maintain consistency and reduce duplication.

#### Acceptance Criteria

1. THE System SHALL share API modules between both frontends
2. THE System SHALL share utility functions between both frontends
3. THE System SHALL share base styles and theme configuration between both frontends
4. WHEN updating shared code, THE System SHALL reflect changes in both frontends

### Requirement 4: 后端CORS配置

**User Story:** As a system, I want to support both frontend ports, so that both applications can communicate with the backend.

#### Acceptance Criteria

1. THE Backend_API SHALL allow CORS requests from port 3001 (User_Frontend)
2. THE Backend_API SHALL allow CORS requests from port 3002 (Admin_Frontend)
3. WHEN a request comes from an unauthorized origin, THE Backend_API SHALL reject it

### Requirement 5: 项目结构

**User Story:** As a developer, I want a clear project structure, so that I can easily maintain both frontends.

#### Acceptance Criteria

1. THE System SHALL organize code as:
   - `Fitness/frontend/` - User Frontend (port 3001)
   - `Fitness/admin/` - Admin Frontend (port 3002)
   - `Fitness/shared/` - Shared code (API, utils, base styles)
2. WHEN running development mode, THE System SHALL support running both frontends simultaneously
