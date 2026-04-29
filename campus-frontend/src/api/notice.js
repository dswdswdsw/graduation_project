import request from '@/utils/request'

export function listNotices(params) {
  return request.get('/api/notice/list', { params })
}

export function getNoticeDetail(id) {
  return request.get(`/api/notice/${id}`)
}

export function addNotice(data) {
  return request.post('/api/notice', data)
}

export function updateNotice(data) {
  return request.put('/api/notice', data)
}

export function deleteNotice(id) {
  return request.delete(`/api/notice/${id}`)
}

export function markAsRead(id) {
  return request.post(`/api/notice/${id}/read`)
}

export function getUnreadCount() {
  return request.get('/api/notice/unread-count')
}
