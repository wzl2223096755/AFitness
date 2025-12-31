<template>
  <div class="user-profile">
    <div class="section-header">
      <h2>个人资料管理</h2>
      <div class="section-actions">
        <el-button type="primary" @click="saveProfile" :loading="saving">
          <el-icon><Check /></el-icon>
          保存更改
        </el-button>
        <el-button @click="resetProfile">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <div class="profile-container">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-container">
            <img :src="profileForm.avatar || defaultAvatar" alt="用户头像" class="avatar" />
            <div class="avatar-upload" @click="showAvatarDialog = true">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
          <h3 class="username">{{ profileForm.username || '用户名' }}</h3>
          <p class="user-email">{{ profileForm.email || 'user@example.com' }}</p>
        </div>

        <div class="basic-info">
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">{{ formatDate(profileForm.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">会员等级</span>
            <el-tag :type="getMemberLevelType(profileForm.memberLevel)">
              {{ getMemberLevelText(profileForm.memberLevel) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">训练天数</span>
            <span class="value">{{ profileForm.trainingDays }} 天</span>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息表单 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="profileForm" :rules="basicRules" ref="basicFormRef" label-width="120px">
              <div class="form-section">
                <h4>账户信息</h4>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="profileForm.username" placeholder="请输入用户名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="phone">
                      <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别" prop="gender">
                      <el-select v-model="profileForm.gender" placeholder="请选择性别">
                        <el-option label="男" value="male" />
                        <el-option label="女" value="female" />
                        <el-option label="其他" value="other" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="生日" prop="birthday">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="选择生日"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </div>

              <div class="form-section">
                <h4>身体数据</h4>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="身高" prop="height">
                      <el-input-number
                        v-model="profileForm.height"
                        :min="100"
                        :max="250"
                        placeholder="cm"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="体重" prop="weight">
                      <el-input-number
                        v-model="profileForm.weight"
                        :min="30"
                        :max="200"
                        placeholder="kg"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="BMI">
                      <el-input :value="calculateBMI()" disabled>
                        <template #suffix>kg/m²</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="体脂率" prop="bodyFat">
                      <el-input-number
                        v-model="profileForm.bodyFat"
                        :min="0"
                        :max="50"
                        :precision="1"
                        placeholder="%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="肌肉量" prop="muscleMass">
                      <el-input-number
                        v-model="profileForm.muscleMass"
                        :min="0"
                        :max="100"
                        placeholder="kg"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-form>
          </el-tab-pane>

          <!-- 健身目标 -->
          <el-tab-pane label="健身目标" name="goals">
            <el-form :model="profileForm" :rules="goalsRules" ref="goalsFormRef" label-width="120px">
              <div class="form-section">
                <h4>主要目标</h4>
                <el-form-item label="健身目标" prop="fitnessGoal">
                  <el-select v-model="profileForm.fitnessGoal" placeholder="请选择健身目标">
                    <el-option label="减脂塑形" value="fat_loss" />
                    <el-option label="增肌增重" value="muscle_gain" />
                    <el-option label="提升体能" value="endurance" />
                    <el-option label="保持健康" value="health" />
                    <el-option label="运动表现" value="performance" />
                  </el-select>
                </el-form-item>
                <el-form-item label="目标体重" prop="targetWeight">
                  <el-input-number
                    v-model="profileForm.targetWeight"
                    :min="30"
                    :max="200"
                    placeholder="kg"
                  />
                </el-form-item>
                <el-form-item label="目标体脂率" prop="targetBodyFat">
                  <el-input-number
                    v-model="profileForm.targetBodyFat"
                    :min="0"
                    :max="50"
                    :precision="1"
                    placeholder="%"
                  />
                </el-form-item>
              </div>

              <div class="form-section">
                <h4>训练偏好</h4>
                <el-form-item label="训练频率" prop="trainingFrequency">
                  <el-select v-model="profileForm.trainingFrequency" placeholder="每周训练次数">
                    <el-option label="1-2次" value="1-2" />
                    <el-option label="3-4次" value="3-4" />
                    <el-option label="5-6次" value="5-6" />
                    <el-option label="每天" value="daily" />
                  </el-select>
                </el-form-item>
                <el-form-item label="训练时长" prop="trainingDuration">
                  <el-select v-model="profileForm.trainingDuration" placeholder="每次训练时长">
                    <el-option label="30分钟以内" value="30" />
                    <el-option label="30-60分钟" value="30-60" />
                    <el-option label="60-90分钟" value="60-90" />
                    <el-option label="90分钟以上" value="90+" />
                  </el-select>
                </el-form-item>
                <el-form-item label="偏好类型" prop="preferredTypes">
                  <el-checkbox-group v-model="profileForm.preferredTypes">
                    <el-checkbox label="strength">力量训练</el-checkbox>
                    <el-checkbox label="cardio">有氧训练</el-checkbox>
                    <el-checkbox label="flexibility">柔韧训练</el-checkbox>
                    <el-checkbox label="sports">运动项目</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </div>
            </el-form>
          </el-tab-pane>

          <!-- 隐私设置 -->
          <el-tab-pane label="隐私设置" name="privacy">
            <el-form :model="profileForm.privacySettings" label-width="120px">
              <div class="form-section">
                <h4>数据可见性</h4>
                <el-form-item label="训练数据">
                  <el-switch
                    v-model="profileForm.privacySettings.showTrainingData"
                    active-text="公开"
                    inactive-text="私密"
                  />
                </el-form-item>
                <el-form-item label="身体数据">
                  <el-switch
                    v-model="profileForm.privacySettings.showBodyData"
                    active-text="公开"
                    inactive-text="私密"
                  />
                </el-form-item>
                <el-form-item label="成就徽章">
                  <el-switch
                    v-model="profileForm.privacySettings.showAchievements"
                    active-text="公开"
                    inactive-text="私密"
                  />
                </el-form-item>
              </div>

              <div class="form-section">
                <h4>通知设置</h4>
                <el-form-item label="训练提醒">
                  <el-switch
                    v-model="profileForm.privacySettings.trainingReminder"
                    active-text="开启"
                    inactive-text="关闭"
                  />
                </el-form-item>
                <el-form-item label="目标达成">
                  <el-switch
                    v-model="profileForm.privacySettings.goalNotification"
                    active-text="开启"
                    inactive-text="关闭"
                  />
                </el-form-item>
                <el-form-item label="系统消息">
                  <el-switch
                    v-model="profileForm.privacySettings.systemNotification"
                    active-text="开启"
                    inactive-text="关闭"
                  />
                </el-form-item>
              </div>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="400px">
      <div class="avatar-upload-dialog">
        <div class="current-avatar">
          <img :src="profileForm.avatar || defaultAvatar" alt="当前头像" />
        </div>
        <div class="upload-options">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button type="primary">选择图片</el-button>
          </el-upload>
          <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarDialog = false">取消</el-button>
          <el-button type="primary" @click="showAvatarDialog = false">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Camera } from '@element-plus/icons-vue'

// 响应式数据
const saving = ref(false)
const activeTab = ref('basic')
const showAvatarDialog = ref(false)
const basicFormRef = ref(null)
const goalsFormRef = ref(null)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 上传地址
const uploadUrl = '/api/upload/avatar'

// 表单数据
const profileForm = ref({
  username: '',
  email: '',
  phone: '',
  gender: '',
  birthday: '',
  avatar: '',
  height: 170,
  weight: 65,
  bodyFat: 20,
  muscleMass: 50,
  fitnessGoal: '',
  targetWeight: 65,
  targetBodyFat: 15,
  trainingFrequency: '3-4',
  trainingDuration: '30-60',
  preferredTypes: [],
  memberLevel: 'basic',
  trainingDays: 0,
  createdAt: new Date().toISOString(),
  privacySettings: {
    showTrainingData: false,
    showBodyData: false,
    showAchievements: true,
    trainingReminder: true,
    goalNotification: true,
    systemNotification: false
  }
})

// 原始数据备份
const originalData = ref({})

// 表单验证规则
const basicRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
  ]
}

const goalsRules = {
  fitnessGoal: [
    { required: true, message: '请选择健身目标', trigger: 'change' }
  ],
  targetWeight: [
    { required: true, message: '请输入目标体重', trigger: 'blur' }
  ],
  trainingFrequency: [
    { required: true, message: '请选择训练频率', trigger: 'change' }
  ]
}

// 计算属性
const calculateBMI = () => {
  const { height, weight } = profileForm.value
  if (!height || !weight) return '0.0'
  const bmi = weight / Math.pow(height / 100, 2)
  return bmi.toFixed(1)
}

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const getMemberLevelType = (level) => {
  const typeMap = {
    basic: 'info',
    silver: 'success',
    gold: 'warning',
    platinum: 'danger'
  }
  return typeMap[level] || 'info'
}

const getMemberLevelText = (level) => {
  const textMap = {
    basic: '基础会员',
    silver: '银牌会员',
    gold: '金牌会员',
    platinum: '白金会员'
  }
  return textMap[level] || '基础会员'
}

const loadProfile = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟用户数据
    const mockData = {
      username: '健身达人',
      email: 'fitness@example.com',
      phone: '13800138000',
      gender: 'male',
      birthday: '1990-01-01',
      avatar: '',
      height: 175,
      weight: 70,
      bodyFat: 18.5,
      muscleMass: 55,
      fitnessGoal: 'muscle_gain',
      targetWeight: 75,
      targetBodyFat: 12,
      trainingFrequency: '4-5',
      trainingDuration: '60-90',
      preferredTypes: ['strength', 'cardio'],
      memberLevel: 'gold',
      trainingDays: 120,
      createdAt: '2023-01-15T10:30:00Z',
      privacySettings: {
        showTrainingData: true,
        showBodyData: false,
        showAchievements: true,
        trainingReminder: true,
        goalNotification: true,
        systemNotification: false
      }
    }
    
    profileForm.value = { ...profileForm.value, ...mockData }
    originalData.value = JSON.parse(JSON.stringify(profileForm.value))
    
  } catch (error) {
    console.error('加载用户资料失败:', error)
    ElMessage.error('加载用户资料失败')
  }
}

const saveProfile = async () => {
  try {
    // 验证表单
    let isValid = true
    
    if (activeTab.value === 'basic') {
      isValid = await basicFormRef.value?.validate().catch(() => false)
    } else if (activeTab.value === 'goals') {
      isValid = await goalsFormRef.value?.validate().catch(() => false)
    }
    
    if (!isValid) {
      ElMessage.error('请检查表单填写是否正确')
      return
    }
    
    saving.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新原始数据
    originalData.value = JSON.parse(JSON.stringify(profileForm.value))
    
    ElMessage.success('个人资料保存成功')
  } catch (error) {
    console.error('保存个人资料失败:', error)
    ElMessage.error('保存个人资料失败')
  } finally {
    saving.value = false
  }
}

const resetProfile = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有更改吗？未保存的修改将会丢失。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    profileForm.value = JSON.parse(JSON.stringify(originalData.value))
    ElMessage.success('已重置到上次保存的状态')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置失败:', error)
    }
  }
}

const handleAvatarSuccess = (response) => {
  profileForm.value.avatar = response.url
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 生命周期
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.profile-container {
  display: flex;
  gap: 30px;
}

.profile-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f0f0f0;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  border: 2px solid white;
  transition: background-color 0.3s ease;
}

.avatar-upload:hover {
  background: #337ecc;
}

.username {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.user-email {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.basic-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  font-weight: 600;
  color: #333;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.form-section {
  margin-bottom: 30px;
}

.form-section h4 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
}

.avatar-upload-dialog {
  text-align: center;
}

.current-avatar img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 20px;
}

.upload-options {
  margin-top: 20px;
}

.upload-tip {
  margin: 10px 0 0 0;
  color: #999;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}
</style>