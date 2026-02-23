<template>
  <div>
    <el-card style="max-width: 600px; margin: 20px auto;">
      <template #header>
        <div style="font-weight: bold;">个人信息中心</div>
      </template>
      <el-form :model="form" label-width="100px">
        <el-form-item label="学号/教工ID">
          <el-input v-model="form.userNo" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="性别">
          <el-input v-model="form.gender" disabled />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" disabled />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" disabled />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" disabled />
        </el-form-item>

        <el-divider>可修改信息</el-divider>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="联系地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.password" type="password" placeholder="若不修改密码请留空" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const form = ref({})
const currentUser = JSON.parse(localStorage.getItem('student-user') || '{}')

// 页面加载时获取当前用户的详细信息
const loadInfo = () => {
  request.get(`/user/info/${currentUser.userNo}`).then(res => {
    form.value = res.data
    form.value.password = '' // 密码置空，防止显示原密码
  })
}

const saveProfile = () => {
  request.put('/user/profile', form.value).then(res => {
    ElMessage.success('个人信息更新成功')
  })
}

onMounted(() => {
  loadInfo()
})
</script>