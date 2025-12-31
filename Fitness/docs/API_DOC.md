# AFitness RESTful API 接口文档

## 概述

AFitness是一个专注于力量训练的智能化负荷计算与恢复监控平台，提供完整的健身数据管理功能。

### 基础信息

| 项目 | 说明 |
|------|------|
| 基础URL | `http://localhost:8080/api/v1` |
| 认证方式 | JWT Bearer Token |
| 内容类型 | `application/json` |
| 字符编码 | UTF-8 |

### 认证方式

所有需要认证的接口都需要在请求头中携带JWT令牌：

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...
```

### 统一响应格式

所有API响应遵循统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

## 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 参数错误/业务异常 | 检查请求参数 |
| 401 | 未授权（未登录或Token失效） | 重新登录获取Token |
| 403 | 权限不足 | 检查用户权限 |
| 404 | 资源不存在 | 检查资源ID |
| 500 | 服务器内部错误 | 联系管理员 |
| 1001 | 用户不存在 | 检查用户名 |
| 1002 | 用户名已存在 | 更换用户名 |
| 1003 | 邮箱已存在 | 更换邮箱 |
| 1004 | 登录失败 | 检查凭据 |
| 1005 | 用户名或密码错误 | 检查用户名和密码 |

---

## 1. 认证管理 (Auth)

所有接口前缀：`/api/v1/auth`

### 1.1 用户登录

用户使用用户名和密码登录，成功后返回JWT访问令牌和刷新令牌。

- **URL**: `POST /login`
- **认证**: 不需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| username | String | 是 | 用户名（3-50字符） | admin |
| password | String | 是 | 密码（6-100字符） | 123456 |

**请求示例**:

```json
{
  "username": "admin",
  "password": "123456"
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0NzI4MjU2fQ...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "username": "admin",
    "role": "ROLE_ADMIN"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

**失败响应** (400):

```json
{
  "code": 1005,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": "2024-01-01 12:00:00",
  "success": false
}
```

---

### 1.2 用户注册

新用户注册，需要提供用户名、密码和邮箱。

- **URL**: `POST /register`
- **认证**: 不需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| username | String | 是 | 用户名（3-50字符，只能包含字母、数字和下划线） | fitness_user |
| password | String | 是 | 密码（6-100字符，必须包含大小写字母和数字） | Password123 |
| email | String | 否 | 邮箱 | user@example.com |

**请求示例**:

```json
{
  "username": "fitness_user",
  "password": "Password123",
  "email": "user@example.com"
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "注册成功",
  "data": "注册成功",
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

**失败响应** (400):

```json
{
  "code": 1002,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2024-01-01 12:00:00",
  "success": false
}
```

---

### 1.3 刷新访问令牌

使用刷新令牌获取新的访问令牌，当访问令牌过期时调用此接口。

- **URL**: `POST /refresh`
- **认证**: 不需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**请求示例**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0NzI4MjU2fQ..."
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 1.4 用户登出

用户登出并使当前token失效。

- **URL**: `POST /logout`
- **认证**: 需要

**请求头**:

```http
Authorization: Bearer <your_token>
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "登出成功",
  "data": "登出成功",
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 1.5 获取当前用户信息

验证token并返回当前登录用户的详细信息。

- **URL**: `GET /me`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ROLE_ADMIN",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 1.6 检查用户名是否存在

检查指定的用户名是否已被占用，用于注册前的验证。

- **URL**: `GET /check-username`
- **认证**: 不需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 要检查的用户名 |

**请求示例**:

```
GET /api/v1/auth/check-username?username=fitness_user
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": false,
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

> `data` 为 `true` 表示用户名已存在，`false` 表示可用

---

## 2. 负荷恢复 (Load Recovery)

所有接口前缀：`/api/v1/load-recovery`
*需要登录认证*

### 2.1 计算1RM

根据重量和次数计算1RM（最大重复次数）。

- **URL**: `GET /one-rep-max`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| weight | Double | 否 | 重量（kg） | - |
| reps | Integer | 否 | 重复次数 | - |
| model | String | 否 | 计算公式模型 | Epley |

**支持的计算公式**:

| 模型 | 公式 | 说明 |
|------|------|------|
| Epley | 1RM = weight × (1 + reps/30) | 最常用的公式 |
| Brzycki | 1RM = weight × (36 / (37 - reps)) | 适用于低次数 |
| Lombardi | 1RM = weight × reps^0.1 | 简单公式 |
| OConner | 1RM = weight × (1 + reps/40) | 保守估计 |
| Mayhew | 1RM = (100 × weight) / (52.2 + 41.9 × e^(-0.055 × reps)) | 科学公式 |

**请求示例**:

```
GET /api/v1/load-recovery/one-rep-max?weight=100&reps=8&model=Epley
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "1RM计算成功",
  "data": 125.0,
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.2 获取支持的1RM计算模型

获取系统支持的所有1RM计算公式模型列表。

- **URL**: `GET /one-rep-max/models`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取支持的模型成功",
  "data": ["Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"],
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.3 计算1RM并保存记录

根据重量和次数计算1RM，并保存计算记录。

- **URL**: `POST /one-rep-max/record`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| weight | Double | 是 | 重量（kg） |
| reps | Integer | 是 | 重复次数 |
| model | String | 否 | 计算公式模型（默认Epley） |

**请求示例**:

```json
{
  "weight": 100.0,
  "reps": 8,
  "model": "Epley"
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "1RM记录保存成功",
  "data": {
    "id": 1,
    "weight": 100.0,
    "reps": 8,
    "oneRepMax": 125.0,
    "exerciseType": "STRENGTH",
    "exerciseName": "RM_CALCULATION",
    "timestamp": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.4 保存训练数据

保存训练数据并自动计算训练负荷。

- **URL**: `POST /training-data`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exerciseName | String | 是 | 动作名称 |
| exerciseType | String | 是 | 动作类型（STRENGTH/CARDIO） |
| weight | Double | 否 | 重量（kg） |
| reps | Integer | 否 | 重复次数 |
| sets | Integer | 否 | 组数 |

**请求示例**:

```json
{
  "exerciseName": "深蹲",
  "exerciseType": "STRENGTH",
  "weight": 100.0,
  "reps": 8,
  "sets": 4
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "训练数据保存成功",
  "data": {
    "id": 1,
    "exerciseName": "深蹲",
    "exerciseType": "STRENGTH",
    "weight": 100.0,
    "reps": 8,
    "sets": 4,
    "trainingLoad": 3200.0,
    "oneRepMax": 125.0,
    "timestamp": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.5 评估恢复状态

根据睡眠时长和压力水平评估用户的恢复状态。

- **URL**: `POST /recovery-assessment`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sleepHours | Integer | 是 | 睡眠时长（小时） |
| stressLevel | Integer | 是 | 压力水平（1-10） |

**请求示例**:

```json
{
  "sleepHours": 7,
  "stressLevel": 3
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "恢复状态评估成功",
  "data": {
    "recoveryScore": 75,
    "recoveryStatus": "良好",
    "recommendation": "恢复状态良好，可以进行中等强度训练",
    "suggestedIntensity": "中等",
    "restDaysNeeded": 0
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.6 获取训练建议

根据用户的训练历史和恢复状态生成个性化训练建议。

- **URL**: `GET /training-suggestions`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "训练建议获取成功",
  "data": {
    "suggestedExercises": ["深蹲", "硬拉", "卧推"],
    "suggestedIntensity": "中等",
    "suggestedVolume": 3200,
    "restRecommendation": "建议训练后休息48小时",
    "nutritionTips": ["增加蛋白质摄入", "保持充足水分"]
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 2.7 获取负荷趋势

获取指定日期范围内的训练负荷趋势数据。

- **URL**: `GET /load-trend`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 格式 |
|------|------|------|------|------|
| startDate | LocalDate | 是 | 开始日期 | yyyy-MM-dd |
| endDate | LocalDate | 是 | 结束日期 | yyyy-MM-dd |

**请求示例**:

```
GET /api/v1/load-recovery/load-trend?startDate=2024-01-01&endDate=2024-01-07
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "负荷趋势获取成功",
  "data": {
    "2024-01-01": 3200.0,
    "2024-01-02": 2800.0,
    "2024-01-03": 0.0,
    "2024-01-04": 3500.0,
    "2024-01-05": 3000.0
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

## 3. 训练管理 (Training)

所有接口前缀：`/api/v1/training`
*需要登录认证*

### 3.1 创建训练记录

创建新的训练记录，包含训练动作详情。

- **URL**: `POST /record`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| trainingDate | LocalDate | 是 | 训练日期 |
| totalVolume | Double | 否 | 总训练量 |
| trainingStress | Double | 否 | 训练压力 |
| exerciseDetails | Array | 是 | 动作详情列表 |

**exerciseDetails 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exerciseName | String | 是 | 动作名称 |
| weight | Double | 是 | 重量（kg） |
| sets | Integer | 是 | 组数 |
| reps | Integer | 是 | 每组次数 |
| rpe | Integer | 否 | RPE评分（1-10） |
| exerciseType | String | 否 | 动作类型 |

**请求示例**:

```json
{
  "trainingDate": "2024-01-01",
  "totalVolume": 5600,
  "trainingStress": 75,
  "exerciseDetails": [
    {
      "exerciseName": "深蹲",
      "weight": 100.0,
      "sets": 4,
      "reps": 8,
      "rpe": 8,
      "exerciseType": "COMPOUND"
    },
    {
      "exerciseName": "腿举",
      "weight": 150.0,
      "sets": 3,
      "reps": 12,
      "rpe": 7,
      "exerciseType": "COMPOUND"
    }
  ]
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "训练记录创建成功",
  "data": {
    "id": 1,
    "exerciseName": "深蹲",
    "sets": 4,
    "reps": 8,
    "weight": 100.0,
    "trainingDate": "2024-01-01",
    "totalVolume": 5600.0,
    "trainingStress": 75.0,
    "calculatedTotalVolume": 5600.0,
    "createdAt": "2024-01-01T12:00:00",
    "exerciseDetails": [...]
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 3.2 获取训练记录列表

获取当前登录用户的所有训练记录。

- **URL**: `GET /records`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取训练记录成功",
  "data": [
    {
      "id": 1,
      "exerciseName": "深蹲",
      "sets": 4,
      "reps": 8,
      "weight": 100.0,
      "trainingDate": "2024-01-01",
      "totalVolume": 3200.0,
      "createdAt": "2024-01-01T12:00:00"
    },
    {
      "id": 2,
      "exerciseName": "卧推",
      "sets": 3,
      "reps": 10,
      "weight": 80.0,
      "trainingDate": "2024-01-01",
      "totalVolume": 2400.0,
      "createdAt": "2024-01-01T11:00:00"
    }
  ],
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 3.3 获取训练记录（分页）

获取当前登录用户的训练记录，支持分页和排序。

- **URL**: `GET /records/page`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| page | Integer | 否 | 页码（从0开始） | 0 |
| size | Integer | 否 | 每页大小（1-100） | 20 |
| sortBy | String | 否 | 排序字段 | trainingDate |
| sortDirection | String | 否 | 排序方向（asc/desc） | desc |

**请求示例**:

```
GET /api/v1/training/records/page?page=0&size=10&sortBy=trainingDate&sortDirection=desc
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

## 4. 营养记录管理 (Nutrition)

所有接口前缀：`/api/v1/nutrition`
*需要登录认证*

### 4.1 获取指定日期的营养记录

根据日期获取用户当天的所有营养记录。

- **URL**: `GET /records/{date}`
- **认证**: 需要

**路径参数**:

| 参数 | 类型 | 说明 | 格式 |
|------|------|------|------|
| date | LocalDate | 日期 | yyyy-MM-dd |

**请求示例**:

```
GET /api/v1/nutrition/records/2024-01-01
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "recordDate": "2024-01-01",
      "mealType": "早餐",
      "foodName": "鸡蛋",
      "calories": 150,
      "protein": 12.0,
      "carbs": 1.0,
      "fat": 10.0,
      "amount": 2,
      "createdAt": "2024-01-01T08:00:00"
    },
    {
      "id": 2,
      "recordDate": "2024-01-01",
      "mealType": "午餐",
      "foodName": "鸡胸肉",
      "calories": 300,
      "protein": 50.0,
      "carbs": 0.0,
      "fat": 8.0,
      "amount": 200,
      "createdAt": "2024-01-01T12:00:00"
    }
  ],
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 4.2 添加营养记录

添加一条新的营养记录。

- **URL**: `POST /records`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| recordDate | LocalDate | 是 | 记录日期 |
| mealType | String | 是 | 餐次（早餐/午餐/晚餐/加餐） |
| foodName | String | 是 | 食物名称 |
| calories | Integer | 否 | 热量（kcal） |
| protein | Double | 否 | 蛋白质（g） |
| carbs | Double | 否 | 碳水化合物（g） |
| fat | Double | 否 | 脂肪（g） |
| fiber | Double | 否 | 纤维（g） |
| amount | Double | 否 | 份量（g） |
| notes | String | 否 | 备注 |

**请求示例**:

```json
{
  "recordDate": "2024-01-01",
  "mealType": "午餐",
  "foodName": "鸡胸肉",
  "calories": 300,
  "protein": 50.0,
  "carbs": 0.0,
  "fat": 8.0,
  "amount": 200,
  "notes": "水煮"
}
```

**成功响应** (201):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "recordDate": "2024-01-01",
    "mealType": "午餐",
    "foodName": "鸡胸肉",
    "calories": 300,
    "protein": 50.0,
    "carbs": 0.0,
    "fat": 8.0,
    "amount": 200,
    "notes": "水煮",
    "createdAt": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 4.3 获取营养统计

获取指定日期的营养摄入统计数据。

- **URL**: `GET /stats/{date}`
- **认证**: 需要

**请求示例**:

```
GET /api/v1/nutrition/stats/2024-01-01
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "date": "2024-01-01",
    "totalCalories": 2100,
    "totalProtein": 150.0,
    "totalCarbs": 200.0,
    "totalFat": 70.0,
    "totalFiber": 25.0,
    "mealBreakdown": {
      "早餐": { "calories": 500, "protein": 30.0 },
      "午餐": { "calories": 800, "protein": 60.0 },
      "晚餐": { "calories": 700, "protein": 50.0 },
      "加餐": { "calories": 100, "protein": 10.0 }
    }
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 4.4 获取推荐营养摄入

根据用户信息和目标计算推荐的每日营养摄入。

- **URL**: `GET /recommendation`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 可选值 |
|------|------|------|------|--------|
| trainingGoal | String | 否 | 训练目标 | fat_loss, muscle_gain, maintenance |
| activityLevel | String | 否 | 活动水平 | sedentary, light, moderate, active, very_active |

**请求示例**:

```
GET /api/v1/nutrition/recommendation?trainingGoal=muscle_gain&activityLevel=active
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "dailyCalories": 2800,
    "proteinGrams": 175,
    "carbsGrams": 350,
    "fatGrams": 78,
    "fiberGrams": 35,
    "waterLiters": 3.5,
    "macroRatio": {
      "protein": 25,
      "carbs": 50,
      "fat": 25
    }
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

## 5. 仪表盘管理 (Dashboard)

所有接口前缀：`/api/v1/dashboard`
*需要登录认证*

### 5.1 获取仪表盘指标概览

获取用户的训练指标概览数据。

- **URL**: `GET /metrics-overview`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| timeRange | String | 否 | 时间范围 | week |

**可选时间范围**: `day`, `week`, `month`, `year`

**请求示例**:

```
GET /api/v1/dashboard/metrics-overview?timeRange=week
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取指标概览成功",
  "data": {
    "totalWorkouts": 5,
    "totalVolume": 15000.0,
    "averageIntensity": 72.5,
    "caloriesBurned": 2500,
    "recoveryScore": 78,
    "weeklyProgress": {
      "volumeChange": 12.5,
      "intensityChange": 5.0
    }
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 5.2 获取用户统计概览

获取用户的统计数据概览。

- **URL**: `GET /user-stats-overview`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取用户统计概览成功",
  "data": {
    "totalTrainingDays": 120,
    "currentStreak": 7,
    "longestStreak": 21,
    "totalVolume": 500000.0,
    "personalRecords": {
      "深蹲": 150.0,
      "卧推": 100.0,
      "硬拉": 180.0
    },
    "favoriteExercises": ["深蹲", "卧推", "硬拉"]
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 5.3 获取分析数据

获取用户的训练分析数据。

- **URL**: `GET /analytics`
- **认证**: 需要

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| timeRange | String | 否 | 时间范围 | week |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取分析数据成功",
  "data": {
    "volumeTrend": [
      { "date": "2024-01-01", "value": 3200 },
      { "date": "2024-01-02", "value": 2800 },
      { "date": "2024-01-03", "value": 0 },
      { "date": "2024-01-04", "value": 3500 }
    ],
    "muscleGroupDistribution": {
      "胸部": 25,
      "背部": 25,
      "腿部": 30,
      "肩部": 10,
      "手臂": 10
    },
    "intensityDistribution": {
      "低强度": 20,
      "中强度": 50,
      "高强度": 30
    }
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 5.4 获取最近训练记录

获取用户最近的训练记录列表。

- **URL**: `GET /recent-training-records`
- **认证**: 需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取最近训练记录成功",
  "data": [
    {
      "id": 1,
      "exerciseName": "深蹲",
      "trainingDate": "2024-01-01",
      "totalVolume": 3200.0,
      "duration": 45
    },
    {
      "id": 2,
      "exerciseName": "卧推",
      "trainingDate": "2024-01-01",
      "totalVolume": 2400.0,
      "duration": 30
    }
  ],
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

## 6. 健康检查 (Health)

所有接口前缀：`/api/v1/health` 或 `/actuator`

### 6.1 基础健康检查

检查系统基础健康状态。

- **URL**: `GET /api/v1/health`
- **认证**: 不需要

**成功响应** (200):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01 12:00:00",
  "success": true
}
```

---

### 6.2 Actuator健康检查

Spring Boot Actuator健康检查端点。

- **URL**: `GET /actuator/health`
- **认证**: 不需要

**成功响应** (200):

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 200000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## 附录

### A. 数据类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| LocalDate | 日期 | 2024-01-01 |
| LocalDateTime | 日期时间 | 2024-01-01T12:00:00 |
| Double | 浮点数 | 100.5 |
| Integer | 整数 | 10 |
| Long | 长整数 | 1234567890 |
| String | 字符串 | "hello" |
| Boolean | 布尔值 | true/false |

### B. 常用请求头

| 请求头 | 说明 | 示例 |
|--------|------|------|
| Authorization | JWT认证令牌 | Bearer eyJhbGciOiJIUzUxMiJ9... |
| Content-Type | 内容类型 | application/json |
| Accept | 接受类型 | application/json |

### C. 分页响应格式

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### D. Swagger UI 访问

开发环境下可通过以下地址访问Swagger UI：

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

---

*文档版本: 1.0.0*
*最后更新: 2024-01-01*
