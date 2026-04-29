<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>课程管理中心</span>
          <el-radio-group v-model="activeTab" size="small" v-if="userStore.role === 'admin'">
            <el-radio-button value="list">课程列表</el-radio-button>
            <el-radio-button value="schedule">课表视图</el-radio-button>
            <el-radio-button value="assign">班级排课</el-radio-button>
          </el-radio-group>
          <el-radio-group v-model="activeTab" size="small" v-else-if="userStore.role === 'teacher'">
            <el-radio-button value="list">课程列表</el-radio-button>
            <el-radio-button value="schedule">课表视图</el-radio-button>
          </el-radio-group>
          <el-radio-group v-model="activeTab" size="small" v-else>
            <el-radio-button value="list">课程列表</el-radio-button>
            <el-radio-button value="schedule">课表视图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- ========== Tab1: 课程列表 ========== -->
      <div v-show="activeTab === 'list'">
        <el-form :inline="true" style="margin-bottom:16px;flex-wrap:wrap">
          <el-form-item label="学期">
            <el-select v-model="listQuery.semester" clearable placeholder="全部学期" style="width:180px" @change="fetchList">
              <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="listQuery.courseType" clearable placeholder="全部类型" style="width:110px" @change="fetchList">
              <el-option label="必修课" value="必修" />
              <el-option label="选修课" value="选修" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级" v-if="userStore.role === 'admin'">
            <el-select v-model="listQuery.className" clearable filterable placeholder="按班级筛选" style="width:150px" @change="fetchList" v-loading="classLoading">
              <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="教师" v-if="userStore.role === 'admin'">
            <el-select v-model="listQuery.teacherId" clearable filterable placeholder="按教师筛选" style="width:140px" @change="fetchList">
              <el-option v-for="t in teacherOptions" :key="t.id" :label="t.realName" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="listQuery.keyword" placeholder="课程名/编号" clearable style="width:160px" @keyup.enter="fetchList" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchList">查询</el-button>
            <el-button @click="resetListQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <div style="margin-bottom:10px;display:flex;justify-content:space-between;align-items:center" v-if="userStore.role !== 'student'">
          <div>
            <el-button type="primary" size="small" v-if="userStore.role === 'admin'" @click="showAddDialog">+ 添加课程</el-button>
            <el-button type="warning" size="small" :disabled="selectedIds.length === 0" @click="showBatchEditDialog" v-if="userStore.role === 'admin'">批量编辑 ({{ selectedIds.length }})</el-button>
          </div>
          <span style="color:#909399;font-size:13px">共 {{ listTotal }} 门课程</span>
        </div>

        <el-table :data="courseList" stripe v-loading="listLoading" @selection-change="handleSelectionChange" :row-class-name="tableRowClassName">
          <el-table-column v-if="userStore.role === 'admin'" type="selection" width="45" />
          <el-table-column prop="code" label="编号" width="100" sortable />
          <el-table-column prop="name" label="课程名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="teacherName" label="教师" width="85" />
          <el-table-column label="类型" width="75">
            <template #default="{ row }"><el-tag :type="row.courseType === '选修' ? 'warning' : ''" size="small">{{ row.courseType === '选修' ? '选修' : '必修' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="credit" label="学分" width="60" align="center" />
          <el-table-column label="时间" width="110">
            <template #default="{ row }">{{ '一二三四五六日'[row.weekday - 1] }} {{ row.startSection }}-{{ row.endSection }}节</template>
          </el-table-column>
          <el-table-column prop="locationDisplay" label="教室" width="130" />
          <el-table-column v-if="userStore.role !== 'student'" label="选课" width="80" align="center">
            <template #default="{ row }"><span v-if="row.courseType === '选修'" :style="{ color: row.remainCount != null && row.remainCount <= 5 ? '#f56c6c' : '#606266' }">{{ row.currentCount || 0 }}/{{ row.maxStudents || '-' }}</span><span v-else style="color:#c0c4cc">-</span></template>
          </el-table-column>
          <el-table-column prop="className" label="班级" min-width="120" show-overflow-tooltip v-if="userStore.role === 'admin'" />
          <el-table-column label="操作" width="160" fixed="right" v-if="userStore.role !== 'student'">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
              <el-button size="small" type="warning" link @click="editSingle(row)" v-if="canEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row.id)" v-if="userStore.role === 'admin'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:14px;justify-content:flex-end" v-model:current-page="listQuery.page"
          v-model:page-size="listQuery.size" :total="listTotal" layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]" @size-change="fetchList" @current-change="fetchList" />
      </div>

      <!-- ========== Tab2: 课表视图 ========== -->
      <div v-show="activeTab === 'schedule'">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;flex-wrap:wrap">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
            <el-select v-model="schedClassName" filterable placeholder="选择班级查看课表" style="width:200px"
              @change="onClassSelected" v-loading="classLoading" clearable>
              <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
            </el-select>
            <el-select v-model="schedSemester" placeholder="学期" style="width:180px" @change="onSemesterChange" :disabled="isAdmin.value && !schedClassName">
              <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-radio-group v-model="viewMode" size="small" @change="fetchScheduleData" :disabled="isAdmin.value && !schedClassName">
              <el-radio-button value="week">周视图</el-radio-button>
              <el-radio-button value="day">日视图</el-radio-button>
            </el-radio-group>
            <el-select v-model="selectedDay" v-if="viewMode === 'day'" style="width:100px" @change="fetchScheduleData" :disabled="isAdmin.value && !schedClassName">
              <el-option v-for="d in 7" :key="d" :label="'周'+['一','二','三','四','五','六','日'][d-1]" :value="d" />
            </el-select>
          </div>
          <el-tag type="info" effect="plain">{{ schedClassName || '未选择' }} · 共 {{ scheduleData.length }} 门课</el-tag>
        </div>

        <div v-if="isAdmin.value && !schedClassName" style="text-align:center;padding:40px 20px;color:#c0c4cc;font-size:14px">
          👆 请先选择班级以加载课表
        </div>

        <div v-loading="schedLoading" class="schedule-grid" v-show="isAdmin.value ? schedClassName : true">
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
              <div v-for="d in 7" :key="d" class="day-cell" @drop="onDrop(d, section, $event)" @dragover.prevent @dragenter.prevent>
                <div v-for="item in getItems(d, section)" :key="item.id" class="course-block"
                  :draggable="canDrag(item)" @dragstart="onDragStart(item, $event)"
                  :style="{ background: getColor(item.id), cursor: canDrag(item) ? 'move' : 'default', minHeight: getBlockHeight(item) }"
                  @click="showDetail(item)">
                  <div class="cb-name">{{ item.name }}</div>
                  <div class="cb-sub">{{ item.teacherName }} · {{ item.locationDisplay || item.location }}</div>
                  <div class="cb-time">{{ item.startSection }}-{{ item.endSection }}节</div>
                </div>
              </div>
            </template>
            <div v-else class="day-cell-full" @drop="onDrop(selectedDay, section, $event)" @dragover.prevent @dragenter.prevent>
              <div v-for="item in getItems(selectedDay, section)" :key="item.id" class="course-block-large"
                :draggable="canDrag(item)" @dragstart="onDragStart(item, $event)"
                :style="{ background: getColor(item.id), cursor: canDrag(item) ? 'move' : 'default' }"
                @click="showDetail(item)">
                <div class="cbl-name">{{ item.code }} {{ item.name }}</div>
                <div class="cbl-row"><strong>{{ item.teacherName }}</strong> · {{ item.locationDisplay || item.location }}</div>
                <div class="cbl-row">🕐 第 {{ item.startSection }}-{{ item.endSection }} 节 · {{ item.courseType === '选修' ? '选修' : '必修' }}</div>
                <div class="cbl-row">👥 选课 {{ item.currentCount || 0 }}/{{ item.maxStudents || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="!schedLoading && scheduleData.length === 0 && schedClassName" description="该班级暂无课程数据" />
      </div>

      <!-- ========== Tab3: 班级排课 (仅管理员) ========== -->
      <div v-show="activeTab === 'assign' && userStore.role === 'admin'">
        <el-alert title="为指定班级批量安排课程，该班级所有学生将自动选上所选课程，已有选课记录会自动跳过" type="info" :closable="false" show-icon style="margin-bottom:16px" />
        <el-form :inline="true" label-width="90px">
          <el-form-item label="目标班级" required>
            <el-select v-model="assignForm.className" filterable placeholder="请选择班级" style="width:220px" v-loading="classLoading">
              <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
            </el-select>
          </el-form-item>
          <el-form-item label="排课课程" required>
            <el-select v-model="assignForm.courseIds" multiple collapse-tags collapse-tags-tooltip placeholder="可选择多门课程同时排课" style="width:420px" v-loading="listLoading">
              <el-option-group v-for="type in [{label:'必修课',value:'必修'},{label:'选修课',value:'选修'}]" :key="type.value" :label="type.label">
                <el-option v-for="c in allCourses.filter(c => c.courseType === type.value)" :key="c.id"
                  :label="c.code + ' - ' + c.name + (' (' + c.teacherName + ')')" :value="c.id">
                  <span>{{ c.code }} - {{ c.name }}</span><span style="float:right;color:#999;font-size:12px;margin-left:15px">{{ c.teacherName }} | {{ c.currentCount }}人</span>
                </el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="success" :loading="assignLoading" :disabled="!assignForm.className || assignForm.courseIds.length === 0" @click="handleAssignSubmit">确认排课</el-button>
          </el-form-item>
        </el-form>
        <div style="color:#909399;font-size:13px" v-if="assignForm.courseIds.length > 0">
          已选 <strong style="color:#409EFF">{{ assignForm.courseIds.length }}</strong> 门课程 → 排给 <strong style="color:#67C23A">{{ assignForm.className || '?' }}</strong>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="课程编号" prop="code"><el-input v-model="form.code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="课程名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="form.teacherId" filterable placeholder="选择教师" style="width:100%">
                <el-option v-for="t in teacherOptions" :key="t.id" :label="t.realName + ' (' + t.userNo + ')'" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" prop="courseType">
              <el-radio-group v-model="form.courseType">
                <el-radio value="必修">必修</el-radio>
                <el-radio value="选修">选修</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :max="10" :precision="1" :step="0.5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="学时"><el-input-number v-model="form.hours" :min="0" :step="1" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="最大人数" v-if="form.courseType === '选修'"><el-input-number v-model="form.maxStudents" :min="1" :max="500" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="form.semester" placeholder="选择学期" style="width:100%">
                <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="星期" prop="weekday">
              <el-select v-model="form.weekday" placeholder="选择星期" style="width:100%" clearable @change="onTimeChange">
                <el-option v-for="i in 7" :key="i" :label="'周'+['一','二','三','四','五','六','日'][i-1]" :value="i" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="节次范围" required>
              <div style="display:flex;gap:8px;align-items:center;width:100%">
                <el-input-number v-model="form.startSection" :min="1" :max="12" style="flex:1" controls-position="right" @change="onTimeChange" />
                <span>至</span>
                <el-input-number v-model="form.endSection" :min="1" :max="12" style="flex:1" controls-position="right" @change="onTimeChange" />
                <span>节</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="10"><el-form-item label="上课地点">
              <el-select v-model="form.location" filterable placeholder="选择教室" style="width:100%" :loading="roomsLoading" clearable>
                <el-option v-for="r in availableRooms" :key="r.name" :label="r.name + (r.building ? ' (' + r.building + ')' : '') + ' - 容纳' + r.capacity + '人'" :value="r.name" :disabled="r.occupiedRate > 0">
                  <span>{{ r.name }}</span>
                  <span style="float:right;color:#999;font-size:12px;margin-left:8px">{{ r.building || '' }} · {{ r.capacity }}人</span>
                  <el-tag v-if="r.occupiedRate > 0" type="danger" size="small" style="margin-left:6px">已占用</el-tag>
                  <el-tag v-else type="success" size="small" style="margin-left:6px">空闲</el-tag>
                </el-option>
              </el-select>
              <div v-if="!form.weekday || !form.startSection" style="font-size:12px;color:#909399;margin-top:4px">请先选择上课时间以筛选空闲教室</div>
            </el-form-item></el-col>
        </el-row>
        <el-form-item label="课程描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量编辑对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量编辑课程" width="520px" destroy-on-close>
      <el-alert :title="'已选择 ' + selectedIds.length + ' 门课程进行批量修改，仅填写需要修改的字段即可'" type="info" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="授课教师">
          <el-select v-model="batchForm.teacherId" clearable filterable placeholder="不修改" style="width:100%">
            <el-option v-for="t in teacherOptions" :key="t.id" :label="t.realName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点">
          <el-input v-model="batchForm.location" placeholder="不修改则留空" />
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="batchForm.weekday" clearable placeholder="不修改">
            <el-option v-for="i in 7" :key="i" :label="'周'+['一','二','三','四','五','六','日'][i-1]" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次开始">
          <el-input-number v-model="batchForm.startSection" :min="1" :max="12" placeholder="不修改" />
        </el-form-item>
        <el-form-item label="节次结束">
          <el-input-number v-model="batchForm.endSection" :min="1" :max="12" placeholder="不修改" />
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="batchForm.maxStudents" :min="1" :max="500" placeholder="不修改" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="batchForm.semester" clearable placeholder="不修改" style="width:100%">
            <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchSubmit">批量更新</el-button>
      </template>
    </el-dialog>

    <!-- 课程详情抽屉 -->
    <el-drawer v-model="detailVisible" :title="detailData?.name || '课程详情'" direction="rtl" size="460px" destroy-on-close>
      <template v-if="detailData">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="课程编号">{{ detailData.code }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ detailData.name }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ detailData.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">
            <el-tag :type="detailData.courseType === '选修' ? 'warning' : ''" size="small">{{ detailData.courseType === '选修' ? '选修课' : '必修课' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学分">{{ detailData.credit }}</el-descriptions-item>
          <el-descriptions-item label="学时">{{ detailData.hours }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ formatSemester(detailData.semester) }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ '周' + ['一','二','三','四','五','六','日'][detailData.weekday - 1] }} 第{{ detailData.startSection }}-{{ detailData.endSection }}节</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ detailData.locationDisplay || detailData.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="选课情况">{{ detailData.currentCount || 0 }} / {{ detailData.maxStudents || '不限' }}
            <el-progress v-if="detailData.maxStudents" :percentage="Math.round((detailData.currentCount||0)/detailData.maxStudents*100)" :stroke-width="6" style="margin-top:4px" :status="detailData.remainCount <= 5 ? 'exception' : ''" />
          </el-descriptions-item>
          <el-descriptions-item label="关联班级" v-if="detailData.className">{{ detailData.className || '无' }}</el-descriptions-item>
          <el-descriptions-item label="课程描述" v-if="detailData.description">{{ detailData.description }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:20px;text-align:right" v-if="userStore.role !== 'student'">
          <el-button type="primary" @click="detailVisible = false; editSingle(detailData)">编辑课程</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCourses, addCourse, updateCourse, deleteCourse, listAdminSchedule, listAllClassNames, batchUpdateCourses, getStudentSchedule, getTeacherSchedule, listAvailableClassrooms as listCourseAvailableRooms } from '@/api/course'
import { listClassrooms } from '@/api/classroom'
import { listUsers } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('list')
const currentYear = new Date().getFullYear()
const semesterOptions = [
  { value: `${currentYear - 1}-${currentYear}-1`, label: `${currentYear - 1}-${currentYear} 第一学期` },
  { value: `${currentYear - 1}-${currentYear}-2`, label: `${currentYear - 1}-${currentYear} 第二学期` },
  { value: `${currentYear}-${currentYear + 1}-1`, label: `${currentYear}-${currentYear + 1} 第一学期` },
  { value: `${currentYear}-${currentYear + 1}-2`, label: `${currentYear}-${currentYear + 1} 第二学期` }
]

const colors = ['#409eff','#67c23a','#e6a23c','#f56c6c','#909399','#9b59b6','#1abc9c','#e74c3c','#3498db','#2ecc71']
const getColor = (id) => colors[(id * 31 + 7) % colors.length]
const isAdmin = computed(() => userStore.role === 'admin')
const isTeacher = computed(() => userStore.role === 'teacher')

/* ====== 列表相关 ====== */
const listLoading = ref(false)
const courseList = ref([])
const listTotal = ref(0)
const selectedIds = ref([])
const classLoading = ref(false)
const classOptions = ref([])
const teacherOptions = ref([])
const allCourses = ref([])

const listQuery = reactive({ semester: '', courseType: '', className: '', teacherId: null, keyword: '', page: 1, size: 20 })

const fetchList = async () => {
  listLoading.value = true
  try {
    const api = isAdmin.value ? listAdminSchedule : listCourses
    const params = { ...listQuery }
    if (!isAdmin.value) delete params.className
    if (!isAdmin.value) delete params.teacherId
    if (!isAdmin.value) delete params.courseType
    const res = await api(params)
    courseList.value = res.data.records || []
    listTotal.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { listLoading.value = false }
}

const resetListQuery = () => {
  Object.assign(listQuery, { semester: '', courseType: '', className: '', teacherId: null, keyword: '', page: 1 })
  fetchList()
}

const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

const tableRowClassName = ({ row }) => row.remainCount != null && row.remainCount <= 3 ? 'warning-row' : ''

/* ====== 对话框相关 ====== */
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  id: null, code: '', name: '', teacherId: null, courseType: '必修',
  credit: 3, hours: 48, semester: '', weekday: 1, startSection: 1, endSection: 2,
  location: '', maxStudents: 50, description: ''
})
const dialogTitle = computed(() => isEdit.value ? '编辑课程' : '添加课程')
const formRules = {
  code: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选教师', trigger: 'change' }],
  semester: [{ required: true, message: '请选学期', trigger: 'change' }]
}

const resetForm = () => Object.assign(form, { id: null, code: '', name: '', teacherId: null, courseType: '必修', credit: 3, hours: 48, semester: semesterOptions[2].value, weekday: 1, startSection: 1, endSection: 2, location: '', maxStudents: 50, description: '' })

const availableRooms = ref([])
const roomsLoading = ref(false)

const fetchAvailableRooms = async () => {
  if (!form.weekday || !form.startSection || !form.endSection) {
    availableRooms.value = []
    return
  }
  roomsLoading.value = true
  try {
    const res = await listCourseAvailableRooms({
      weekday: form.weekday,
      startSection: form.startSection,
      endSection: form.endSection,
      semester: form.semester || ''
    })
    availableRooms.value = res.data || []
  } catch (e) {
    availableRooms.value = []
  } finally {
    roomsLoading.value = false
  }
}

const onTimeChange = () => {
  if (form.weekday && form.startSection && form.endSection) {
    fetchAvailableRooms()
    if (!isEdit.value) form.location = ''
  } else {
    availableRooms.value = []
  }
}

watch([() => form.weekday, () => form.startSection, () => form.endSection, () => form.semester], onTimeChange)

const showAddDialog = () => { isEdit.value = false; resetForm(); dialogVisible.value = true; nextTick(() => fetchAvailableRooms()) }

const editSingle = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  if (!form.courseType) form.courseType = '必修'
  dialogVisible.value = true
  nextTick(() => {
    if (form.weekday && form.startSection && form.endSection) {
      fetchAvailableRooms()
    }
  })
}

const handleSubmit = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  submitLoading.value = true
  try {
    if (isEdit.value) await updateCourse(form)
    else await addCourse(form)
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    fetchList(); fetchScheduleData(); loadAllCourses()
  } finally { submitLoading.value = false }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该课程？', '提示', { type: 'warning' })
  await deleteCourse(id)
  ElMessage.success('已删除'); fetchList(); fetchScheduleData(); loadAllCourses()
}

const canEdit = (row) => {
  if (isAdmin.value) return true
  if (isTeacher.value) return Number(row.teacherId) === Number(userStore.userId)
  return false
}

/* ====== 详情抽屉 ====== */
const detailVisible = ref(false)
const detailData = ref(null)

const showDetail = (row) => { detailData.value = { ...row }; detailVisible.value = true }

const formatSemester = (s) => {
  if (!s) return '-'
  const opt = semesterOptions.find(o => o.value === s)
  return opt ? opt.label : s
}

/* ====== 批量编辑 ====== */
const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const batchForm = reactive({ teacherId: null, location: '', weekday: null, startSection: null, endSection: null, maxStudents: null, semester: '' })

const showBatchEditDialog = () => { Object.assign(batchForm, { teacherId: null, location: '', weekday: null, startSection: null, endSection: null, maxStudents: null, semester: '' }); batchDialogVisible.value = true }

const handleBatchSubmit = async () => {
  await ElMessageBox.confirm(`确定批量更新 ${selectedIds.value.length} 门课程？`, '确认', { type: 'info' })
  batchLoading.value = true
  try {
    const dtoList = selectedIds.value.map(id => ({ id, ...batchForm }))
    await batchUpdateCourses(dtoList)
    ElMessage.success('批量更新成功')
    batchDialogVisible.value = false
    selectedIds.value = []
    fetchList(); fetchScheduleData(); loadAllCourses()
  } finally { batchLoading.value = false }
}

/* ====== 课表视图 ====== */
const schedLoading = ref(false)
const schedClassName = ref('')
const schedSemester = ref(semesterOptions[2].value)
const viewMode = ref('week')
const selectedDay = ref(1)
const scheduleData = ref([])
const dragItem = ref(null)
const maxSections = computed(() => viewMode.value === 'week' ? 12 : 12)

const onClassSelected = () => { if (schedClassName.value) fetchScheduleData() }

const onSemesterChange = () => { fetchScheduleData() }

const fetchScheduleData = async () => {
  if (isAdmin.value && !schedClassName.value) return
  schedLoading.value = true
  try {
    let data
    if (isAdmin.value) {
      const res = await listAdminSchedule({ semester: schedSemester.value, className: schedClassName.value, page: 1, size: 200 })
      data = res.data.records || []
    } else if (isTeacher.value) {
      const res = await getTeacherSchedule({ semester: schedSemester.value })
      data = res.data || []
    } else {
      const res = await getStudentSchedule({ semester: schedSemester.value })
      data = res.data || []
    }
    const mapped = data.map(item => ({
      id: item.courseId || item.id,
      code: item.code,
      name: item.courseName || item.name,
      teacherName: item.teacherName,
      location: item.location,
      locationDisplay: item.locationDisplay || '',
      weekday: item.weekday,
      startSection: item.startSection,
      endSection: item.endSection,
      className: item.className,
      courseType: item.courseType,
      credit: item.credit,
      currentCount: item.currentCount || item.studentCount || 0,
      maxStudents: item.maxStudents,
      remainCount: item.remainCount
    }))
    const seen = new Set()
    scheduleData.value = mapped.filter(item => {
      const key = item.id + '_' + item.weekday + '_' + item.startSection + '_' + item.endSection
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })

    const locations = [...new Set(scheduleData.value.map(i => i.location).filter(Boolean))]
    if (locations.length > 0) {
      try {
        const roomRes = await listClassrooms({ page: 1, size: 200 })
        const rooms = (roomRes.data?.records || [])
        const roomMap = {}
        rooms.forEach(r => { roomMap[r.name] = (r.building ? r.building + ' ' : '') + r.name })
        scheduleData.value.forEach(item => {
          if (item.location && !item.locationDisplay) {
            item.locationDisplay = roomMap[item.location] || item.location
          }
        })
      } catch (_) { /* ignore */ }
    }
  } catch (e) { /* ignore */ } finally { schedLoading.value = false }
}

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
  scheduleData.value.filter(s => s.weekday === weekday && section >= s.startSection && section <= s.endSection)

const getBlockHeight = (item) => {
  const span = (item.endSection - item.startSection + 1)
  return Math.max(span * 46, 46) + 'px'
}

const canDrag = (item) => isAdmin.value || (isTeacher.value && item.teacherName && String(item.teacherId) === String(userStore.userId))

const onDragStart = (item, e) => {
  dragItem.value = item
  e.dataTransfer.effectAllowed = 'move'
}

const onDrop = (targetDay, targetSection, e) => {
  if (!dragItem.value || !canDrag(dragItem.value)) return
  const span = dragItem.value.endSection - dragItem.value.startSection + 1
  const newEnd = Math.min(targetSection + span - 1, 12)
  ElMessageBox.confirm(
    `将「${dragItem.value.name}」移动到 周${['一','二','三','四','五','六','日'][targetDay-1]} 第${targetSection}-${newEnd}节？`,
    '调整时间',
    { confirmButtonText: '确认调整', cancelButtonText: '取消', type: 'info' }
  ).then(() => {
    updateCourse({
      id: dragItem.value.id, weekday: targetDay, startSection: targetSection, endSection: newEnd
    }).then(() => { ElMessage.success('时间已调整'); fetchScheduleData(); fetchList() }).catch(() => {})
  }).catch(() => {})
  dragItem.value = null
}

/* ====== 班级排课 ====== */
const assignLoading = ref(false)
const assignForm = reactive({ className: '', courseIds: [] })

const handleAssignSubmit = async () => {
  if (!assignForm.className || assignForm.courseIds.length === 0) return
  await ElMessageBox.confirm(`确定将 ${assignForm.courseIds.length} 门课程排给【${assignForm.className}】吗？`, '确认排课', { type: 'info' })
  assignLoading.value = true
  let successCount = 0, failList = []
  for (const cid of assignForm.courseIds) {
    try {
      const { batchAssignToClass } = await import('@/api/courseSelection')
      const res = await batchAssignToClass(cid, assignForm.className)
      successCount += res.data || 0
    } catch (e) { failList.push(allCourses.value.find(c => c.id === cid)?.name || cid) }
  }
  assignLoading.value = false
  if (failList.length > 0) ElMessage.warning(`完成 ${successCount} 条，失败：${failList.join('、')}`)
  else ElMessage.success(`排课成功！新增 ${successCount} 条选课记录`)
  assignForm.courseIds = []; fetchList(); fetchScheduleData(); loadAllCourses()
}

/* ====== 基础数据加载 ====== */
const loadTeachers = async () => {
  try { const res = await listUsers({ role: 'teacher', page: 1, size: 200 }); teacherOptions.value = res.data.records || [] } catch (_) {}
}
const loadClasses = async () => {
  if (!isAdmin.value) return
  classLoading.value = true
  try { const res = await listAllClassNames(); classOptions.value = res.data || [] } catch (_) {} finally { classLoading.value = false }
}
const loadAllCourses = async () => {
  try { const res = await listAdminSchedule({ page: 1, size: 300 }); allCourses.value = res.data.records || [] } catch (_) {}
}

onMounted(async () => {
  await Promise.all([loadTeachers(), loadClasses(), loadAllCourses()])
  fetchList()
  fetchScheduleData()
})
</script>

<style scoped>
.schedule-grid { width: 100%; border: 1px solid #ebeef5; border-radius: 4px; overflow-x: auto; }
.schedule-header { display: flex; background: #f5f7fa; font-weight: 600; font-size: 13px; }
.schedule-row { display: flex; border-top: 1px solid #ebeef5; min-height: 48px; }
.time-col { width: 72px; min-height: 48px; display: flex; flex-direction: column; align-items: center; justify-content: center; border-right: 1px solid #ebeef5; font-size: 12px; color: #606266; background: #fafafa; flex-shrink: 0; }
.sec-num { font-weight: 600; font-size: 13px; line-height: 1.2; }
.sec-time { font-size: 10px; color: #909399; margin-top: 2px; }
.day-col { flex: 1; min-width: 90px; border-right: 1px solid #ebeef5; padding: 4px 2px; text-align: center; font-size: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.day-col-full { flex: 1; min-width: 280px; border-right: 1px solid #ebeef5; padding: 1px; text-align: center; font-size: 13px; }
.day-cell { flex: 1; min-width: 90px; border-right: 1px solid #ebeef5; padding: 1px; min-height: 48px; position: relative; transition: background 0.15s; }
.day-cell:hover { background: #f0f9ff; }
.day-cell-full { flex: 1; min-width: 280px; border-right: 1px solid #ebeef5; padding: 3px; min-height: 60px; position: relative; }
.course-block { padding: 4px 6px; border-radius: 4px; color: #fff; font-size: 11px; margin: 1px; cursor: pointer; transition: transform 0.1s, box-shadow 0.1s; line-height: 1.4; }
.course-block:hover { transform: scale(1.02); box-shadow: 0 2px 8px rgba(0,0,0,0.18); z-index: 2; position: relative; }
.cb-name { font-weight: bold; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cb-sub { opacity: 0.88; font-size: 10px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cb-extra { opacity: 0.75; font-size: 10px; margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cb-time { opacity: 0.9; font-size: 10px; margin-top: 1px; font-weight: 500; }
.course-block-large { padding: 8px 12px; border-radius: 6px; color: #fff; font-size: 12px; margin: 2px; cursor: pointer; transition: transform 0.1s; line-height: 1.6; }
.course-block-large:hover { transform: translateY(-1px); box-shadow: 0 3px 12px rgba(0,0,0,0.2); }
.cbl-name { font-weight: bold; font-size: 14px; }
.cbl-row { opacity: 0.92; font-size: 12px; margin-top: 2px; }
:deep(.warning-row) { --el-table-tr-bg-color: #fdf6ec !important; }
</style>
