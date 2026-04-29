import request from '@/utils/request'

export function listClassrooms(params) {
  return request.get('/api/classroom/list', { params })
}

export function listAvailableClassrooms(params) {
  return request.get('/api/classroom/available', { params })
}

export function addClassroom(data) {
  return request.post('/api/classroom', data)
}

export function updateClassroom(data) {
  return request.put('/api/classroom', data)
}

export function deleteClassroom(id) {
  return request.delete(`/api/classroom/${id}`)
}

export function listAllBuildings() {
  return request.get('/api/classroom/buildings')
}
