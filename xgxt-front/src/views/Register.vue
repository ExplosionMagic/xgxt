<template>
  <div class="register-container">
    <el-card class="box-card">
      <h2>学生账号注册</h2>
      <el-form :model="form" label-width="80px" size="default">
        <el-divider content-position="left">必填信息</el-divider>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入真实姓名"/>
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="用于登录和找回密码"/>
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入密码"/>
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"/>
        </el-form-item>

        <el-divider content-position="left">学籍信息 (选填)</el-divider>
        <el-form-item label="专业">
          <el-input v-model="form.major" placeholder="例如：计算机科学与技术"/>
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" placeholder="例如：2023级"/>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" placeholder="例如：计科1班"/>
        </el-form-item>
        <el-form-item label="联系地址">
          <el-input v-model="form.address" placeholder="例如：XX校区X栋XXX室"/>
        </el-form-item>

        <el-button type="success" class="btn" @click="handleRegister">提交注册</el-button>
        <div style="text-align: right; margin-top: 15px;">
          <el-link type="primary" @click="$router.push('/login')">已有账号？返回登录</el-link>
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
const form = reactive({
  name: '', gender: '男', phone: '', password: '', confirmPassword: '',
  major: '', grade: '', className: '', address: '', role: 'STUDENT' // 默认强制为学生
})

const handleRegister = () => {
  // 简单前端校验
  if (!form.name || !form.gender || !form.phone || !form.password) {
    ElMessage.warning('请填写所有必填项！')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致！')
    return
  }
  if (form.phone.length !== 11) {
    ElMessage.warning('请输入11位有效的手机号码')
    return
  }

  // 构造请求数据 (剔除不需要传给后端的确认密码)
  const submitData = { ...form }
  delete submitData.confirmPassword

  request.post('/register', submitData).then(res => {
    ElMessage.success('注册成功，系统已自动分配学号，请登录')
    router.push('/login')
  })
}
</script>

<style scoped>
.register-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background-color: #f0f2f5; padding: 20px 0; }
.box-card { width: 500px; }
.btn { width: 100%; margin-top: 10px; }
</style>