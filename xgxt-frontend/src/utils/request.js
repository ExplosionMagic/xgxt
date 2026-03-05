import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080/api', // 后端地址
    timeout: 5000
})

// 请求拦截器：添加Token
request.interceptors.request.use(config => {
    const user = JSON.parse(localStorage.getItem('student-user') || '{}')
    if (user.token) {
        config.headers['Authorization'] = user.token
    }
    return config
})

// 响应拦截器
request.interceptors.response.use(res => {
    if (res.data.code === 200) {
        return res.data
    } else {
        ElMessage.error(res.data.msg || '网络异常')
        return Promise.reject(res.data)
    }
}, err => {
    ElMessage.error('请求失败')
    return Promise.reject(err)
})

export default request