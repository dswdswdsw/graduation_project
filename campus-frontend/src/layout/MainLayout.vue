<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-show="!isCollapse">智慧校园</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/schedule" v-if="userStore.role === 'student'">
          <el-icon><Calendar /></el-icon>
          <template #title>我的课表</template>
        </el-menu-item>
        <el-menu-item index="/teacher-courses" v-if="userStore.role === 'teacher'">
          <el-icon><Reading /></el-icon>
          <template #title>我的课程</template>
        </el-menu-item>
        <el-menu-item index="/course-selection" v-if="userStore.role === 'student'">
          <el-icon><Select /></el-icon>
          <template #title>在线选课</template>
        </el-menu-item>
        <el-menu-item index="/score" v-if="userStore.role === 'student'">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>成绩查询</template>
        </el-menu-item>
        <el-menu-item index="/score-entry" v-if="userStore.role === 'teacher'">
          <el-icon><EditPen /></el-icon>
          <template #title>成绩录入</template>
        </el-menu-item>
        <el-menu-item index="/homework" v-if="userStore.role === 'student' || userStore.role === 'teacher'">
          <el-icon><Document /></el-icon>
          <template #title>作业管理</template>
        </el-menu-item>
        <el-menu-item index="/repair">
          <el-icon><SetUp /></el-icon>
          <template #title>设备报修</template>
        </el-menu-item>
        <el-menu-item index="/notice">
          <el-icon><Bell /></el-icon>
          <template #title>通知公告</template>
        </el-menu-item>
        <el-menu-item index="/course" v-if="userStore.role === 'admin'">
          <el-icon><Reading /></el-icon>
          <template #title>课程管理</template>
        </el-menu-item>
        <el-menu-item index="/user-manage" v-if="userStore.role === 'admin'">
          <el-icon><UserFilled /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/classroom" v-if="userStore.role === 'admin'">
          <el-icon><School /></el-icon>
          <template #title>教室管理</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="userStore.role === 'admin' || unreadCount === 0" class="badge-item">
            <el-icon class="notice-icon" @click="router.push('/notice')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.realName || userStore.username }}
              <el-tag size="small" type="info" style="margin-left:6px">{{ roleLabel }}</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { getUnreadCount } from '@/api/notice'
import eventBus from '@/utils/eventBus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const unreadCount = ref(0)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/homework')) return '/homework'
  return path
})

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userStore.role] || userStore.role
})

const fetchUnread = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  fetchUnread()
  eventBus.on('notice:read', fetchUnread)
})

onBeforeUnmount(() => {
  eventBus.off('notice:read', fetchUnread)
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background-color: #304156;
  overflow: hidden;
  border-right: none;
}

.aside :deep(.el-menu) {
  border-right: none;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.2s ease;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  z-index: 10;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.collapse-btn:hover { color: #409eff; }

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notice-icon {
  font-size: 20px;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.main {
  background: #f0f2f5;
  position: relative;
  overflow-y: auto;
}
</style>
