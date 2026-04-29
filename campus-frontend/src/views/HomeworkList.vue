<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>作业管理</span>
          <el-button type="primary" v-if="userStore.role === 'teacher' || userStore.role === 'admin'" @click="showAddDialog">发布作业</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom:16px" v-if="userStore.role !== 'student'">
        <el-form-item label="课程" v-if="userStore.role !== 'student'">
          <el-select v-model="query.courseId" clearable placeholder="全部课程" style="width:200px">
            <el-option v-for="c in courseOptions" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" v-if="classOptions.length > 0">
          <el-select v-model="query.className" clearable placeholder="全部班级" style="width:160px">
            <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe>
        <el-table-column prop="title" label="作业标题" min-width="180" />
        <el-table-column prop="courseName" label="课程" width="130" />
        <el-table-column prop="classNames" label="班级" width="150" v-if="userStore.role !== 'student'">
          <template #default="{ row }">
            <span>{{ row.classNames || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="教师" width="100" v-if="userStore.role === 'admin'" />
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">
            <span :style="{ color: row.deadline && new Date(row.deadline) < Date.now() ? '#F56C6C' : '' }">{{ row.deadline?.replace('T', ' ') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分数" width="80">
          <template #default="{ row }">
            <span v-if="row.score != null">{{ row.score }}</span>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="提交数" width="80" v-if="userStore.role !== 'student'">
          <template #default="{ row }">{{ row.submitCount || 0 }}/{{ row.studentCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="!row.submitted && row.deadline && new Date(row.deadline) < Date.now()" type="danger">已截止</el-tag>
            <el-tag v-else-if="!row.submitted" type="info">未提交</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已打回</el-tag>
            <el-tag v-else-if="row.score != null" type="success">已批改</el-tag>
            <el-tag v-else type="warning">未批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row.id)">查看</el-button>
            <el-button size="small" type="success" link v-if="userStore.role === 'student' && (!row.submitted || row.status === 2) && (!row.deadline || new Date(row.deadline) > Date.now())" @click="showSubmitDialog(row.id)">{{ row.status === 2 ? '重新提交' : '提交' }}</el-button>
            <el-button size="small" type="warning" link v-if="userStore.role === 'teacher'" @click="$router.push('/homework/grade/' + row.id)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="addDialogVisible" title="发布作业" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-if="userStore.role === 'teacher' && myCourses.length" v-model="form.courseId" placeholder="选择课程" style="width:100%">
            <el-option v-for="c in myCourses" :key="c.courseId" :label="c.courseName + ' (' + c.code + ')'" :value="c.courseId" />
          </el-select>
          <el-input v-else v-model.number="form.courseId" type="number" placeholder="课程ID" />
        </el-form-item>
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="满分"><el-input-number v-model="form.totalScore" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submitDialogVisible" title="提交作业" width="500px">
      <el-form ref="submitFormRef" :model="submitForm" :rules="submitRules" label-width="80px">
        <el-form-item label="作业内容" prop="content">
          <el-input v-model="submitForm.content" type="textarea" :rows="6" placeholder="请输入作业内容" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :auto-upload="false"
            :limit="3"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="submitForm.fileList"
            accept=".pdf,.doc,.docx,.zip,.rar,.ppt,.pptx,.xls,.xlsx,.txt,.jpg,.png"
          >
            <el-button type="primary" plain size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 PDF/Word/Excel/PPT/压缩包/图片，最多3个文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submissionsDialogVisible" title="提交列表" width="800px">
      <el-table :data="submissions" stripe>
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="userNo" label="学号" width="100" />
        <el-table-column prop="content" label="内容" min-width="150" />
        <el-table-column label="附件" width="200">
          <template #default="{ row }">
            <template v-if="row.attachmentId">
              <el-image v-if="isImageAttachment(row.attachmentName)"
                :src="getDownloadUrl(row.attachmentId)"
                :preview-src-list="[getDownloadUrl(row.attachmentId)]"
                fit="cover"
                style="width:60px;height:60px;border-radius:4px;cursor:pointer"
                preview-teleported
              />
              <el-button v-else size="small" type="primary" link @click="downloadAttachment(row.attachmentId)">下载附件</el-button>
            </template>
            <span v-else style="color:#999">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已批改' : '待批改' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" link v-if="row.status === 0" @click="showGradeDialog(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="gradeDialogVisible" title="批改作业" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-input-number v-model="gradeForm.score" :min="0" :max="100" /></el-form-item>
        <el-form-item label="评语"><el-input v-model="gradeForm.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGrade">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="作业详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="作业标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="所属课程">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ detailData.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ detailData.deadline?.replace('T', ' ') }}</el-descriptions-item>
        <el-descriptions-item label="满分">{{ detailData.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="提交人数" v-if="userStore.role !== 'student'">{{ detailData.submitCount || 0 }}/{{ detailData.studentCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="作业描述" :span="2">
          <div style="white-space:pre-wrap;line-height:1.6">{{ detailData.description || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2" v-if="detailData.attachmentId">
          <div style="display:flex;align-items:center;gap:10px">
            <el-image v-if="isImageAttachment(detailData.attachmentName)"
              :src="getDownloadUrl(detailData.attachmentId)"
              :preview-src-list="[getDownloadUrl(detailData.attachmentId)]"
              fit="cover"
              style="width:100px;height:100px;border-radius:6px;cursor:pointer"
              preview-teleported
            />
            <el-button type="primary" link @click="downloadAttachment(detailData.attachmentId)">
              {{ detailData.attachmentName || '下载附件' }}
            </el-button>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="userStore.role === 'student' && detailData.mySubmission" style="margin-top:16px">
        <el-divider>我的提交</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="提交内容">{{ detailData.mySubmission.content || '无' }}</el-descriptions-item>
          <el-descriptions-item label="附件" v-if="detailData.mySubmission.attachmentId">
            <div style="display:flex;align-items:center;gap:10px">
              <el-image v-if="isImageAttachment(detailData.mySubmission.attachmentName)"
                :src="getDownloadUrl(detailData.mySubmission.attachmentId)"
                :preview-src-list="[getDownloadUrl(detailData.mySubmission.attachmentId)]"
                fit="cover"
                style="width:80px;height:80px;border-radius:6px;cursor:pointer"
                preview-teleported
              />
              <el-button type="primary" link @click="downloadAttachment(detailData.mySubmission.attachmentId)">下载附件</el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="成绩">
            <span v-if="detailData.mySubmission.score != null" style="font-weight:700;font-size:16px" :style="{color: detailData.mySubmission.score >= 60 ? '#67c23a' : '#f56c6c'}">{{ detailData.mySubmission.score }}分</span>
            <span v-else style="color:#999">未批改</span>
          </el-descriptions-item>
          <el-descriptions-item label="评语" v-if="detailData.mySubmission.comment">{{ detailData.mySubmission.comment }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listHomework, listStudentHomework, addHomework, submitHomework, listSubmissions, gradeSubmission, getHomeworkDetail } from '@/api/homework'
import { uploadFile, getDownloadUrl, getFileDetail } from '@/api/file'
import { getTeacherSchedule } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, courseId: null, className: null })
const courseOptions = ref([])
const myCourses = ref([])
const classOptions = ref([])

const addDialogVisible = ref(false)
const submitDialogVisible = ref(false)
const submissionsDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const detailData = ref({})

const formRef = ref(null)
const form = reactive({ courseId: null, title: '', description: '', deadline: '', totalScore: 100 })
const formRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  deadline: [{ required: true, message: '请输入截止时间', trigger: 'blur' }]
}

const submitFormRef = ref(null)
const submitForm = reactive({ content: '', fileList: [] })
const submitRules = {
  content: [{ required: true, message: '请输入作业内容', trigger: 'blur' }]
}
const submitLoading = ref(false)
const currentHomeworkId = ref(null)
const submissions = ref([])
const gradeForm = reactive({ submissionId: null, score: 0, comment: '' })

const fetchData = async () => {
  const api = userStore.role === 'student' ? listStudentHomework : listHomework
  const res = await api(query)
  let records = res.data.records || []
  if (query.className) {
    records = records.filter(r => r.classNames && r.classNames.includes(query.className))
  }
  list.value = records
  total.value = query.className ? records.length : res.data.total
}

const loadTeacherCourses = async () => {
  if (userStore.role !== 'teacher') return
  try {
    const res = await getTeacherSchedule({})
    myCourses.value = res.data || []
    courseOptions.value = res.data || []
    const allClasses = new Set()
    ;(res.data || []).forEach(c => {
      if (c.classNames) c.classNames.split(',').forEach(cls => { if (cls.trim()) allClasses.add(cls.trim()) })
    })
    classOptions.value = Array.from(allClasses).sort()
  } catch (e) { /* ignore */ }
}

const showAddDialog = () => {
  Object.assign(form, { courseId: null, title: '', description: '', deadline: '', totalScore: 100 })
  addDialogVisible.value = true
}

const handleAdd = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await addHomework(form)
  ElMessage.success('发布成功')
  addDialogVisible.value = false
  fetchData()
}

const viewDetail = async (id) => {
  try {
    const res = await getHomeworkDetail(id)
    detailData.value = res.data || {}
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取作业详情失败')
  }
}

const showSubmitDialog = (id) => {
  currentHomeworkId.value = id
  submitForm.content = ''
  submitForm.fileList = []
  submitDialogVisible.value = true
}

const handleFileChange = (file, fileList) => {
  submitForm.fileList = fileList
}

const handleFileRemove = (file, fileList) => {
  submitForm.fileList = fileList
}

const handleSubmit = async () => {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    let attachmentId = null
    if (submitForm.fileList.length > 0) {
      const formData = new FormData()
      formData.append('file', submitForm.fileList[0].raw)
      formData.append('bizType', 'homework')
      const fileRes = await uploadFile(formData)
      attachmentId = fileRes.data.id
    }
    await submitHomework(currentHomeworkId.value, submitForm.content, attachmentId)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const downloadAttachment = (fileId) => {
  window.open(getDownloadUrl(fileId), '_blank')
}

const isImageAttachment = (name) => {
  if (!name) return false
  const ext = name.toLowerCase().split('.').pop()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
}

const viewSubmissions = async (id) => {
  const res = await listSubmissions(id, { page: 1, size: 100 })
  submissions.value = res.data.records
  submissionsDialogVisible.value = true
}

const showGradeDialog = (row) => {
  gradeForm.submissionId = row.id
  gradeForm.score = 0
  gradeForm.comment = ''
  gradeDialogVisible.value = true
}

const handleGrade = async () => {
  await gradeSubmission(gradeForm)
  ElMessage.success('批改成功')
  gradeDialogVisible.value = false
  submissionsDialogVisible.value = false
  fetchData()
}

onMounted(async () => {
  await loadTeacherCourses()
  fetchData()
})
</script>