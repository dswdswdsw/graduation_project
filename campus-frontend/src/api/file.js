import request from '@/utils/request'

export function uploadFile(formData) {
  return request.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getFileDetail(id) {
  return request.get(`/api/file/${id}`)
}

export function deleteFile(id) {
  return request.delete(`/api/file/${id}`)
}

export function getDownloadUrl(id) {
  return `/api/file/download/${id}`
}
