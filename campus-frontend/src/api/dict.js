import request from '@/utils/request'

export function getDictData(typeCode) {
  return request.get(`/api/dict/${typeCode}`)
}
