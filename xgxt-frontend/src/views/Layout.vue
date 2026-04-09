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
          unique-opened
          style="border-right: none;"
          :default-active="$route.path"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <span>系统首页</span>
        </el-menu-item>

<!--        <el-menu-item index="/dashboard" v-if="user.role !== 'STUDENT'">-->
<!--          <el-icon><PieChart /></el-icon>-->
<!--          <span>数据大屏</span>-->
<!--        </el-menu-item>-->

        <el-menu-item index="/announcement-manage" v-if="user.role === 'ADMIN'">
          <el-icon><Bell /></el-icon>
          <span>公告发布</span>
        </el-menu-item>

        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

        <template v-if="user.role === 'STUDENT'">

          <el-menu-item index="/courseselection">
            <el-icon><Grid /></el-icon>
            <span>我的选课</span>
          </el-menu-item>

          <el-menu-item index="/score">
            <el-icon><DataLine /></el-icon>
            <span>成绩查询</span>
          </el-menu-item>

          <el-sub-menu index="self">
            <template #title>
              <el-icon><Tickets /></el-icon>
              <span>自主申请</span>
            </template>
            <el-menu-item index="/leave-apply">
              <el-icon><Calendar /></el-icon>
              <span>请假申请</span>
            </el-menu-item>
            <el-menu-item index="/aid-apply">
              <el-icon><Money /></el-icon>
              <span>资助申请</span>
            </el-menu-item>
          </el-sub-menu>




          <el-sub-menu index="student_reward_punish">
            <template #title>
              <el-icon><Medal /></el-icon>
              <span>奖惩中心</span>
            </template>
            <el-menu-item index="/honor-apply">荣誉奖项申请</el-menu-item>
            <el-menu-item index="/discipline">违纪与处分记录</el-menu-item>
          </el-sub-menu>

        </template>

        <template v-if="user.role !== 'STUDENT'">

          <el-menu-item index="/student-audit">
            <el-icon><Stamp /></el-icon>
            <span>新生审核</span>
          </el-menu-item>
          
          <el-menu-item index="/leave-approval">
            <el-icon><List /></el-icon>
            <span>请假审批管理</span>
          </el-menu-item>

          <el-sub-menu index="org_manage" v-if="user.role === 'ADMIN'">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>组织架构管理</span>
            </template>
            <el-menu-item index="/college"><el-icon><School /></el-icon>学院管理</el-menu-item>
            <el-menu-item index="/major"><el-icon><Collection /></el-icon>专业管理</el-menu-item>
            <el-menu-item index="/grade"><el-icon><Flag /></el-icon>年级管理</el-menu-item>
            <el-menu-item index="/class"><el-icon><OfficeBuilding /></el-icon>班级管理</el-menu-item>
          </el-sub-menu>


          <el-sub-menu index="personnel_manage" v-if="user.role === 'ADMIN'">
            <template #title>
              <el-icon><Avatar /></el-icon>
              <span>人员信息管理</span>
            </template>
            <el-menu-item index="/student"><el-icon><Avatar /></el-icon>学生管理</el-menu-item>
            <el-menu-item index="/user"><el-icon><Setting /></el-icon>用户管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="academic_manage">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>教学事务管理</span>
            </template>
            <el-menu-item index="/course" v-if="user.role === 'ADMIN'">
              <el-icon><Reading /></el-icon><span>课程管理</span>
            </el-menu-item>
            <el-menu-item index="/score">
              <el-icon><DataLine /></el-icon><span>成绩管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="aid_manage">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>资助工作管理</span>
            </template>
            <el-menu-item index="/aid-approval">资助待办审批</el-menu-item>
            <el-menu-item index="/aid-record">资助申请记录</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="reward_punish_manage">
            <template #title>
              <el-icon><Trophy /></el-icon>
              <span>奖惩工作管理</span>
            </template>
            <el-menu-item index="/honor-approval">荣誉奖项审批</el-menu-item>
            <el-menu-item index="/honor-record">荣誉申报记录</el-menu-item>
            <el-menu-item index="/discipline">
              <span v-if="user.role === 'TEACHER'">违纪行为上报</span>
              <span v-else>违纪行为审批</span>
            </el-menu-item>
          </el-sub-menu>

        </template>

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
import { useRouter, useRoute } from 'vue-router'
import {
  House, Reading, Tickets, Warning,
  OfficeBuilding, User, Collection,
  Stamp, FolderOpened, Bell, Setting,
  PieChart, Grid, Calendar, List, DataLine,
  Avatar, School, Flag, Checked, Money, Document, Medal, Trophy
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = ref(JSON.parse(localStorage.getItem('student-user') || '{}'))

// 面包屑名称字典
const getBreadcrumbName = (path) => {
  const map = {
    '/dashboard': '数据大屏',
    '/profile': '个人中心',
    '/announcement-manage': '公告发布',
    '/courseselection': '我的选课',
    '/timetable': '我的课表',
    '/leave-apply': '请假申请',
    '/leave-approval': '请假审批',
    '/score': user.value.role === 'STUDENT' ? '成绩查询' : '成绩管理',
    '/student': '学生管理',
    '/college': '学院管理',
    '/major': '专业管理',
    '/grade': '年级管理',
    '/class': '班级管理',
    '/course': '课程管理',
    '/elective': '选修审核',
    '/student-audit': '新生审核',
    '/aid-apply': '资助申请与进度',
    '/aid-approval': '资助待办审批',
    '/aid-record': '资助申请记录',
    '/honor-apply': '荣誉奖项申请',
    '/honor-approval': '荣誉奖项审批',
    '/honor-record': '荣誉申报记录',
    '/discipline': user.value.role === 'STUDENT' ? '违纪与处分记录' : (user.value.role === 'TEACHER' ? '违纪上报' : '处分审批记录'),
    '/user': '用户管理'
  }
  return map[path] || '详情'
}

const logout = () => {
  localStorage.removeItem('student-user')
  router.push('/login')
}
</script>