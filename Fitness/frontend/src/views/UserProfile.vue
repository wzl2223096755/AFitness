 <template>
  <div class="user-profile-container">
    <h2>个人信息管理</h2>
    
    <div class="profile-card">
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="true" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" type="email" />
        </el-form-item>
        
        <el-form-item label="身高(cm)" prop="height">
          <el-input v-model.number="userForm.height" type="number" />
        </el-form-item>
        
        <el-form-item label="体重(kg)" prop="weight">
          <el-input v-model.number="userForm.weight" type="number" />
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="userForm.age" type="number" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="userForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="训练经验">
          <el-select v-model="userForm.experienceLevel">
            <el-option label="初学者" value="beginner" />
            <el-option label="中级" value="intermediate" />
            <el-option label="高级" value="advanced" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updateProfile">更新信息</el-button>
          <el-button @click="changePasswordDialogVisible = true">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="body-stats">
      <h3>身体数据统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ calculateBMI() }}</div>
          <div class="stat-label">BMI指数</div>
          <div class="stat-desc">{{ getBMIDescription() }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ calculateBMR() }}</div>
          <div class="stat-label">基础代谢</div>
          <div class="stat-desc">卡路里/天</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ getTrainingDays() }}</div>
          <div class="stat-label">本周训练</div>
          <div class="stat-desc">天数</div>
        </div>
      </div>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="changePasswordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changePasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="changePassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from '../utils/message.js'
import service from '../api/request'

export default {
  name: 'UserProfile',
  setup() {
    const userFormRef = ref(null)
    const passwordFormRef = ref(null)
    const changePasswordDialogVisible = ref(false)
    
    const userForm = reactive({
      username: '',
      email: '',
      height: 170,
      weight: 65,
      age: 25,
      gender: '男',
      experienceLevel: 'beginner'
    })
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const rules = {
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      height: [
        { required: true, message: '请输入身高', trigger: 'blur' },
        { type: 'number', min: 50, max: 250, message: '身高应在50-250cm之间', trigger: 'blur' }
      ],
      weight: [
        { required: true, message: '请输入体重', trigger: 'blur' },
        { type: 'number', min: 20, max: 300, message: '体重应在20-300kg之间', trigger: 'blur' }
      ],
      age: [
        { required: true, message: '请输入年龄', trigger: 'blur' },
        { type: 'number', min: 1, max: 150, message: '年龄应在1-150岁之间', trigger: 'blur' }
      ]
    }
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入旧密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    const fetchUserProfile = async () => {
      try {
        const response = await service.get('/api/v1/user/profile')
        if (response.success) {
          Object.assign(userForm, response.data)
        }
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    }
    
    const updateProfile = async () => {
      try {
        await userFormRef.value.validate()
        const response = await service.put('/api/v1/user/profile', userForm)
        if (response.success) {
          ElMessage.success('个人信息更新成功')
        }
      } catch (error) {
        if (error.message) {
          ElMessage.error('更新失败：' + error.message)
        }
      }
    }
    
    const changePassword = async () => {
      try {
        await passwordFormRef.value.validate()
        const response = await service.post('/api/v1/user/change-password', passwordForm)
        if (response.success) {
          ElMessage.success('密码修改成功')
          changePasswordDialogVisible.value = false
          // 重置表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        }
      } catch (error) {
        if (error.message) {
          ElMessage.error('修改失败：' + error.message)
        }
      }
    }
    
    const calculateBMI = () => {
      if (!userForm.height || !userForm.weight) return 0
      const heightM = userForm.height / 100
      const bmi = userForm.weight / (heightM * heightM)
      return bmi.toFixed(1)
    }
    
    const getBMIDescription = () => {
      const bmi = calculateBMI()
      if (bmi < 18.5) return '偏瘦'
      if (bmi < 24) return '正常'
      if (bmi < 28) return '超重'
      return '肥胖'
    }
    
    const calculateBMR = () => {
      if (!userForm.weight || !userForm.height || !userForm.age) return 0
      // 使用Mifflin-St Jeor公式
      let bmr
      if (userForm.gender === '男') {
        bmr = 10 * userForm.weight + 6.25 * userForm.height - 5 * userForm.age + 5
      } else {
        bmr = 10 * userForm.weight + 6.25 * userForm.height - 5 * userForm.age - 161
      }
      return Math.round(bmr)
    }
    
    const getTrainingDays = () => {
      // 从后端获取实际训练天数数据
      return 0
    }
    
    onMounted(() => {
      fetchUserProfile()
    })
    
    return {
      userFormRef,
      passwordFormRef,
      userForm,
      passwordForm,
      rules,
      passwordRules,
      changePasswordDialogVisible,
      updateProfile,
      changePassword,
      calculateBMI,
      getBMIDescription,
      calculateBMR,
      getTrainingDays
    }
  }
}
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.user-profile-container h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: var(--el-text-color-primary);
}

.profile-card {
  background: rgba(18, 18, 37, 0.95);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  box-shadow: var(--shadow-base, 0 0 15px rgba(112, 0, 255, 0.3));
}

.body-stats {
  background: rgba(18, 18, 37, 0.95);
  padding: 24px;
  border-radius: 12px;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  box-shadow: var(--shadow-base, 0 0 15px rgba(112, 0, 255, 0.3));
}

.body-stats h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary, #ffffff);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  background: rgba(18, 18, 37, 0.8);
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-base, 0 0 15px rgba(112, 0, 255, 0.3));
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-primary, #8020ff);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 15px;
  color: var(--text-primary, #ffffff);
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-desc {
  font-size: 13px;
  color: var(--text-secondary, #8888aa);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-container {
    padding: 12px;
  }
  
  .user-profile-container h2 {
    font-size: 20px;
    margin-bottom: 16px;
  }
  
  .profile-card,
  .body-stats {
    padding: 16px;
    border-radius: 12px;
  }
  
  /* 表单标签左对齐 */
  :deep(.el-form-item__label) {
    text-align: left !important;
    float: none !important;
    display: block !important;
    width: 100% !important;
    padding-bottom: 8px;
  }
  
  :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
  
  /* 输入框全宽 */
  :deep(.el-input),
  :deep(.el-select) {
    width: 100% !important;
  }
  
  /* 按钮组响应式 */
  :deep(.el-form-item:last-child .el-form-item__content) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  :deep(.el-form-item:last-child .el-button) {
    width: 100%;
    margin: 0;
  }
  
  /* 统计网格响应式 */
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .stat-item {
    padding: 20px;
    display: flex;
    flex-direction: row;
    align-items: center;
    text-align: left;
    gap: 16px;
  }
  
  .stat-value {
    font-size: 28px;
    margin-bottom: 0;
    min-width: 80px;
  }
  
  .stat-info {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .user-profile-container {
    padding: 10px;
  }
  
  .profile-card,
  .body-stats {
    padding: 14px;
  }
  
  .body-stats h3 {
    font-size: 16px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
    min-width: 70px;
  }
  
  .stat-label {
    font-size: 14px;
  }
  
  .stat-desc {
    font-size: 12px;
  }
}

/* 对话框移动端优化 */
:deep(.el-dialog) {
  @media (max-width: 768px) {
    width: 95% !important;
    margin: 0 auto !important;
  }
  
  @media (max-width: 480px) {
    width: 100% !important;
    height: 100vh !important;
    max-height: 100vh !important;
    margin: 0 !important;
    border-radius: 0 !important;
  }
}

:deep(.el-dialog__body) {
  @media (max-width: 480px) {
    max-height: calc(100vh - 140px);
    overflow-y: auto;
  }
}
</style>