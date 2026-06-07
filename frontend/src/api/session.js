import request from './request'

export function listSessions() {
  return request.get('/session/list')
}

export function getHistory(sessionId) {
  return request.get('/session/history', { params: { sessionId } })
}
