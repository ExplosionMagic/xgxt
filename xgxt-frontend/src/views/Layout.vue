<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <div style="height: 60px; color: white; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold;">
        学生工作管理系统
      </div>
      <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#fff"
          router
          :default-active="$route.path"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <span>系统首页</span>
        </el-menu-item>

        <el-menu-item index="/announcement-manage" v-if="user.role === 'ADMIN'">
          <el-icon><Bell /></el-icon>
          <span>公告发布</span>
        </el-menu-item>

        <el-menu-item index="/dashboard" v-if="user.role !== 'STUDENT'">
          <el-icon><PieChart /></el-icon>
          <span>数据大屏</span>
        </el-menu-item>

        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

<!--        <el-menu-item index="/timetable" v-if="user.role === 'STUDENT' || user.role === 'TEACHER'">-->
<!--          <el-icon><Grid /></el-icon>-->
<!--          <span>我的课表</span>-->
<!--        </el-menu-item>-->

        <el-menu-item index="/courseselection" v-if="user.role === 'STUDENT'">
          <el-icon><Grid /></el-icon>
          <span>我的选课</span>
        </el-menu-item>

        <el-menu-item index="/leave-apply" v-if="user.role === 'STUDENT'">
          <el-icon><Calendar /></el-icon>
          <span>请假申请</span>
        </el-menu-item>

        <el-menu-item index="/leave-approval" v-if="user.role !== 'STUDENT'">
          <el-icon><List /></el-icon>
          <span>请假审批</span>
        </el-menu-item>

        <el-menu-item index="/score" v-if="user.role === 'STUDENT'">
          <el-icon><DataLine /></el-icon>
          <span>成绩查询</span>
        </el-menu-item>

        <el-menu-item index="/score" v-if="user.role !== 'STUDENT'">
          <el-icon><DataLine /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>

        <el-menu-item index="/student" v-if="user.role === 'ADMIN'">
          <el-icon><Avatar /></el-icon>
          <span>学生管理</span>
        </el-menu-item>

        <el-menu-item index="/college" v-if="user.role === 'ADMIN'">
          <el-icon><School /></el-icon>
          <span>学院管理</span>
        </el-menu-item>

        <el-menu-item index="/major" v-if="user.role === 'ADMIN'">
          <el-icon><Collection /></el-icon>
          <span>专业管理</span>
        </el-menu-item>

        <el-menu-item index="/grade" v-if="user.role === 'ADMIN'">
          <el-icon><Flag /></el-icon>
          <span>年级管理</span>
        </el-menu-item>

        <el-menu-item index="/class" v-if="user.role === 'ADMIN'">
          <el-icon><OfficeBuilding /></el-icon>
          <span>班级管理</span>
        </el-menu-item>

        <el-menu-item index="/course" v-if="user.role === 'ADMIN'">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>

<!--        <el-menu-item index="/elective" v-if="user.role === 'ADMIN' || user.role === 'TEACHER'">-->
<!--          <el-icon><Checked /></el-icon>-->
<!--          <span>选修审核</span>-->
<!--        </el-menu-item>-->

        <el-menu-item index="/student-audit" v-if="user.role === 'ADMIN'">
          <el-icon><Stamp /></el-icon>
          <span>新生审核</span>
        </el-menu-item>

        <el-menu-item index="/aid-apply" v-if="user.role === 'STUDENT'">
          <el-icon><Money /></el-icon>
          <span>资助申请</span>
        </el-menu-item>

        <el-menu-item index="/aid-approval" v-if="user.role !== 'STUDENT'">
          <el-icon><Money /></el-icon>
          <span>资助审批</span>
        </el-menu-item>

        <el-menu-item index="/aid-record" v-if="user.role !== 'STUDENT'">
          <el-icon><Document /></el-icon>
          <span>资助申请记录</span>
        </el-menu-item>

        <el-menu-item index="/honor-apply" v-if="user.role === 'STUDENT'">
          <el-icon><Medal /></el-icon>
          <span>荣誉奖励申请</span>
        </el-menu-item>

        <el-menu-item index="/honor-approval" v-if="user.role !== 'STUDENT'">
          <el-icon><Medal /></el-icon>
          <span>荣誉待办审批</span>

        </el-menu-item>
        <el-menu-item index="/honor-record" v-if="user.role !== 'STUDENT'">
          <el-icon><Trophy /></el-icon>
          <span>荣誉申报档案</span>
        </el-menu-item>

        <el-menu-item index="/user" v-if="user.role === 'ADMIN'">
          <el-icon><Setting /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/discipline">
          <el-icon><Warning /></el-icon>
          <span v-if="user.role === 'STUDENT'">违纪与处分</span>
          <span v-else-if="user.role === 'TEACHER'">违纪上报</span>
          <span v-else>处分审批档案</span>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; background-color: #fff;">

        <div style="display: flex; align-items: center;">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/home' && route.path !== '/'">
              {{ getBreadcrumbName(route.path) }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div style="display: flex; align-items: center; gap: 15px;">
          <span style="font-size: 14px; color: #606266;">欢迎你，{{ user.name }} ({{ user.userNo }})</span>
          <el-button type="danger" size="small" plain @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main style="background-color: #f0f2f5;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router' // 【修改点 2】：引入 useRoute
// 请确保这些图标都按需引入了
import { House, PieChart, User, Grid, Calendar, List, Bell, DataLine, Avatar, Collection, OfficeBuilding, Reading, Checked, Stamp, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute() // 获取当前激活的路由信息
const user = ref(JSON.parse(localStorage.getItem('student-user') || '{}'))

// 【修改点 3】：创建一个简单的映射字典，把路径翻译成中文菜单名
const getBreadcrumbName = (path) => {
  const map = {
    '/dashboard': '数据大屏',
    '/profile': '个人中心',
    '/score': user.value.role === 'STUDENT' ? '成绩查询' : '成绩管理',
    '/student': '学生管理',
    '/major': '专业管理',
    '/class': '班级管理',
    '/course': '课程管理',
    '/elective': '选修审核',
    '/student-audit': '新生审核',
    '/user': '用户管理',
    '/leave-apply': '请假申请',
    '/leave-approval': '请假审批',
    '/announcement-manage': '公告发布',
    '/timetable': '我的课表'
  }
  return map[path] || '详情'
}

const logout = () => {
  localStorage.removeItem('student-user')
  router.push('/login')
}
</script>