import request from '@/utils/request'

export function listHomework(params) {
  return request.get('/api/homework/list', { params })
}

export function listStudentHomework(params) {
  return request.get('/api/homework/student', { params })
}

export function getHomeworkDetail(id) {
  return request.get(`/api/homework/${id}`)
}

export function addHomework(data) {
  return request.post('/api/homework', data)
}

export function updateHomework(data) {
  return request.put('/api/homework', data)
}

export function deleteHomework(id) {
  return request.delete(`/api/homework/${id}`)
}

export function submitHomework(id, content, attachmentId) {
  return request.post(`/api/homework/${id}/submit`, { content, attachmentId })
}

export function listSubmissions(homeworkId, params) {
  return request.get(`/api/homework/${homeworkId}/submissions`, { params })
}

export function gradeSubmission(data) {
  return request.post('/api/homework/grade', data)
}

export function returnSubmission(data) {
  return request.post('/api/homework/return', data)
}

export function getGradeDetail(id) {
  return request.get(`/api/homework/${id}/grade-detail`)
}

export function listHomeworkStudents(id) {
  return request.get(`/api/homework/${id}/students`)
}
