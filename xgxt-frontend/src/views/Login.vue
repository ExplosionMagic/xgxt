<template>
  <div class="login-container">
    <el-card class="box-card">
      <h2>高校学生工作管理系统</h2>
      <el-form :model="form" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="form.account" prefix-icon="User" placeholder="学号 / 教师员工ID / 手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" prefix-icon="Lock" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" class="btn" @click="handleLogin">登 录</el-button>
        <div style="text-align: right; margin-top: 15px;">
          <el-link type="primary" @click="$router.push('/register')">学生首次使用？去注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
// 注意：这里将 username 改为了 account，与后端 LoginController 保持一致
const form = reactive({ account: '', password: '' })

const handleLogin = () => {
  if (!form.account || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  request.post('/login', form).then(res => {
    // res.data 现在包含了 token, role, name, userNo
    localStorage.setItem('student-user', JSON.stringify(res.data))
    ElMessage.success('登录成功，欢迎 ' + res.data.name)
    router.push('/')
  })
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f2f5; }
.box-card { width: 400px; text-align: center; }
.btn { width: 100%; margin-top: 10px; }
</style>