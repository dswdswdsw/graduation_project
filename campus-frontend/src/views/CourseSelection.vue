<template>
  <div class="selection-page">
    <el-card class="card-select">
      <template #header>
        <div class="card-header-row">
          <span class="header-title">🎯 在线选课</span>
          <div class="header-actions">
            <el-select v-model="query.semester" placeholder="选择学期" clearable style="width:200px" @change="fetchCourses">
              <el-option label="2026-2027学年第一学期" value="2026-2027-1" />
              <el-option label="2026-2027学年第二学期" value="2026-2027-2" />
            </el-select>
            <el-input v-model="query.keyword" placeholder="课程名/编号" prefix-icon="Search" clearable
              @keyup.enter="fetchCourses" style="width:220px" />
            <el-button type="primary" @click="fetchCourses">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="courses" stripe v-loading="loading" :header-cell-style="{ background:'#fafafa', color:'#606266', fontWeight:600 }">
        <el-table-column prop="code" label="课程编号" width="110" />
        <el-table-column prop="name" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="95" />
        <el-table-column label="类型" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.courseType === '选修' ? 'warning' : ''" size="small" effect="light" round>{{ row.courseType === '选修' ? '选修' : '必修' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="60" align="center" />
        <el-table-column label="上课时间" width="120">
          <template #default="{ row }">周{{ ['一','二','三','四','五','六','日'][row.weekday - 1] }} {{ row.startSection }}-{{ row.endSection }}节</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="110" show-overflow-tooltip />
        <el-table-column label="余量" width="80" align="center">
          <template #default="{ row }">
            <span :class="['remain-text', row.remainCount > 10 ? 'remain-ok' : row.remainCount > 3 ? 'remain-warn' : 'remain-danger']">
              {{ row.remainCount }}/{{ row.maxStudents }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" :type="selectedIds.includes(row.id) ? 'success' : 'primary'"
              @click="handleSelect(row.id)" :disabled="row.remainCount <= 0 && !selectedIds.includes(row.id)">
              {{ selectedIds.includes(row.id) ? '✓ 已选' : row.remainCount <= 0 ? '已满' : '+ 选课' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.page"
        v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchCourses" />
      <el-empty v-if="!loading && courses.length === 0" description="暂无可选课程" />
    </el-card>

    <el-card class="card-my" style="margin-top:20px">
      <template #header>
        <div class="card-header-row">
          <div class="my-header-left">
            <span class="header-title">✅ 我的课表</span>
            <el-tag size="small" type="success" effect="light">{{ mySelections.length }} 门</el-tag>
            <span class="credit-badge" v-if="mySelections.length > 0">共 {{ totalCredits }} 学分</span>
          </div>
        </div>
      </template>

      <el-table :data="sortedMyCourses" stripe :header-cell-style="{ background:'#f0f9ff', color:'#303133', fontWeight:600 }">
        <el-table-column prop="name" label="课程名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }"><b>{{ row.name }}</b></template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="90" />
        <el-table-column label="类型" width="65" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.courseType === '选修' ? 'warning' : ''" effect="plain">{{ row.courseType === '选修' ? '选修' : '必修' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="55" align="center" />
        <el-table-column label="上课时间" width="125">
          <template #default="{ row }">周{{ ['一','二','三','四','五','六','日'][row.weekday - 1] }} {{ row.startSection }}-{{ row.endSection }}节</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="105" show-overflow-tooltip />
        <el-table-column prop="createTime" label="选课时间" width="155">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="75" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="handleDrop(row.courseId)">退选</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="mySelections.length === 0" description="还没有选择任何课程，快去上方挑选吧 🚀" />

      <div v-if="mySelections.length > 0" class="credit-bar-wrap">
        <div class="bar-label">学分进度</div>
        <div class="bar-track">
          <div class="bar-fill" :style="{ width: Math.min(totalCredits / 30 * 100, 100) + '%' }"></div>
          <span class="bar-text">{{ totalCredits }}/30</span>
        </div>
        <div class="bar-detail">
          必修 {{ requiredCredits }}分 · 选修 {{ electiveCredits }}分
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCourses } from '@/api/course'
import { selectCourse, dropCourse, listMySelections } from '@/api/courseSelection'
import { formatTime } from '@/utils/format'

const courses = ref([])
const mySelections = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ semester: '2026-2027-1', keyword: '', page: 1, size: 10 })

const selectedIds = computed(() => mySelections.value.map(s => s.courseId))

const totalCredits = computed(() => mySelections.value.reduce((sum, s) => sum + (Number(s.credit) || 0), 0))
const requiredCredits = computed(() => mySelections.value.filter(s => s.courseType !== '选修').reduce((sum, s) => sum + (Number(s.credit) || 0), 0))
const electiveCredits = computed(() => totalCredits.value - requiredCredits.value)

const sortedMyCourses = computed(() => {
  return [...mySelections.value].sort((a, b) => {
    if ((a.weekday || 7) !== (b.weekday || 7)) return (a.weekday || 7) - (b.weekday || 7)
    return (a.startSection || 99) - (b.startSection || 99)
  })
})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await listCourses(query)
    courses.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

const fetchMySelections = async () => {
  try {
    const res = await listMySelections({ page: 1, size: 100 })
    mySelections.value = res.data.records || []
  } catch (e) {
    ElMessage.error('获取已选课程失败')
  }
}

const handleSelect = async (courseId) => {
  if (selectedIds.value.includes(courseId)) return
  const course = courses.value.find(c => c.id === courseId)

  const conflictCourse = mySelections.value.find(s => {
    if (!s.weekday || !course) return false
    if (s.weekday !== course.weekday) return false
    return !(course.endSection < s.startSection || course.startSection > s.endSection)
  })
  if (conflictCourse) {
    await ElMessageBox.confirm(
      `该课程与已选课程「${conflictCourse.name}」（周${['一','二','三','四','五','六','日'][conflictCourse.weekday - 1]} 第${conflictCourse.startSection}-${conflictCourse.endSection}节）时间冲突，是否继续尝试选课？`,
      '时间冲突提醒',
      { confirmButtonText: '继续选课', cancelButtonText: '取消', type: 'warning' }
    )
  } else {
    await ElMessageBox.confirm(`确定选择「${course?.name}」？`, '确认选课', {
      confirmButtonText: '确定选课',
      cancelButtonText: '再想想',
      type: 'info'
    })
  }

  try {
    await selectCourse(courseId)
    ElMessage.success({ message: `成功选课：${course?.name}`, duration: 2000 })
    fetchCourses()
    fetchMySelections()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '选课失败'
    if (msg.includes('冲突')) {
      ElMessage.error({ message: msg, duration: 4000 })
    } else {
      ElMessage.error(msg)
    }
  }
}

const handleDrop = async (courseId) => {
  const sel = mySelections.value.find(s => s.courseId === courseId)
  await ElMessageBox.confirm(`确定退选「${sel?.name}」？`, '确认退选', {
    confirmButtonText: '确定退选',
    cancelButtonText: '保留',
    type: 'warning'
  })
  await dropCourse(courseId)
  ElMessage.success({ message: `已退选：${sel?.name}`, duration: 2000 })
  fetchCourses()
  fetchMySelections()
}

onMounted(() => {
  fetchCourses()
  fetchMySelections()
})
</script>

<style scoped>
.selection-page { padding: 2px; }

.card-select :deep(.el-card__header),
.card-my :deep(.el-card__header) { padding: 14px 20px; border-bottom: 1px solid #ebeef5; }

.card-header-row {
  display: flex; align-items: center; justify-content: space-between;
}
.header-title { font-size: 16px; font-weight: 700; color: #303133; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.my-header-left { display: flex; align-items: center; gap: 8px; }

.credit-badge {
  font-size: 12px; color: #67c23a; font-weight: 600;
  background: #f0f9eb; padding: 2px 10px; border-radius: 12px;
}

/* 余量颜色 */
.remain-text { font-size: 13px; font-weight: 500; }
.remain-ok { color: #67c23a; }
.remain-warn { color: #e6a23c; }
.remain-danger { color: #f56c6c; font-weight: 700; }

/* 星期圆点 */
.weekday-dot {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%; color: #fff;
  font-size: 12px; font-weight: 700;
}
.dot-1 { background: linear-gradient(135deg, #ff6b6b, #ee5a5a); }
.dot-2 { background: linear-gradient(135deg, #ffa94d, #fd7e14); }
.dot-3 { background: linear-gradient(135deg, #ffd43b, #fab005); }
.dot-4 { background: linear-gradient(135deg, #69db7c, #40c057); }
.dot-5 { background: linear-gradient(135deg, #74c0fc, #339af0); }
.dot-6 { background: linear-gradient(135deg, #b197fc, #7950f2); }
.dot-7 { background: linear-gradient(135deg, #e599f7, #be4bdb); }

/* 学分进度条 */
.credit-bar-wrap {
  margin-top: 18px; padding: 14px 18px; background: #fafbfc;
  border-radius: 10px; border: 1px solid #ebeef5;
}
.bar-label { font-size: 13px; color: #606266; margin-bottom: 8px; font-weight: 600; }
.bar-track {
  position: relative; height: 20px; background: #ebeef5; border-radius: 10px; overflow: hidden;
}
.bar-fill {
  height: 100%; background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 10px; transition: width .6s ease;
}
.bar-text {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  font-size: 12px; color: #fff; font-weight: 700; text-shadow: 0 1px 3px rgba(0,0,0,.25);
}
.bar-detail { margin-top: 6px; font-size: 12px; color: #909399; text-align: right; }
</style>
