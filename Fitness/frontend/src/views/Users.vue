<template>
  <div class="users">
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">用户管理</h3>
        <el-button type="primary" @click="showAddUserDialog = true">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
      
      <div class="user-table">
        <el-table :data="fitnessStore.users" style="width: 100%" v-loading="fitnessStore.loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="200" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="scope">
              {{ scope?.row?.createdAt ? formatDate(scope.row.createdAt) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="viewUser(scope?.row)">查看</el-button>
              <el-button size="small" type="primary" @click="editUser(scope?.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteUser(scope?.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加用户对话框 -->
    <el-dialog v-model="showAddUserDialog" title="添加用户" width="500px">
      <el-form :model="newUser" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddUserDialog = false">取消</el-button>
        <el-button type="primary" @click="addUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from '../utils/message.js'

import { useFitnessStore } from '../stores/fitness'
import { fitnessApi } from '@/api/fitness'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const fitnessStore = useFitnessStore()

// 响应式数据
const showAddUserDialog = ref(false)
const userFormRef = ref(null)
const newUser = ref({
  username: '',
  email: '',
  password: ''
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 查看用户
const viewUser = (user) => {
  if (!user) {
    ElMessage.warning('无法查看用户信息')
    return
  }
  ElMessage.info(`查看用户: ${user.username || '未知用户'}`)
}

// 编辑用户
const editUser = (user) => {
  if (!user) {
    ElMessage.warning('无法编辑用户信息')
    return
  }
  ElMessage.info(`编辑用户: ${user.username || '未知用户'}`)
}

// 删除用户
const deleteUser = async (user) => {
  if (!user) {
    ElMessage.warning('无法删除用户')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username || '未知用户'} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

// 添加用户
const addUser = async () => {
  try {
    await userFormRef.value.validate()
    await userApi.addUser(newUser.value)
    ElMessage.success('用户添加成功')
    showAddUserDialog.value = false
    newUser.value = { username: '', email: '', password: '' }
    await fitnessStore.fetchUsers()
  } catch (error) {
    ElMessage.error('添加用户失败：' + error.message)
  }
}

onMounted(() => {
  fitnessStore.fetchUsers()
})
</script>

<style scoped>
.users {
  padding: 20px;
}

.user-table {
  margin-top: 20px;
}
</style>
