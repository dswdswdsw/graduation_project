import request from '@/utils/request'

export function selectCourse(courseId) {
  return request.post(`/api/course-selection/select/${courseId}`)
}

export function dropCourse(courseId) {
  return request.post(`/api/course-selection/drop/${courseId}`)
}

export function listMySelections(params) {
  return request.get('/api/course-selection/my', { params })
}

export function listAllClasses() {
  return request.get('/api/course-selection/admin/classes')
}

export function batchAssignToClass(courseId, className) {
  return request.post('/api/course-selection/admin/assign', null, { params: { courseId, className } })
}
