<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #545c64">
      <h3 style="color:white; text-align:center; padding: 20px 0;">学生工作系统</h3>
      <el-menu active-text-color="#ffd04b" background-color="#545c64" text-color="#fff" router default-active="/student">

        <el-menu-item index="/student">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>

        <el-menu-item index="/course">
          <el-icon><Reading /></el-icon> <!-- 需要在 main.js 注册图标，或使用其他图标 -->
          <span>课程管理</span>
        </el-menu-item>

        <el-menu-item index="/score">
          <el-icon><DataLine /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>
		
        <el-menu-item index="/login" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
const logout = () => {
  localStorage.removeItem('student-user')
}
</script>