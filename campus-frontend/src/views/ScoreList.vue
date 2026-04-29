<template>
  <div>
    <el-card>
      <template #header><span>成绩查询</span></template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="学期">
          <el-select v-model="semester" placeholder="选择学期" clearable style="width:220px" @change="fetchScores">
            <el-option label="全部学期" value="" />
            <el-option label="2025-2026学年第一学期" value="2025-2026-1" />
            <el-option label="2025-2026学年第二学期" value="2025-2026-2" />
            <el-option label="2026-2027学年第一学期" value="2026-2027-1" />
            <el-option label="2026-2027学年第二学期" value="2026-2027-2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchScores">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="scores" stripe v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="usualScore" label="平时成绩" width="100" />
        <el-table-column prop="examScore" label="考试成绩" width="100" />
        <el-table-column prop="finalScore" label="最终成绩" width="100">
          <template #default="{ row }">
            <el-tag :type="row.finalScore >= 60 ? 'success' : 'danger'">{{ row.finalScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gradePoint" label="绩点" width="80" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <el-empty v-if="!loading && scores.length === 0" description="暂无成绩数据" />
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span>成绩统计</span></template>
      <el-descriptions :column="4" border v-if="statistics.totalStudents > 0">
        <el-descriptions-item label="平均分">{{ statistics.avgScore }}</el-descriptions-item>
        <el-descriptions-item label="最高分">{{ statistics.maxScore }}</el-descriptions-item>
        <el-descriptions-item label="最低分">{{ statistics.minScore }}</el-descriptions-item>
        <el-descriptions-item label="及格率">{{ statistics.passRate }}%</el-descriptions-item>
        <el-descriptions-item label="优秀率">{{ statistics.excellentRate }}%</el-descriptions-item>
        <el-descriptions-item label="总学分">{{ statistics.totalCredits }}</el-descriptions-item>
        <el-descriptions-item label="GPA">{{ statistics.gpa }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无统计数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listStudentScores, getStudentStatistics } from '@/api/score'

const semester = ref('')
const scores = ref([])
const statistics = ref({})
const loading = ref(false)

const fetchScores = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (semester.value) params.semester = semester.value
    const [scoreRes, statRes] = await Promise.allSettled([
      listStudentScores(params),
      getStudentStatistics(params)
    ])
    if (scoreRes.status === 'fulfilled') scores.value = scoreRes.value.data.records || []
    if (statRes.status === 'fulfilled') statistics.value = statRes.value.data || {}
  } catch (e) {
    ElMessage.error('获取成绩数据失败')
    scores.value = []
    statistics.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(fetchScores)
</script>