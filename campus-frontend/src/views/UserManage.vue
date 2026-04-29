<template>
  <div>
    <el-card>
      <template #header><span>用户管理</span></template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="角色">
          <el-select v-model="query.role" clearable placeholder="全部角色" style="width:140px">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width:120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/姓名/编号" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="{ admin: 'danger', teacher: 'warning', student: '' }[row.role]" size="small">{{ { admin: '管理员', teacher: '教师', student: '学生' }[row.role] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userNo" label="编号" width="120" />
        <el-table-column prop="className" label="班级" width="130" />
        <el-table-column prop="college" label="学院" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleResetPwd(row.id)">重置密码</el-button>
            <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next, sizes" :page-sizes="[10,20,50]"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUser, resetPassword } from '@/api/user'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ role: '', status: undefined, keyword: '', page: 1, size: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUsers(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.role = ''
  query.status = undefined
  query.keyword = ''
  query.page = 1
  fetchData()
}

const handleResetPwd = async (id) => {
  await ElMessageBox.confirm('确定重置该用户密码为默认密码 123456？', '提示', { type: 'warning' })
  await resetPassword(id)
  ElMessage.success('密码已重置为 123456')
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateUser(row.id, { status: newStatus })
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  fetchData()
}

onMounted(fetchData)
</script>
