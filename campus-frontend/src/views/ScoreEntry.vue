<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>成绩录入</span>
          <div style="display:flex;gap:10px;align-items:center">
            <el-select v-model="semester" placeholder="选择学期" style="width:220px" @change="onSemesterChange">
              <el-option v-for="(s, idx) in semesterOptions" :key="s.value" :label="s.label + (idx === currentSemesterIdx ? '（当前）' : '')" :value="s.value" />
            </el-select>
            <el-select v-model="selectedCourseId" placeholder="选择课程" style="width:240px" @change="fetchStudents">
              <el-option v-for="c in courseList" :key="c.courseId" :label="c.name" :value="c.courseId" />
            </el-select>
            <el-button type="success" :disabled="!selectedCourseId || students.length === 0 || saving || isPastSemester(semester)"
              @click="batchSave" :loading="saving">批量保存</el-button>
          </div>
        </div>
      </template>

      <el-alert v-if="!selectedCourseId" title="请先选择一门课程" type="info" :closable="false" show-icon style="margin-bottom:16px" />

      <el-alert v-if="selectedCourseId && !isPastSemester(semester)" title="成绩计算规则：最终成绩 = 平时成绩 × 30% + 考试成绩 × 70%" type="success" :closable="false" show-icon style="margin-bottom:12px" />

      <el-alert v-if="selectedCourseId && isPastSemester(semester)" title="该学期已结束，成绩为只读状态，不可修改" type="warning" :closable="false" show-icon style="margin-bottom:12px" />

      <el-table :data="students" stripe v-loading="loading" border v-if="selectedCourseId">
        <el-table-column label="学号" prop="studentNo" width="120" />
        <el-table-column label="姓名" prop="studentName" width="100">
          <template #default="{ row }">
            <span style="font-weight:600">{{ row.studentName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="班级" prop="className" width="130" />
        <el-table-column label="平时成绩" width="120" align="center">
          <template #default="{ row }">
            <el-input v-model.number="row.usualScore" type="number" min="0" max="100" size="small"
              style="width:80px" placeholder="0-100" :disabled="isPastSemester(semester)" @blur="calcFinal(row)" @keyup.enter="calcFinal(row)" />
          </template>
        </el-table-column>
        <el-table-column label="考试成绩" width="120" align="center">
          <template #default="{ row }">
            <el-input v-model.number="row.examScore" type="number" min="0" max="100" size="small"
              style="width:80px" placeholder="0-100" :disabled="isPastSemester(semester)" @blur="calcFinal(row)" @keyup.enter="calcFinal(row)" />
          </template>
        </el-table-column>
        <el-table-column label="最终成绩" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.finalScore >= 60 ? 'success' : 'danger'" effect="dark" size="large"
              style="min-width:48px;font-size:15px;font-weight:700">
              {{ row.finalScore ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="saving || isPastSemester(semester)" @click="saveSingle(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="selectedCourseId && !loading && students.length === 0" description="该课程暂无选课学生" />

      <div v-if="students.length > 0" style="margin-top:16px;display:flex;justify-content:space-between;align-items:center;font-size:13px;color:#606266">
        <span>共 <strong>{{ students.length }}</strong> 名学生</span>
        <span>
          已录入 {{ students.filter(s => s.finalScore !== null && s.finalScore !== undefined).length }} 人，
          待录入 {{ students.filter(s => s.finalScore === null || s.finalScore === undefined).length }} 人
        </span>
      </div>
    </el-card>

    <el-card style="margin-top:20px" v-if="selectedCourseId && scoreStats.total > 0">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>成绩统计分析</span>
          <el-tag type="info">共 {{ scoreStats.total }} 人</el-tag>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="14">
          <div ref="chartRef" style="height:320px;width:100%"></div>
        </el-col>
        <el-col :span="10">
          <el-descriptions :column="2" border style="margin-bottom:16px">
            <el-descriptions-item label="平均分">
              <span :style="{color: scoreStats.avg >= 60 ? '#67c23a' : '#f56c6c', fontWeight: 700}">{{ scoreStats.avg }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="最高分">
              <span style="color:#409eff;font-weight:700">{{ scoreStats.max }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="最低分">
              <span style="color:#f56c6c;font-weight:700">{{ scoreStats.min }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="中位数">
              <span style="font-weight:700">{{ scoreStats.median }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="及格率">
              <span :style="{color: scoreStats.passRate >= 80 ? '#67c23a' : '#e6a23c', fontWeight: 700}">{{ scoreStats.passRate }}%</span>
            </el-descriptions-item>
            <el-descriptions-item label="优秀率">
              <span style="color:#9b59b6;font-weight:700">{{ scoreStats.excellentRate }}%</span>
            </el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:8px">
            <div v-for="item in scoreStats.distribution" :key="item.label" style="display:flex;align-items:center;margin-bottom:6px;font-size:13px">
              <span style="width:70px;flex-shrink:0">{{ item.label }}</span>
              <el-progress :percentage="item.rate" :color="item.color" :stroke-width="16" style="flex:1" :format="() => item.count + '人'" />
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeacherSchedule } from '@/api/course'
import { listCourseScores, listCourseStudentsForEntry, addScore, updateScore, getCourseStatistics } from '@/api/score'

const route = useRoute()
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
  { value: `${baseYear - 1}-${baseYear}-1`, label: `${baseYear - 1}-${baseYear} 第一学期`, year: baseYear - 1, sem: 1 },
  { value: `${baseYear - 1}-${baseYear}-2`, label: `${baseYear - 1}-${baseYear} 第二学期`, year: baseYear - 1, sem: 2 },
  { value: `${baseYear}-${baseYear + 1}-1`, label: `${baseYear}-${baseYear + 1} 第一学期`, year: baseYear, sem: 1 },
  { value: `${baseYear}-${baseYear + 1}-2`, label: `${baseYear}-${baseYear + 1} 第二学期`, year: baseYear, sem: 2 }
]

const currentSemesterIdx = semesterOptions.findIndex(s => s.value === currentSemesterValue)

function isPastSemester(semValue) {
  if (!semValue) return false
  const idx = semesterOptions.findIndex(s => s.value === semValue)
  return idx >= 0 && idx < currentSemesterIdx
}

const semester = ref(currentSemesterValue)
const courseList = ref([])
const selectedCourseId = ref(null)
const students = ref([])
const loading = ref(false)
const saving = ref(false)

const fetchCourseList = async () => {
  try {
    const res = await getTeacherSchedule({ semester: semester.value })
    const raw = res.data || []
    const seen = new Set()
    courseList.value = raw.filter(c => {
      if (seen.has(c.courseId)) return false
      seen.add(c.courseId)
      return true
    }).map(c => ({ courseId: c.courseId, name: c.courseName, code: c.code }))
    if (route.query.courseId) {
      selectedCourseId.value = Number(route.query.courseId)
      fetchStudents()
    }
  } catch (e) {
    ElMessage.error('获取课程列表失败')
  }
}

const onSemesterChange = () => {
  selectedCourseId.value = null
  students.value = []
  fetchCourseList()
}

const fetchStudents = async () => {
  if (!selectedCourseId.value) return
  loading.value = true
  try {
    const res = await listCourseStudentsForEntry(selectedCourseId.value)
    const records = (res.data || []).map(s => ({
      ...s,
      studentNo: s.userNo,
      usualScore: s.usualScore ?? null,
      examScore: s.examScore ?? null,
      finalScore: s.finalScore ?? null
    }))
    students.value = records
  } catch (e) {
    ElMessage.error('获取学生名单失败')
    students.value = []
  } finally {
    loading.value = false
  }
}

const calcFinal = (row) => {
  const u = Number(row.usualScore)
  const e = Number(row.examScore)
  if (!isNaN(u) && !isNaN(e)) {
    row.usualScore = Math.max(0, Math.min(100, u))
    row.examScore = Math.max(0, Math.min(100, e))
    row.finalScore = Math.round(row.usualScore * 0.3 + row.examScore * 0.7)
  } else {
    row.finalScore = null
  }
}

const saveSingle = async (row) => {
  if (row.usualScore == null || row.examScore == null) {
    ElMessage.warning('请先填写平时成绩和考试成绩')
    return
  }
  saving.value = true
  try {
    const data = {
      studentId: row.studentId,
      courseId: selectedCourseId.value,
      usualScore: row.usualScore,
      examScore: row.examScore,
      finalScore: row.finalScore,
      semester: semester.value
    }
    if (row.id) {
      await updateScore({ ...data, id: row.id })
    } else {
      await addScore(data)
    }
    ElMessage.success(`已保存 ${row.studentName} 的成绩`)
    fetchStudents()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const batchSave = async () => {
  const unsaved = students.value.filter(s => s.usualScore != null && s.examScore != null)
  if (unsaved.length === 0) {
    ElMessage.warning('没有可保存的成绩数据')
    return
  }
  await ElMessageBox.confirm(`确认批量保存 ${unsaved.length} 条成绩记录？`, '批量保存', { type: 'warning' })
  saving.value = true
  let ok = 0
  let fail = 0
  for (const row of unsaved) {
    try {
      const data = {
        studentId: row.studentId,
        courseId: selectedCourseId.value,
        usualScore: row.usualScore,
        examScore: row.examScore,
        finalScore: row.finalScore,
        semester: semester.value
      }
      if (row.id) {
        await updateScore({ ...data, id: row.id })
      } else {
        await addScore(data)
      }
      ok++
    } catch {
      fail++
    }
  }
  saving.value = false
  if (fail === 0) {
    ElMessage.success(`全部 ${ok} 条成绩保存成功`)
  } else {
    ElMessage.warning(`成功 ${ok} 条，失败 ${fail} 条`)
  }
  fetchStudents()
}

const chartRef = ref(null)
let chartInstance = null

const scoreStats = computed(() => {
  const scored = students.value.filter(s => s.finalScore !== null && s.finalScore !== undefined)
  const total = scored.length
  if (total === 0) return { total: 0 }

  const scores = scored.map(s => s.finalScore).sort((a, b) => a - b)
  const sum = scores.reduce((a, b) => a + b, 0)
  const avg = (sum / total).toFixed(1)
  const max = scores[total - 1]
  const min = scores[0]
  const median = total % 2 === 0 ? ((scores[total / 2 - 1] + scores[total / 2]) / 2).toFixed(1) : scores[Math.floor(total / 2)]
  const passCount = scores.filter(s => s >= 60).length
  const excellentCount = scores.filter(s => s >= 90).length

  const ranges = [
    { label: '90-100分', min: 90, max: 100, color: '#67c23a' },
    { label: '80-89分', min: 80, max: 89, color: '#409eff' },
    { label: '70-79分', min: 70, max: 79, color: '#e6a23c' },
    { label: '60-69分', min: 60, max: 69, color: '#f5a623' },
    { label: '0-59分', min: 0, max: 59, color: '#f56c6c' }
  ]
  const distribution = ranges.map(r => {
    const count = scores.filter(s => s >= r.min && s <= r.max).length
    return { ...r, count, rate: total > 0 ? Math.round(count / total * 100) : 0 }
  })

  return {
    total, avg: Number(avg), max, min, median: Number(median),
    passRate: (passCount / total * 100).toFixed(1),
    excellentRate: (excellentCount / total * 100).toFixed(1),
    distribution
  }
})

const renderChart = async () => {
  if (!chartRef.value || scoreStats.value.total === 0) return
  const echarts = await import('echarts')
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const dist = scoreStats.value.distribution
  chartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 50, right: 30, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: dist.map(d => d.label), axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', name: '人数', minInterval: 1 },
    series: [{
      type: 'bar',
      data: dist.map(d => ({ value: d.count, itemStyle: { color: d.color, borderRadius: [4, 4, 0, 0] } })),
      barWidth: '50%',
      label: { show: true, position: 'top', formatter: '{c}人', fontSize: 12, fontWeight: 'bold' }
    }]
  })
}

watch(scoreStats, () => {
  nextTick(() => renderChart())
}, { deep: true })

onMounted(fetchCourseList)
</script>
