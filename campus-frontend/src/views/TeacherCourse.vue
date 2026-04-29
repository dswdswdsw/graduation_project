<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>我的课程</span>
          <el-select v-model="semester" placeholder="选择学期" style="width:220px" @change="fetchCourses">
            <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </div>
      </template>

      <el-table :data="courses" stripe v-loading="loading">
        <el-table-column prop="code" label="课程编号" width="100" />
        <el-table-column prop="courseName" label="课程名称" min-width="160">
          <template #default="{ row }">
            <span style="font-weight:600;color:#303133">{{ row.courseName }}</span>
            <el-tag v-if="row.courseType === '必修'" size="small" type="" style="margin-left:6px">必修</el-tag>
            <el-tag v-else-if="row.courseType === '选修'" size="small" type="warning" style="margin-left:6px">选修</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上课时间" width="130">
          <template #default="{ row }">
            周{{ weekdayMap[row.weekday] }} {{ row.startSection }}-{{ row.endSection }}节
          </template>
        </el-table-column>
        <el-table-column prop="location" label="上课地点" width="120" />
        <el-table-column label="关联班级" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="(cls, idx) in (row.classNames || '').split(',')" :key="idx" size="small"
              style="margin:1px 4px 1px 0">{{ cls }}</el-tag>
            <span v-if="!row.classNames">-</span>
          </template>
        </el-table-column>
        <el-table-column label="选课人数" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.currentCount >= (row.maxStudents || 0) * 0.9 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
              {{ row.currentCount || 0 }}/{{ row.maxStudents || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="60" align="center" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToScoreEntry(row)">录入成绩</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && courses.length === 0" description="当前学期暂无授课安排" />
    </el-card>

    <el-card style="margin-top:20px" v-if="courses.length > 0">
      <template #header><span>授课概览</span></template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(stat, idx) in overviewStats" :key="idx">
          <el-statistic :title="stat.title" :value="stat.value">
            <template #suffix>{{ stat.suffix }}</template>
          </el-statistic>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTeacherSchedule } from '@/api/course'

const router = useRouter()
const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = now.getMonth() + 1

let baseYear = currentYear
if (currentMonth >= 9) {
  baseYear = currentYear
} else if (currentMonth >= 2) {
  baseYear = currentYear - 1
} else {
  baseYear = currentYear - 1
}

const isSecondSemester = currentMonth >= 2 && currentMonth <= 8
const currentSemesterValue = `${baseYear}-${baseYear + 1}-${isSecondSemester ? '2' : '1'}`

const semesterOptions = [
  { value: `${baseYear - 1}-${baseYear}-1`, label: `${baseYear - 1}-${baseYear} 第一学期` },
  { value: `${baseYear - 1}-${baseYear}-2`, label: `${baseYear - 1}-${baseYear} 第二学期` },
  { value: `${baseYear}-${baseYear + 1}-1`, label: `${baseYear}-${baseYear + 1} 第一学期` },
  { value: `${baseYear}-${baseYear + 1}-2`, label: `${baseYear}-${baseYear + 1} 第二学期` }
]

const weekdayMap = { 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六', 7: '日' }
const semester = ref(currentSemesterValue)
const courses = ref([])
const loading = ref(false)

const overviewStats = computed(() => {
  const totalCourses = new Set(courses.value.map(c => c.courseId)).size
  const totalStudents = courses.value.reduce((sum, c) => sum + (c.currentCount || 0), 0)
  const totalCredits = [...new Set(courses.value.map(c => c.courseId))].reduce((sum, id) => {
    const course = courses.value.find(c => c.courseId === id)
    return sum + (course?.credit || 0)
  }, 0)
  const requiredCount = courses.value.filter(c => c.courseType === '必修').length
  const electiveCount = courses.value.filter(c => c.courseType === '选修').length
  return [
    { title: '授课门数', value: totalCourses, suffix: ' 门' },
    { title: '总学生数', value: totalStudents, suffix: ' 人' },
    { title: '总学分', value: totalCredits, suffix: '' },
    { title: '必修/选修', value: `${requiredCount}/${electiveCount}`, suffix: '' }
  ]
})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getTeacherSchedule({ semester: semester.value })
    courses.value = res.data || []
  } catch (e) {
    ElMessage.error('获取课程数据失败')
    courses.value = []
  } finally {
    loading.value = false
  }
}

const goToScoreEntry = (row) => {
  router.push({ path: '/score-entry', query: { courseId: row.courseId, courseName: row.courseName } })
}

onMounted(fetchCourses)
</script>
