import request from '@/utils/request'

export function listMyRepairs(params) {
  return request.get('/api/repair/my', { params })
}

export function getRepairDetail(id) {
  return request.get(`/api/repair/${id}`)
}

export function addRepair(data) {
  return request.post('/api/repair', data)
}

export function handleRepair(id, result) {
  return request.post(`/api/repair/${id}/handle`, { result })
}

export function rateRepair(id, data) {
  return request.post(`/api/repair/${id}/rate`, data)
}

export function listAllRepairs(params) {
  return request.get('/api/repair/all', { params })
}
