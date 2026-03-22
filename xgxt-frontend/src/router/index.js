import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'

const routes = [
    // 用户登录
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    // 用户注册
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
    },

    {
        path: '/',
        name: 'Layout',
        component: Layout, // 主布局
        redirect: '/profile', // 登录后默认跳转到学生管理
        children: [

            //
            {
                path: 'student',
                name: 'StudentList',
                component: () => import('../views/StudentList.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            //
            {
                path: 'course',
                name: 'CourseList',
                component: () => import('../views/CourseList.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            //
            {
                path: 'score',
                name: 'ScoreList',
                component: () => import('../views/ScoreList.vue'),
            },

            //
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue'),
            },

            //
            {
                path: 'user',
                name: 'UserList',
                component: () => import('../views/UserList.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            //
            {
                path: 'major',
                name: 'MajorList',
                component: () => import('../views/MajorList.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            //
            {
                path: 'class',
                name: 'ClassList',
                component: () => import('../views/ClassList.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            // 选课审核（已弃用）
            // {
            //     path: 'elective',
            //     name: 'ElectiveApproval',
            //     component: () => import('../views/ElectiveApproval.vue'),
            //     meta: { requireRoles: ['ADMIN', 'TEACHER'] }
            // },

            //
            {
                path: 'student-audit',
                name: 'StudentAudit',
                component: () => import('../views/StudentAudit.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            // 首页
            {
                path: 'home',
                name: 'Home',
                component: () => import('../views/Home.vue'),
            },

            // 学生端：请假申请页
            {
                path: '/leave-apply',
                name: 'LeaveApply',
                component: () => import('../views/LeaveApply.vue') // 请确保这里的路径和你的文件名完全一致
            },

            // 教职工/管理员端：请假审批页
            {
                path: '/leave-approval',
                name: 'LeaveApproval',
                component: () => import('../views/LeaveApproval.vue'),
                meta: { requireRoles: ['ADMIN', 'TEACHER'] }
            },

            // 管理员端：公告发布页
            {
                path: '/announcement-manage',
                name: 'AnnouncementManage',
                component: () => import('../views/AnnouncementManage.vue'),
                meta: { requireRoles: ['ADMIN'] }
            },

            // 课表页
            {
                path: '/timetable',
                name: 'Timetable',
                component: () => import('../views/Timetable.vue'),
            },

            // 选课
            {
                path: '/courseselection',
                name: 'CourseSelection',
                component: () => import('../views/CourseSelection.vue'),
            },

            // 数据大屏
            {
                path: '/dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue'),
            },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// ================= 全局前置路由守卫 =================
router.beforeEach((to, from, next) => {
    // 1. 白名单配置：访问登录或注册页，直接放行，绝不拦截
    if (to.path === '/login' || to.path === '/register') {
        return next()
    }

    // 2. 获取当前登录用户信息
    const userStr = localStorage.getItem('student-user')
    let user = null
    try {
        user = userStr ? JSON.parse(userStr) : null
    } catch (e) {
        user = null
    }

    // 3. 全局登录拦截：如果没登录（且去的不是白名单页面），一律踢回登录页
    if (!user || !user.role) {
        ElMessage.error('请先登录系统！')
        return next('/login')
    }

    // 4. 精准权限校验：如果目标页面带有 requireRoles 标签
    if (to.meta && to.meta.requireRoles) {
        if (!to.meta.requireRoles.includes(user.role)) {
            ElMessage.error('您没有权限访问该页面！')
            // 踢回个人中心，防止踢回 '/' 导致死循环白屏
            return next('/profile')
        }
    }

    // 5. 校验全部通过，正常放行
    next()
})

export default router
