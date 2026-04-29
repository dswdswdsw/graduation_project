import request from '@/utils/request'

export function getStudentDashboard() {
  return request.get('/api/dashboard/student', { showError: false })
}

export function getTeacherDashboard() {
  return request.get('/api/dashboard/teacher', { showError: false })
}

export function getAdminDashboard() {
  return request.get('/api/dashboard/admin', { showError: false })
}

export function getTodayCourses() {
  return request.get('/api/dashboard/today-courses', { showError: false })
}
