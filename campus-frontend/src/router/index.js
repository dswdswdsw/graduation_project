import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/CourseList.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'course-selection',
        name: 'CourseSelection',
        component: () => import('@/views/CourseSelection.vue'),
        meta: { title: '在线选课' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/Schedule.vue'),
        meta: { title: '我的课表' }
      },
      {
        path: 'teacher-courses',
        name: 'TeacherCourse',
        component: () => import('@/views/TeacherCourse.vue'),
        meta: { title: '我的课程', roles: ['teacher'] }
      },
      {
        path: 'score',
        name: 'Score',
        component: () => import('@/views/ScoreList.vue'),
        meta: { title: '成绩查询' }
      },
      {
        path: 'score-entry',
        name: 'ScoreEntry',
        component: () => import('@/views/ScoreEntry.vue'),
        meta: { title: '成绩录入', roles: ['teacher'] }
      },
      {
        path: 'homework',
        name: 'Homework',
        component: () => import('@/views/HomeworkList.vue'),
        meta: { title: '作业管理' }
      },
      {
        path: 'homework/grade/:id',
        name: 'HomeworkGrade',
        component: () => import('@/views/HomeworkGrade.vue'),
        meta: { title: '作业批改' }
      },
      {
        path: 'homework/grade/:homeworkId/:submissionId',
        name: 'HomeworkGradeDetail',
        component: () => import('@/views/HomeworkGradeDetail.vue'),
        meta: { title: '批改作业' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/RepairList.vue'),
        meta: { title: '设备报修' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/NoticeList.vue'),
        meta: { title: '通知公告' }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        path: 'classroom',
        name: 'Classroom',
        component: () => import('@/views/ClassroomList.vue'),
        meta: { title: '教室管理', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧校园服务平台` : '智慧校园服务平台'
  const token = sessionStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
