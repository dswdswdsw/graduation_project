<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>{{ userStore.role === 'teacher' ? '教师课表' : '我的课表' }}</span>
          <div style="display:flex;gap:10px;align-items:center">
            <el-select v-model="semester" placeholder="选择学期" style="width:200px" @change="fetchSchedule">
              <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-radio-group v-model="viewMode" size="small" @change="fetchSchedule">
              <el-radio-button value="week">周视图</el-radio-button>
              <el-radio-button value="day">日视图</el-radio-button>
            </el-radio-group>
            <el-select v-model="selectedDay" v-if="viewMode === 'day'" style="width:100px" @change="fetchSchedule">
              <el-option v-for="d in 7" :key="d" :label="'周'+['一','二','三','四','五','六','日'][d-1]" :value="d" />
            </el-select>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="schedule-grid">
        <div class="schedule-header">
            <div class="time-col">节次/时间</div>
          <template v-if="viewMode === 'week'">
            <div v-for="d in 7" :key="d" class="day-col">{{ '一二三四五六日'[d - 1] }}</div>
          </template>
          <div v-else class="day-col-full">周{{ ['一','二','三','四','五','六','日'][selectedDay - 1] }}</div>
        </div>
        <div v-for="section in maxSections" :key="section" class="schedule-row">
            <div class="time-col"><div class="sec-num">{{ section }}</div><div class="sec-time">{{ getSectionTime(section) }}</div></div>
          <template v-if="viewMode === 'week'">
            <div v-for="d in 7" :key="d" class="day-cell">
              <div v-for="item in getItems(d, section)" :key="item.courseId" class="course-block"
                :style="{ background: getColor(item.courseId), minHeight: getBlockHeight(item) }"
                @click="showCourseDetail(item)">
                <div class="cb-name">{{ item.courseName }}</div>
                <div class="cb-sub">{{ item.teacherName }} · {{ item.locationDisplay || item.location }}</div>
                <div class="cb-time">{{ item.startSection }}-{{ item.endSection }}节</div>
              </div>
            </div>
          </template>
          <div v-else class="day-cell-full">
            <div v-for="item in getItems(selectedDay, section)" :key="item.courseId" class="course-block-large"
              :style="{ background: getColor(item.courseId) }"
              @click="showCourseDetail(item)">
              <div class="cbl-name">{{ item.code }} {{ item.courseName }}</div>
              <div class="cbl-row"><strong>{{ item.teacherName }}</strong> · {{ item.locationDisplay || item.location }}</div>
              <div class="cbl-row">第 {{ item.startSection }}-{{ item.endSection }} 节</div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && schedule.length === 0" description="当前学期暂无课程安排，请先选课" />

    </el-card>

    <el-dialog v-model="detailVisible" :title="currentDetail?.courseName || '课程详情'" width="440px" destroy-on-close>
      <template v-if="currentDetail">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="课程编号">{{ currentDetail.code }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ currentDetail.courseName }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ currentDetail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">周{{ ['一','二','三','四','五','六','日'][currentDetail.weekday-1] }} 第{{ currentDetail.startSection }}-{{ currentDetail.endSection }}节</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ currentDetail.locationDisplay || currentDetail.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联班级" v-if="currentDetail.classNames">{{ currentDetail.classNames || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentSchedule, getTeacherSchedule } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentYear = new Date().getFullYear()
const semesterOptions = [
  { value: `${currentYear - 1}-${currentYear}-1`, label: `${currentYear - 1}-${currentYear} 第一学期` },
  { value: `${currentYear - 1}-${currentYear}-2`, label: `${currentYear - 1}-${currentYear} 第二学期` },
  { value: `${currentYear}-${currentYear + 1}-1`, label: `${currentYear}-${currentYear + 1} 第一学期` },
  { value: `${currentYear}-${currentYear + 1}-2`, label: `${currentYear}-${currentYear + 1} 第二学期` }
]

const semester = ref(semesterOptions[2].value)
const schedule = ref([])
const loading = ref(false)
const viewMode = ref('week')
const selectedDay = ref(1)
const detailVisible = ref(false)
const currentDetail = ref(null)

const colors = ['#409eff','#67c23a','#e6a23c','#f56c6c','#909399','#9b59b6','#1abc9c','#e74c3c','#3498db','#2ecc71']
const getColor = (id) => colors[(id * 31 + 7) % colors.length]
const maxSections = computed(() => 12)

const sectionTimes = ['08:00','08:50','09:50','10:40','11:30','14:00','14:50','15:50','16:40','17:30','19:00','19:50']
const getSectionTime = (sec) => {
  const start = sectionTimes[sec - 1] || ''
  const endIdx = sec < sectionTimes.length ? sec : sec - 1
  const end = (sectionTimes[endIdx] || '').replace(/(\d{2}):(\d{2})/, (_, h, m) => {
    return String(Number(h) + Math.floor((Number(m) + 45) / 60)).padStart(2, '0') + ':' + String((Number(m) + 45) % 60).padStart(2, '0')
  })
  return start && end ? `${start}-${end}` : ''
}

const getItems = (weekday, section) =>
  schedule.value.filter(s => s.weekday === weekday && section >= s.startSection && section <= s.endSection)

const getBlockHeight = (item) => Math.max((item.endSection - item.startSection + 1) * 46, 46) + 'px'

const showCourseDetail = (item) => { currentDetail.value = item; detailVisible.value = true }

const fetchSchedule = async () => {
  loading.value = true
  try {
    const api = userStore.role === 'teacher' ? getTeacherSchedule : getStudentSchedule
    const res = await api({ semester: semester.value })
    const rawList = res.data || []
    const seen = new Set()
    schedule.value = rawList.filter(item => {
      const key = item.courseId + '_' + item.weekday + '_' + item.startSection + '_' + item.endSection
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
  } catch (e) {
    ElMessage.error('获取课表失败')
    schedule.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchSchedule)
</script>

<style scoped>
.schedule-grid { width: 100%; border: 1px solid #ebeef5; border-radius: 4px; overflow-x: auto; }
.schedule-header { display: flex; background: #f5f7fa; font-weight: 600; font-size: 13px; }
.schedule-row { display: flex; border-top: 1px solid #ebeef5; min-height: 48px; }
.time-col { width: 72px; min-height: 48px; display: flex; flex-direction: column; align-items: center; justify-content: center; border-right: 1px solid #ebeef5; font-size: 12px; color: #606266; background: #fafafa; flex-shrink: 0; }
.sec-num { font-weight: 600; font-size: 13px; line-height: 1.2; }
.sec-time { font-size: 10px; color: #909399; margin-top: 2px; }
.day-col { flex: 1; min-width: 90px; border-right: 1px solid #ebeef5; padding: 1px; text-align: center; font-size: 12px; }
.day-col-full { flex: 1; min-width: 280px; border-right: 1px solid #ebeef5; padding: 1px; text-align: center; font-size: 13px; }
.day-cell { flex: 1; min-width: 90px; border-right: 1px solid #ebeef5; padding: 1px; min-height: 48px; }
.day-cell-full { flex: 1; min-width: 280px; border-right: 1px solid #ebeef5; padding: 3px; min-height: 60px; }
.course-block { padding: 4px 6px; border-radius: 4px; color: #fff; font-size: 11px; margin: 1px; cursor: pointer; transition: transform 0.1s, box-shadow 0.1s; line-height: 1.4; }
.course-block:hover { transform: scale(1.03); box-shadow: 0 2px 10px rgba(0,0,0,0.2); z-index: 2; position: relative; }
.cb-name { font-weight: bold; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cb-sub { opacity: 0.88; font-size: 10px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cb-time { opacity: 0.9; font-size: 10px; margin-top: 1px; font-weight: 500; }
.course-block-large { padding: 8px 14px; border-radius: 6px; color: #fff; font-size: 12px; margin: 2px; cursor: pointer; transition: transform 0.1s; line-height: 1.6; }
.course-block-large:hover { transform: translateY(-1px); box-shadow: 0 3px 12px rgba(0,0,0,0.22); }
.cbl-name { font-weight: bold; font-size: 14px; }
.cbl-row { opacity: 0.92; font-size: 12px; margin-top: 2px; }
</style>
