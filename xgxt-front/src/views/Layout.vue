<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <div style="height: 60px; color: white; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold;">
        高校管理系统
      </div>
      <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#fff"
          router
          :default-active="$route.path"
      >
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
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

        <el-menu-item index="/user" v-if="user.role === 'ADMIN'">
          <el-icon><Setting /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/major" v-if="user.role === 'ADMIN'">
          <el-icon><Collection /></el-icon>
          <span>专业管理</span>
        </el-menu-item>

        <el-menu-item index="/course" v-if="user.role === 'ADMIN'">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>

        <el-menu-item index="/class" v-if="user.role === 'ADMIN'">
          <el-icon><OfficeBuilding /></el-icon>
          <span>班级管理</span>
        </el-menu-item>

        <el-menu-item index="/elective" v-if="user.role === 'ADMIN' || user.role === 'TEACHER'">
          <el-icon><Checked /></el-icon>
          <span>选修审核</span>
        </el-menu-item>

        <el-menu-item index="/student-audit" v-if="user.role === 'ADMIN'">
          <el-icon><Stamp /></el-icon>
          <span>新生审核</span>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="border-bottom: 1px solid #ccc; display: flex; justify-content: space-between; align-items: center;">
        <div>欢迎你，{{ user.name }} ({{ user.userNo }})</div>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// 从本地存储获取当前登录的用户信息
const user = ref(JSON.parse(localStorage.getItem('student-user') || '{}'))

const logout = () => {
  localStorage.removeItem('student-user')
  router.push('/login')
}
</script>