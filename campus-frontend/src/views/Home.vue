<template>
  <div class="home-page">
    <el-row :gutter="20">
      <template v-if="role === 'student'">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/course-selection')" style="cursor:pointer">
            <div class="stat-icon" style="background:#409eff"><el-icon :size="28"><Reading /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/homework')" style="cursor:pointer">
            <div class="stat-icon" style="background:#67c23a"><el-icon :size="28"><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.homeworkCount }}</div>
              <div class="stat-label">待交作业</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/repair')" style="cursor:pointer">
            <div class="stat-icon" style="background:#e6a23c"><el-icon :size="28"><SetUp /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.repairCount }}</div>
              <div class="stat-label">报修工单</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/notice')" style="cursor:pointer">
            <div class="stat-icon" style="background:#f56c6c"><el-icon :size="28"><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.unreadCount }}</div>
              <div class="stat-label">未读通知</div>
            </div>
          </el-card>
        </el-col>
      </template>

      <template v-else-if="role === 'teacher'">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/teacher-courses')" style="cursor:pointer">
            <div class="stat-icon" style="background:#409eff"><el-icon :size="28"><Reading /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">我的课程</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/course')" style="cursor:pointer">
            <div class="stat-icon" style="background:#67c23a"><el-icon :size="28"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">选课学生</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/homework')" style="cursor:pointer">
            <div class="stat-icon" style="background:#e6a23c"><el-icon :size="28"><EditPen /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingScore }}</div>
              <div class="stat-label">待批改作业</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/notice')" style="cursor:pointer">
            <div class="stat-icon" style="background:#f56c6c"><el-icon :size="28"><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.unreadCount }}</div>
              <div class="stat-label">未读通知</div>
            </div>
          </el-card>
        </el-col>
      </template>

      <template v-else-if="role === 'admin'">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/course')" style="cursor:pointer">
            <div class="stat-icon" style="background:#67c23a"><el-icon :size="28"><Reading /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ adminStats.courseCount }}</div>
              <div class="stat-label">课程总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/repair')" style="cursor:pointer">
            <div class="stat-icon" style="background:#e6a23c"><el-icon :size="28"><SetUp /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ adminStats.pendingRepairCount }}</div>
              <div class="stat-label">待处理工单</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/notice')" style="cursor:pointer">
            <div class="stat-icon" style="background:#f56c6c"><el-icon :size="28"><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ adminStats.noticeCount }}</div>
              <div class="stat-label">通知公告</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/classroom')" style="cursor:pointer">
            <div class="stat-icon" style="background:#9b59b6"><el-icon :size="28"><School /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ adminStats.classroomCount }}</div>
              <div class="stat-label">教室数量</div>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="role === 'admin' ? 24 : 14">
        <el-card>
          <template #header><span>最新通知</span></template>
          <el-table :data="notices" stripe style="width:100%">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <span style="cursor:pointer;color:#409eff" @click="viewNotice(row)">{{ row.isTop ? '【置顶】' : '' }}{{ row.title }}</span>
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
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10" v-if="role !== 'admin'">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>今日课程</span>
              <el-tag size="small" type="info">{{ todayCourses.length }} 门</el-tag>
            </div>
          </template>
          <div v-if="todayCourses.length === 0" style="text-align:center;padding:30px 0;color:#c0c4cc">
            <el-icon :size="40"><Calendar /></el-icon>
            <div style="margin-top:10px">今天没有课程安排</div>
          </div>
          <div v-else class="today-course-list">
            <div v-for="c in todayCourses" :key="c.courseId" class="today-course-item">
              <div class="course-time-badge">
                <div class="time-section">第{{ c.startSection }}-{{ c.endSection }}节</div>
              </div>
              <div class="course-info">
                <div class="course-name">{{ c.courseName }}</div>
                <div class="course-meta">
                  <span v-if="role === 'student'">{{ c.teacherName }}</span>
                  <span v-if="role === 'teacher'">{{ c.currentCount || 0 }}名学生</span>
                  <span>{{ c.location }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailVisible" :title="detailNotice.title" width="650px">
      <div style="margin-bottom:12px;color:#999;font-size:13px">
        {{ categoryLabel(detailNotice.category) }} | {{ detailNotice.publisherName }} | {{ formatTime(detailNotice.publishTime) }}
      </div>
      <div style="line-height:1.8;white-space:pre-wrap">{{ detailNotice.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'
import { listNotices, markAsRead } from '@/api/notice'
import { getStudentDashboard, getTeacherDashboard, getAdminDashboard, getTodayCourses } from '@/api/dashboard'
import eventBus from '@/utils/eventBus'

const userStore = useUserStore()
const role = userStore.role

const stats = ref({ courseCount: 0, homeworkCount: 0, repairCount: 0, unreadCount: 0, studentCount: 0, pendingScore: 0 })
const adminStats = ref({ userCount: 0, studentCount: 0, teacherCount: 0, courseCount: 0, selectionCount: 0, classroomCount: 0, pendingRepairCount: 0, totalRepairCount: 0, noticeCount: 0 })
const notices = ref([])
const todayCourses = ref([])
const detailVisible = ref(false)
const detailNotice = ref({})

const categoryLabel = (c) => ({ academic: '教务', activity: '活动', urgent: '紧急', general: '普通' }[c] || '普通')
const categoryTag = (c) => ({ academic: '', activity: 'success', urgent: 'danger', general: 'info' }[c] || 'info')

const viewNotice = async (row) => {
  detailNotice.value = row
  detailVisible.value = true
  if (!row.isRead) {
    await markAsRead(row.id)
    row.isRead = true
    if (stats.value.unreadCount > 0) stats.value.unreadCount--
    eventBus.emit('notice:read')
  }
}

onMounted(async () => {
  try {
    const [noticeRes] = await Promise.allSettled([
      listNotices({ page: 1, size: 10 })
    ])
    if (noticeRes.status === 'fulfilled') notices.value = noticeRes.value.data?.records || []

    if (role === 'student') {
      const [dashRes, todayRes] = await Promise.allSettled([getStudentDashboard(), getTodayCourses()])
      if (dashRes.status === 'fulfilled') {
        const d = dashRes.value.data
        stats.value = { courseCount: d.courseCount || 0, homeworkCount: d.homeworkCount || 0, repairCount: d.repairCount || 0, unreadCount: d.unreadCount || 0 }
      }
      if (todayRes.status === 'fulfilled') todayCourses.value = todayRes.value.data || []
    } else if (role === 'teacher') {
      const [dashRes, todayRes] = await Promise.allSettled([getTeacherDashboard(), getTodayCourses()])
      if (dashRes.status === 'fulfilled') {
        const d = dashRes.value.data
        stats.value = { courseCount: d.courseCount || 0, studentCount: d.studentCount || 0, pendingScore: d.pendingScore || 0, unreadCount: d.unreadCount || 0 }
      }
      if (todayRes.status === 'fulfilled') todayCourses.value = todayRes.value.data || []
    } else if (role === 'admin') {
      const [dashRes, todayRes] = await Promise.allSettled([getAdminDashboard(), getTodayCourses()])
      if (dashRes.status === 'fulfilled') adminStats.value = dashRes.value.data
      if (todayRes.status === 'fulfilled') todayCourses.value = todayRes.value.data || []
    }
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; width: 100%; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; }
.stat-value { font-size: 24px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }

.today-course-list { display: flex; flex-direction: column; gap: 10px; }
.today-course-item { display: flex; align-items: center; gap: 14px; padding: 10px 12px; border-radius: 8px; background: #f8f9fa; transition: background 0.2s; }
.today-course-item:hover { background: #ecf5ff; }
.course-time-badge { flex-shrink: 0; }
.time-section { background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.course-info { flex: 1; min-width: 0; }
.course-name { font-size: 14px; font-weight: 600; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.course-meta { font-size: 12px; color: #909399; margin-top: 4px; display: flex; gap: 12px; }
</style>
