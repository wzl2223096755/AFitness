# 角色管理API测试文档

## 系统账户信息

### 默认管理员账户
- **用户名**: admin
- **密码**: admin123
- **邮箱**: admin@fitness.com
- **角色**: ADMIN

### 测试普通用户账户
- **用户名**: testuser
- **密码**: user123
- **邮箱**: user@test.com
- **角色**: USER

## API测试流程

### 1. 管理员登录
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh_token_here",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### 2. 获取所有用户角色信息
```bash
GET /api/admin/roles/users
Authorization: Bearer {access_token}
```

### 3. 更新用户角色
```bash
PUT /api/admin/roles/users/{userId}/role?newRole=USER
Authorization: Bearer {access_token}
```

### 4. 批量更新用户角色
```bash
PUT /api/admin/roles/users/batch-role?newRole=ADMIN
Authorization: Bearer {access_token}
Content-Type: application/json

[1, 2, 3]  // 用户ID列表
```

### 5. 获取角色统计信息
```bash
GET /api/admin/roles/statistics
Authorization: Bearer {access_token}
```

### 6. 检查特定用户角色
```bash
GET /api/admin/roles/users/{userId}/role
Authorization: Bearer {access_token}
```

## 权限测试

### 测试管理员权限
管理员可以访问以下接口：
- `/api/admin/roles/*` - 角色管理相关接口
- `/api/admin/users/*` - 用户管理接口
- `/api/admin/cleanup/*` - 数据清理接口

### 测试普通用户权限
普通用户只能访问：
- `/api/auth/login` - 登录
- `/api/auth/register` - 注册
- `/api/fitness/calculate` - 健身计算
- `/api/fitness/analysis` - 分析报告
- `/api/user/profile` - 个人资料

### 测试权限拒绝
使用普通用户token访问管理员接口应该返回403错误：
```bash
GET /api/admin/roles/users
Authorization: Bearer {user_token}
```

**预期响应:**
```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

## 创建测试用户

### 注册新用户
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser2",
  "email": "test2@example.com",
  "password": "password123"
}
```

### 使用管理员账户提升权限
```bash
PUT /api/admin/roles/users/{new_user_id}/role?newRole=ADMIN
Authorization: Bearer {admin_token}
```

## 系统功能验证

### 1. 权限拦截器验证
- 未登录用户访问受保护接口 → 401错误
- 普通用户访问管理员接口 → 403错误
- 管理员访问所有接口 → 正常访问

### 2. 角色管理验证
- 查看用户角色信息
- 更新用户角色
- 批量角色操作
- 角色统计信息

### 3. 数据安全验证
- 用户只能访问自己的数据
- 管理员可以访问所有数据
- 敏感操作需要管理员权限

## 注意事项

1. **首次使用**: 系统启动时会自动创建admin账户
2. **安全建议**: 首次登录后请修改默认密码
3. **权限管理**: 谨慎分配ADMIN权限，确保系统安全
4. **测试环境**: 建议在测试环境充分验证后再部署到生产环境

## 错误处理

### 常见错误码
- **401**: 未登录或token无效
- **403**: 权限不足
- **404**: 用户不存在
- **409**: 用户名或邮箱已存在
- **500**: 服务器内部错误

### 错误响应格式
```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```