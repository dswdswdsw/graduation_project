<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>教室资源管理</span>
          <el-button type="primary" @click="showAddDialog">添加教室</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="教学楼">
          <el-select v-model="query.building" clearable placeholder="全部" style="width:160px" @change="fetchData">
            <el-option v-for="b in buildingOptions" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width:140px" @change="fetchData">
            <el-option label="多媒体教室" value="多媒体教室" />
            <el-option label="阶梯教室" value="阶梯教室" />
            <el-option label="普通教室" value="普通教室" />
            <el-option label="实验室" value="实验室" />
            <el-option label="报告厅" value="报告厅" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px" @change="fetchData">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="name" label="教室名称" width="120" />
        <el-table-column prop="building" label="教学楼" width="120">
          <template #default="{ row }">{{ row.building || '-' }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="facilities" label="设施" min-width="180">
          <template #default="{ row }">{{ row.facilities || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教室' : '添加教室'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="教室名称" prop="name">
          <el-input v-model="form.name" placeholder="如：A101" />
        </el-form-item>
        <el-form-item label="教学楼" prop="building">
          <el-select v-model="form.building" filterable allow-create placeholder="选择或输入" style="width:100%">
            <el-option v-for="b in buildingOptions" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" style="width:100%" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="多媒体教室" value="多媒体教室" />
            <el-option label="阶梯教室" value="阶梯教室" />
            <el-option label="普通教室" value="普通教室" />
            <el-option label="实验室" value="实验室" />
            <el-option label="报告厅" value="报告厅" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施描述">
          <el-input v-model="form.facilities" placeholder="如：投影仪,空调,音响" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listClassrooms, addClassroom, updateClassroom, deleteClassroom, listAllBuildings } from '@/api/classroom'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const buildingOptions = ref([])
const query = reactive({ page: 1, size: 20, building: null, type: null, status: null })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', building: '', capacity: 60, type: '多媒体教室', facilities: '', status: 1 })
const formRules = {
  name: [{ required: true, message: '请输入教室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const typeLabel = (t) => t || t
const typeTag = (t) => ({ '多媒体教室': 'success', '阶梯教室': '', '普通教室': 'info', '实验室': 'warning', '报告厅': 'danger' }[t] || 'info')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listClassrooms(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取教室列表失败')
  } finally {
    loading.value = false
  }
}

const loadBuildings = async () => {
  try {
    const res = await listAllBuildings()
    buildingOptions.value = res.data || []
  } catch (e) { /* ignore */ }
}

const resetQuery = () => {
  query.building = null
  query.type = null
  query.status = null
  query.page = 1
  fetchData()
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '', building: '', capacity: 60, type: '多媒体教室', facilities: '', status: 1 })
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateClassroom(form)
      ElMessage.success('更新成功')
    } else {
      await addClassroom(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
    loadBuildings()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  await ElMessageBox.confirm(`确定${action}教室「${row.name}」？`, '确认', { type: 'warning' })
  try {
    await updateClassroom({ ...row, status: newStatus })
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除教室「${row.name}」？此操作不可恢复！`, '确认删除', { type: 'error' })
  try {
    await deleteClassroom(row.id)
    ElMessage.success('删除成功')
    fetchData()
    loadBuildings()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(async () => {
  await loadBuildings()
  fetchData()
})
</script>
