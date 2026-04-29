<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <span style="font-size:16px;font-weight:bold">{{ homeworkInfo.title || '作业批改' }}</span>
            <el-tag style="margin-left:12px" type="info">{{ homeworkInfo.courseName }}</el-tag>
            <span style="margin-left:12px;color:#999;font-size:13px">截止：{{ homeworkInfo.deadline?.replace('T', ' ') }} | 满分：{{ homeworkInfo.totalScore }}分</span>
          </div>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="已提交" name="submitted">
          <el-table :data="submittedList" stripe v-loading="loading">
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="userNo" label="学号" width="100" />
            <el-table-column prop="className" label="班级" width="150" />
            <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
            <el-table-column label="附件" width="80">
              <template #default="{ row }">
                <el-button v-if="row.attachmentId" size="small" type="primary" link @click="downloadAttachment(row.attachmentId)">下载</el-button>
                <span v-else style="color:#999">无</span>
              </template>
            </el-table-column>
            <el-table-column label="成绩" width="80">
              <template #default="{ row }">
                <span v-if="row.score != null">{{ row.score }}</span>
                <el-tag v-else size="small" type="warning">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'" size="small">{{ row.status === 1 ? '已批改' : row.status === 2 ? '已打回' : '待批改' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="$router.push('/homework/grade/' + homeworkId + '/' + row.id)">批改</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane name="unsubmitted" label="未提交">
          <el-table :data="unsubmittedList" stripe v-loading="loading">
            <el-table-column prop="studentName" label="学生" width="120" />
            <el-table-column prop="userNo" label="学号" width="120" />
            <el-table-column prop="className" label="班级" width="200" />
            <el-table-column label="状态" width="100">
              <template #default>
                <el-tag type="info" size="small">未提交</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGradeDetail, listHomeworkStudents } from '@/api/homework'
import { getDownloadUrl } from '@/api/file'

const route = useRoute()
const homeworkId = Number(route.params.id)

const loading = ref(false)
const activeTab = ref('submitted')
const homeworkInfo = ref({})
const allStudents = ref([])
const submittedList = ref([])
const unsubmittedList = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const [detailRes, studentsRes] = await Promise.all([getGradeDetail(homeworkId), listHomeworkStudents(homeworkId)])
    homeworkInfo.value = detailRes.data || {}
    allStudents.value = studentsRes.data || []
    splitStudents()
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const splitStudents = () => {
  submittedList.value = allStudents.value.filter(s => s.submitted)
  unsubmittedList.value = allStudents.value.filter(s => !s.submitted)
}

const handleTabChange = () => {}

const downloadAttachment = (fileId) => {
  window.open(getDownloadUrl(fileId), '_blank')
}

onMounted(fetchData)
</script>
