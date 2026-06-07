import request from './request'

export function listKnowledge() {
  return request.get('/knowledge/list')
}

export function getKnowledgeDetail(id) {
  return request.get('/knowledge/detail', { params: { id } })
}

export function addKnowledge(data) {
  return request.post('/knowledge/add', data)
}

export function updateKnowledge(data) {
  return request.put('/knowledge/update', data)
}

export function deleteKnowledge(id) {
  return request.delete('/knowledge/delete', { params: { id } })
}
