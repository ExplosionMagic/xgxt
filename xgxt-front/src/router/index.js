import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    // 新增注册路由
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
    },

    {
        path: '/',
        name: 'Layout',
        component: Layout, // 主布局
        redirect: '/student', // 登录后默认跳转到学生管理
        children: [
            {
                path: 'student', // 注意这里不能有斜杠
                name: 'StudentList',
                component: () => import('../views/StudentList.vue')
            },
            {
                path: 'course',
                name: 'CourseList',
                component: () => import('../views/CourseList.vue')
            },
            {
                path: 'score',
                name: 'ScoreList',
                component: () => import('../views/ScoreList.vue')
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue')
            },
            {
                path: 'user',
                name: 'UserList',
                component: () => import('../views/UserList.vue')
            },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router