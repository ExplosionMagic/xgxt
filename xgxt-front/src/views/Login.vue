<template>
  <div class="login-container">
    <el-card class="box-card">
      <h2>高校学生管理系统</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="btn" @click="handleLogin">登录</el-button>
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
const form = reactive({ username: 'admin', password: '' })

const handleLogin = () => {
  request.post('/login', form).then(res => {
    localStorage.setItem('student-user', JSON.stringify(res.data))
    ElMessage.success('登录成功')
    router.push('/')
  })
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f2f5; }
.box-card { width: 400px; text-align: center; }
.btn { width: 100%; margin-top: 10px; }
</style>