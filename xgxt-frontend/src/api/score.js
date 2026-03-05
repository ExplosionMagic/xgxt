import request from '../utils/request'

export function getScoreList() {
    return request.get('/score/list')
}

export function saveScore(data) {
    return request.post('/score', data)
}

export function deleteScore(id) {
    return request.delete(`/score/${id}`)
}