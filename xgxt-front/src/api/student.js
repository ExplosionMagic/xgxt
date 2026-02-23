import request from '../utils/request'

export function getStudentList() {
    return request.get('/student/list')
}