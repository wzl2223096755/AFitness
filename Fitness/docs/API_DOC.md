# AFitness RESTful API 接口文档

## 1. 认证管理 (Auth)
所有接口前缀：`/api/v1/auth`

### 1.1 用户注册
- **URL**: `/register`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }
  ```
- **响应格式**: `ApiResponse<User>`

### 1.2 用户登录
- **URL**: `/login`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "username": "testuser",
    "password": "password123"
  }
  ```
- **响应格式**: `ApiResponse<LoginResponse>` (含Token)

---

## 2. 算法计算 (Calculation)
所有接口前缀：`/api/v1/calculate`
*需要登录认证*

### 2.1 计算1RM (One Rep Max)
- **URL**: `/1rm`
- **Method**: `GET`
- **请求参数**:
  - `weight` (Double): 重量
  - `reps` (Integer): 次数
  - `model` (String, 可选): 公式模型 (Epley, Brzycki, Lombardi, OConner, Mayhew)，默认为 Brzycki
- **响应格式**: `ApiResponse<Map>`

### 2.2 计算训练容量 (Training Volume)
- **URL**: `/volume`
- **Method**: `GET`
- **请求参数**:
  - `weight` (Double): 重量
  - `sets` (Integer): 组数
  - `reps` (Integer): 次数
- **响应格式**: `ApiResponse<Map>`

### 2.3 计算热量消耗 (Calories Burned)
- **URL**: `/calories`
- **Method**: `GET`
- **请求参数**:
  - `duration` (Double): 持续时间(分钟)
  - `intensity` (Double, 可选): 强度系数，默认为 5.0
- **响应格式**: `ApiResponse<Map>`

### 2.4 获取计算历史
- **URL**: `/history`
- **Method**: `GET`
- **响应格式**: `ApiResponse<List<CalculationRecord>>`

---

## 3. 训练计划 (Training Plan)
所有接口前缀：`/api/v1/training-plans`
*需要登录认证*

### 3.1 获取我的所有计划
- **URL**: `/`
- **Method**: `GET`
- **响应格式**: `ApiResponse<List<TrainingPlan>>`

### 3.2 创建新计划
- **URL**: `/`
- **Method**: `POST`
- **请求参数**: `TrainingPlanRequestDTO`
- **响应格式**: `ApiResponse<TrainingPlan>`

### 3.3 更新计划
- **URL**: `/{id}`
- **Method**: `PUT`
- **响应格式**: `ApiResponse<TrainingPlan>`

### 3.4 删除计划
- **URL**: `/{id}`
- **Method**: `DELETE`
- **响应格式**: `ApiResponse<Void>`

---

## 4. 用户管理 (User Management)
所有接口前缀：`/api/v1/users`

### 4.1 获取当前用户信息
- **URL**: `/current`
- **Method**: `GET`
- **响应格式**: `ApiResponse<User>`

### 4.2 更新用户信息 (管理员)
- **URL**: `/{id}`
- **Method**: `PUT`
- **权限**: `ADMIN`
- **响应格式**: `ApiResponse<User>`

---

## 状态码说明
| 状态码 | 说明 |
| :--- | :--- |
| 200 | 请求成功 |
| 400 | 业务异常/参数错误 |
| 401 | 未授权 (未登录或Token失效) |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |
