#!/bin/bash

echo "=== 测试用户资料更新API ==="

# 测试用户登录获取token
echo "1. 测试用户登录..."
LOGIN_RESPONSE=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "登录失败，无法获取token"
  exit 1
fi

echo "获取到token: $TOKEN"

# 测试获取用户资料
echo "2. 测试获取用户资料..."
PROFILE_RESPONSE=$(curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN")

echo "用户资料响应: $PROFILE_RESPONSE"

# 测试更新用户资料
echo "3. 测试更新用户资料..."
UPDATE_RESPONSE=$(curl -X PUT http://localhost:8080/api/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "age": 30,
    "weight": 75.0,
    "gender": "男",
    "height": 175,
    "experienceLevel": "intermediate"
  }')

echo "更新资料响应: $UPDATE_RESPONSE"

# 测试修改密码
echo "4. 测试修改密码..."
PASSWORD_RESPONSE=$(curl -X POST http://localhost:8080/api/users/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "oldPassword": "admin123",
    "newPassword": "newadmin123",
    "confirmPassword": "newadmin123"
  }')

echo "修改密码响应: $PASSWORD_RESPONSE"

echo "=== 测试完成 ==="