<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div style="display:flex;align-items:center;gap:12px">
            <span style="font-size:16px;font-weight:bold">批改作业</span>
            <el-tag :type="statusTagType" size="small">{{ statusLabel }}</el-tag>
          </div>
          <el-button @click="$router.back()">返回列表</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-descriptions :column="2" border size="large" style="margin-bottom:24px">
          <el-descriptions-item label="作业标题">{{ homeworkInfo.title }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ homeworkInfo.courseName }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ submission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ submission.userNo }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ submission.className }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ submission.submitTime?.replace('T', ' ') }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-bottom:12px">作业内容</h4>
        <el-card shadow="never" style="margin-bottom:24px;background:#fafafa">
          <p style="white-space:pre-wrap;line-height:1.8;font-size:14px">{{ submission.content || '无内容' }}</p>
        </el-card>

        <div v-if="submission.attachmentId" style="margin-bottom:24px">
          <h4 style="margin-bottom:12px">附件</h4>
          <el-button type="primary" link @click="downloadAttachment(submission.attachmentId)">下载附件</el-button>
        </div>

        <h4 style="margin-bottom:16px">评分批改</h4>
        <el-form :model="form" label-width="70px" style="max-width:500px">
          <el-form-item label="评分">
            <el-input-number v-model="form.score" :min="0" :max="Number(homeworkInfo.totalScore || 100)" :disabled="isReturned" style="width:100%" />
            <span style="margin-left:8px;color:#999">满分 {{ homeworkInfo.totalScore || 100 }} 分</span>
          </el-form-item>
          <el-form-item label="评语">
            <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="可选填写评语" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" :disabled="isReturned" @click="handleSubmit">确认批改</el-button>
            <el-button type="danger" plain :loading="returning" :disabled="isReturned" @click="handleReturn">打回重做</el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGradeDetail, listHomeworkStudents, gradeSubmission as apiGradeSubmission, returnSubmission as apiReturnSubmission } from '@/api/homework'
import { getDownloadUrl } from '@/api/file'

const route = useRoute()
const router = useRouter()
const homeworkId = Number(route.params.homeworkId)
const submissionId = Number(route.params.submissionId)

const loading = ref(true)
const submitting = ref(false)
const returning = ref(false)
const homeworkInfo = ref({})
const submission = ref({})
const form = reactive({ score: 0, comment: '' })

const isReturned = computed(() => submission.value.status === 2)

const statusLabel = computed(() => {
  const s = submission.value.status
  if (s === 1) return '已批改'
  if (s === 2) return '已打回'
  return '待批改'
})

const statusTagType = computed(() => {
  const s = submission.value.status
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
})

const fetchData = async () => {
  loading.value = true
  try {
    const [detailRes, studentsRes] = await Promise.all([getGradeDetail(homeworkId), listHomeworkStudents(homeworkId)])
    homeworkInfo.value = detailRes.data || {}
    const target = (studentsRes.data || []).find(s => s.id === submissionId)
    if (target) {
      submission.value = target
      form.score = target.score ? Number(target.score) : 0
      form.comment = ''
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (form.score === null || form.score === undefined) {
    ElMessage.warning('请输入分数')
    return
  }
  submitting.value = true
  try {
    await apiGradeSubmission({ submissionId, score: form.score, comment: form.comment })
    ElMessage.success('批改成功')
    router.push(`/homework/grade/${homeworkId}`)
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

const handleReturn = async () => {
  try {
    await ElMessageBox.confirm('确定打回此作业？学生可以重新提交。', '打回确认', {
      confirmButtonText: '确定打回',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  returning.value = true
  try {
    await apiReturnSubmission({ submissionId, comment: form.comment })
    ElMessage.success('已打回，学生可重新提交')
    router.push(`/homework/grade/${homeworkId}`)
  } catch (e) {
  } finally {
    returning.value = false
  }
}

const downloadAttachment = (fileId) => {
  window.open(getDownloadUrl(fileId), '_blank')
}

onMounted(fetchData)
</script>
