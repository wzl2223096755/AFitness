<template>
  <el-dialog v-model="visible" title="编辑个人资料" width="500px" class="glass-modal">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="年龄" prop="age">
        <el-input-number v-model="form.age" :min="1" :max="150" placeholder="请输入年龄" />
      </el-form-item>
      <el-form-item label="身高(cm)" prop="height">
        <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" placeholder="请输入身高" />
      </el-form-item>
      <el-form-item label="体重(kg)" prop="weight">
        <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" placeholder="请输入体重" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio label="男">男</el-radio>
          <el-radio label="女">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="训练经验" prop="experienceLevel">
        <el-select v-model="form.experienceLevel" placeholder="请选择训练经验">
          <el-option label="初学者" value="beginner" />
          <el-option label="中级" value="intermediate" />
          <el-option label="高级" value="advanced" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <button class="btn-unified btn-unified--ghost" @click="close">取消</button>
      <button class="btn-unified btn-unified--primary" @click="save" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  user: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = ref(false)
const formRef = ref(null)
const saving = ref(false)

const form = ref({
  username: '',
  email: '',
  age: null,
  height: null,
  weight: null,
  gender: '',
  experienceLevel: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.user) {
    form.value = {
      username: props.user.username || '',
      email: props.user.email || '',
      age: props.user.age || null,
      height: props.user.height || null,
      weight: props.user.weight || null,
      gender: props.user.gender || '',
      experienceLevel: props.user.experienceLevel || ''
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const close = () => {
  visible.value = false
}

const save = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    await userApi.updateProfile(form.value)
    
    ElMessage.success('个人资料更新成功')
    emit('saved')
    close()
  } catch (error) {
    ElMessage.error('更新失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
:deep(.el-dialog) {
  background: var(--glass-bg, rgba(15, 15, 35, 0.95)) !important;
  border: 1px solid var(--glass-border, rgba(99, 102, 241, 0.2)) !important;
  border-radius: 20px !important;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
}

:deep(.el-dialog__title) {
  color: var(--color-text-primary, #f8fafc) !important;
  font-weight: 600;
}

:deep(.el-form-item__label) {
  color: var(--color-text-secondary, #94a3b8) !important;
}
</style>
