<template>
  <div>
    <el-card>
      <template #header><span>个人中心</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
        <el-descriptions-item :label="noLabel">{{ userInfo.userNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ userInfo.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ userInfo.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业" v-if="userInfo.role === 'student'">{{ userInfo.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级" v-if="userInfo.role === 'student'">{{ userInfo.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span>修改密码</span></template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width:400px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePwd">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, changePassword } from '@/api/user'

const userInfo = ref({})
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }]
}

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userInfo.value.role] || userInfo.value.role
})

const noLabel = computed(() => {
  const map = { admin: '编号', teacher: '工号', student: '学号' }
  return map[userInfo.value.role] || '编号'
})

onMounted(async () => {
  const res = await getUserInfo()
  userInfo.value = res.data
})

const handleChangePwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  await changePassword(pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
}
</script>