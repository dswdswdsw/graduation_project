import request from '@/utils/request'

export function getUserInfo() {
  return request.get('/api/user/info')
}

export function updateUserInfo(data) {
  return request.put('/api/user/info', data)
}

export function changePassword(data) {
  return request.put('/api/user/password', data)
}

export function listUsers(params) {
  return request.get('/api/user/list', { params })
}

export function updateUser(id, data) {
  return request.put(`/api/user/${id}`, data)
}

export function resetPassword(id) {
  return request.put(`/api/user/${id}/reset-password`)
}
