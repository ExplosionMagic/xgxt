import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import StudentList from '../views/StudentList.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        redirect: '/login' // 默认跳转到登录
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        // 临时写一个简单的组件显示，防止跳转报错
        component: { template: '<h1>登录成功！这里是主页</h1>' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router