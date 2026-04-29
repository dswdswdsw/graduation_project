import request from '@/utils/request'

export function listStudentScores(params) {
  return request.get('/api/score/student', { params })
}

export function listCourseScores(courseId, params) {
  return request.get(`/api/score/course/${courseId}`, { params })
}

export function listCourseStudentsForEntry(courseId) {
  return request.get(`/api/score/course/${courseId}/students`)
}

export function addScore(data) {
  return request.post('/api/score', data)
}

export function updateScore(data) {
  return request.put('/api/score', data)
}

export function getStudentStatistics(params) {
  return request.get('/api/score/student/statistics', { params })
}

export function getCourseStatistics(courseId, params) {
  return request.get(`/api/score/course/${courseId}/statistics`, { params })
}
