import request from '../utils/request'

export function getCourseList() {
    return request.get('/course/list')
}

export function saveCourse(data) {
    return request.post('/course', data)
}

export function deleteCourse(id) {
    return request.delete(`/course/${id}`)
}