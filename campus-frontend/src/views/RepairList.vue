<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>{{ userStore.role === 'admin' ? '报修管理' : '设备报修' }}</span>
          <el-button type="primary" v-if="userStore.role !== 'admin'" @click="showAddDialog">提交报修</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" style="width:150px">
            <el-option label="全部分类" value="" />
            <el-option label="电气故障" value="设备故障" />
            <el-option label="水管问题" value="水管问题" />
            <el-option label="家具损坏" value="设施损坏" />
            <el-option label="网络故障" value="网络故障" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" style="width:120px">
            <el-option label="全部状态" :value="-1" />
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column v-if="userStore.role === 'admin'" prop="userName" label="报修人" width="100" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">{{ categoryMap[row.category] || row.category }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column prop="urgency" label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag :type="{ low: 'info', normal: '', high: 'danger' }[row.urgency]">{{ { low: '低', normal: '中', high: '高' }[row.urgency] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="success" link v-if="row.status === 2 && !row.rating" @click="showRateDialog(row)">评价</el-button>
            <el-button size="small" type="warning" link v-if="userStore.role === 'admin' && row.status === 0" @click="showHandleDialog(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
      <el-empty v-if="!loading && list.length === 0" description="暂无报修记录" />
    </el-card>

    <el-dialog v-model="addDialogVisible" title="提交报修" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="电气故障" value="设备故障" />
            <el-option label="水管问题" value="水管问题" />
            <el-option label="家具损坏" value="设施损坏" />
            <el-option label="网络故障" value="网络故障" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.urgency">
            <el-radio value="low">低</el-radio>
            <el-radio value="normal">中</el-radio>
            <el-radio value="high">高</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handleDialogVisible" title="处理报修" width="500px">
      <el-input v-model="handleResult" type="textarea" :rows="4" placeholder="请输入处理结果" />
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rateDialogVisible" title="评价" width="400px">
      <el-rate v-model="rateForm.rating" style="margin-bottom:16px" />
      <el-input v-model="rateForm.content" type="textarea" :rows="3" placeholder="评价内容" />
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRate">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="报修详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentRepair.title }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentRepair.content }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ currentRepair.location }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ categoryMap[currentRepair.category] }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[currentRepair.status] }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" v-if="currentRepair.handleResult">{{ currentRepair.handleResult }}</el-descriptions-item>
        <el-descriptions-item label="评价" v-if="currentRepair.rating">
          <el-rate v-model="currentRepair.rating" disabled /> {{ currentRepair.ratingContent }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyRepairs, addRepair, handleRepair, rateRepair, listAllRepairs } from '@/api/repair'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ category: '', status: -1, page: 1, size: 10 })

const categoryMap = { '设备故障': '电气故障', '水管问题': '水管问题', '设施损坏': '家具损坏', '网络故障': '网络故障', '其他': '其他' }
const statusMap = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
const statusType = (s) => ({ 0: 'warning', 1: '', 2: 'success', 3: 'info' }[s])

const addDialogVisible = ref(false)
const handleDialogVisible = ref(false)
const rateDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const formRef = ref(null)
const form = reactive({ title: '', content: '', location: '', category: '设备故障', urgency: 'normal' })
const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const handleResult = ref('')
const currentRepairId = ref(null)
const currentRepair = ref({})
const rateForm = reactive({ rating: 5, content: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (params.status === -1) params.status = null
    const api = userStore.role === 'admin' ? listAllRepairs : listMyRepairs
    const res = await api(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  Object.assign(form, { title: '', content: '', location: '', category: '设备故障', urgency: 'normal' })
  addDialogVisible.value = true
}

const handleAdd = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await addRepair(form)
  ElMessage.success('报修提交成功')
  addDialogVisible.value = false
  fetchData()
}

const viewDetail = (row) => {
  currentRepair.value = row
  detailDialogVisible.value = true
}

const showHandleDialog = (row) => {
  currentRepairId.value = row.id
  handleResult.value = ''
  handleDialogVisible.value = true
}

const handleHandle = async () => {
  await handleRepair(currentRepairId.value, handleResult.value)
  ElMessage.success('处理成功')
  handleDialogVisible.value = false
  fetchData()
}

const showRateDialog = (row) => {
  currentRepairId.value = row.id
  rateForm.rating = 5
  rateForm.content = ''
  rateDialogVisible.value = true
}

const handleRate = async () => {
  await rateRepair(currentRepairId.value, rateForm)
  ElMessage.success('评价成功')
  rateDialogVisible.value = false
  fetchData()
}

onMounted(fetchData)
</script>
