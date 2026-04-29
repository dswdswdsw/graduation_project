<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>通知公告</span>
          <el-button type="primary" v-if="userStore.role === 'admin' || userStore.role === 'teacher'" @click="showAddDialog">发布公告</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" style="width:150px">
            <el-option label="全部分类" value="" />
            <el-option label="教务通知" value="academic" />
            <el-option label="校园活动" value="activity" />
            <el-option label="紧急通知" value="urgent" />
            <el-option label="普通通知" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="250">
          <template #default="{ row }">
            <span style="cursor:pointer;color:#409eff" @click="viewDetail(row)">{{ row.isTop ? '【置顶】' : '' }}{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.category)">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" v-if="userStore.role !== 'admin'">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">{{ row.isRead ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" link v-if="userStore.role === 'admin'" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
      <el-empty v-if="!loading && list.length === 0" description="暂无通知公告" />
    </el-card>

    <el-dialog v-model="addDialogVisible" title="发布公告" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="教务通知" value="academic" />
            <el-option label="校园活动" value="activity" />
            <el-option label="紧急通知" value="urgent" />
            <el-option label="普通通知" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标角色">
          <el-select v-model="form.targetRole" style="width:100%">
            <el-option label="所有人" value="all" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" :title="currentNotice.title" width="700px">
      <div style="margin-bottom:12px;color:#999;font-size:13px">
        {{ categoryLabel(currentNotice.category) }} | {{ currentNotice.publisherName }} | {{ formatTime(currentNotice.publishTime) }}
      </div>
      <div style="line-height:1.8;white-space:pre-wrap">{{ currentNotice.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listNotices, addNotice, deleteNotice, markAsRead } from '@/api/notice'
import { useUserStore } from '@/stores/user'
import eventBus from '@/utils/eventBus'
import { formatTime } from '@/utils/format'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ category: '', page: 1, size: 10 })

const categoryLabel = (c) => ({ academic: '教务通知', activity: '校园活动', urgent: '紧急通知', general: '普通通知' }[c] || '普通通知')
const categoryTag = (c) => ({ academic: '', activity: 'success', urgent: 'danger', general: 'info' }[c] || 'info')

const addDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentNotice = ref({})

const formRef = ref(null)
const form = reactive({ title: '', content: '', category: 'general', targetRole: 'all' })
const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listNotices(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  Object.assign(form, { title: '', content: '', category: 'general', targetRole: 'all' })
  addDialogVisible.value = true
}

const handleAdd = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await addNotice(form)
  ElMessage.success('发布成功')
  addDialogVisible.value = false
  fetchData()
}

const viewDetail = async (row) => {
  currentNotice.value = row
  detailDialogVisible.value = true
  if (!row.isRead) {
    await markAsRead(row.id)
    row.isRead = true
    eventBus.emit('notice:read')
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await deleteNotice(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>