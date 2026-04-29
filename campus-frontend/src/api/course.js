import request from '@/utils/request'

export function listCourses(params) {
  return request.get('/api/course/list', { params })
}

export function getCourseDetail(id) {
  return request.get(`/api/course/${id}`)
}

export function addCourse(data) {
  return request.post('/api/course', data)
}

export function updateCourse(data) {
  return request.put('/api/course', data)
}

export function deleteCourse(id) {
  return request.delete(`/api/course/${id}`)
}

export function getStudentSchedule(params) {
  return request.get('/api/course/schedule/student', { params })
}

export function getTeacherSchedule(params) {
  return request.get('/api/course/schedule/teacher', { params })
}

export function listAdminSchedule(params) {
  return request.get('/api/course/admin/schedule', { params })
}

export function listAllClassNames() {
  return request.get('/api/course/admin/classes')
}

export function batchUpdateCourses(data) {
  return request.put('/api/course/batch', data)
}

export function listAvailableClassrooms(params) {
  return request.get('/api/course/available-classrooms', { params })
}

export function checkCourseConflict(data) {
  return request.post('/api/course/check-conflict', data)
}
