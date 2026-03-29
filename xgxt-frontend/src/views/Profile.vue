<template>
  <div class="profile-container">
    <el-card class="box-card">

      <div v-if="userInfo.auditStatus === 0 || userInfo.auditStatus === 3" style="text-align: center; padding: 40px 20px;">
        <el-alert v-if="userInfo.auditStatus === 3" title="您的专业选择被管理员驳回，请重新选择" type="error" show-icon style="margin-bottom: 20px;" />
        <h2 style="margin-bottom: 30px;">欢迎来到系统，请先完善您的专业信息</h2>
        <el-form style="max-width: 400px; margin: 0 auto;">
          <el-form-item label="选择专业" required>
            <el-select v-model="selectedMajor" placeholder="请选择您就读的专业" style="width: 100%;">
              <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
            </el-select>
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%; margin-top: 20px;" @click="submitMajor">提交审核</el-button>
        </el-form>
      </div>

      <div v-else-if="userInfo.auditStatus === 1" style="text-align: center; padding: 60px 20px;">
        <el-icon size="60" color="#E6A23C"><Clock /></el-icon>
        <h2>专业审核中，请耐心等待</h2>
        <p style="color: #666;">管理员正在为您审核专业并分配班级，分配完成后即可使用系统全部功能。</p>
        <el-button type="primary" plain style="margin-top: 20px;" @click="loadInfo">刷新状态</el-button>
      </div>

      <el-tabs v-else v-model="activeName">

        <el-tab-pane label="个人信息" name="info">
          <div style="margin: 20px 0;">
            <el-descriptions title="我的基本信息" :column="2" border>
              <el-descriptions-item label="学号" v-if="userInfo.role == 'STUDENT'">{{ userInfo.userNo }}</el-descriptions-item>
              <el-descriptions-item label="教职员工ID" v-if="userInfo.role !== 'STUDENT'">{{ userInfo.userNo }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ userInfo.gender }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="联系地址">{{ userInfo.address || '--' }}</el-descriptions-item>

              <template>
                <el-descriptions-item label="专业">{{ userInfo.major || '--' }}</el-descriptions-item>
                <el-descriptions-item label="年级">{{ userInfo.grade || '--' }}</el-descriptions-item>
                <el-descriptions-item label="班级">
                  <span v-if="userInfo.className">{{ userInfo.className }}</span>
                  <span v-else>--</span>
                </el-descriptions-item>
              </template>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="修改个人信息" name="edit">
          <el-form :model="editForm" label-width="100px" style="max-width: 500px; margin-top: 30px;">
            <el-form-item label="学号" v-if="userInfo.role == 'STUDENT'">
              <el-input v-model="editForm.userNo" disabled />
            </el-form-item>
            <el-form-item label="教职员工ID" v-if="userInfo.role !== 'STUDENT'">
              <el-input v-model="editForm.userNo" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="editForm.name" disabled />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input v-model="editForm.phone" placeholder="请输入新的手机号" />
            </el-form-item>
            <el-form-item label="联系地址">
              <el-input v-model="editForm.address" placeholder=" " />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" label-width="100px" style="max-width: 400px; margin-top: 30px;">
            <el-alert title="修改成功后将强制退出，需要使用新密码重新登录。" type="warning" show-icon style="margin-bottom: 20px;" :closable="false" />

            <el-form-item label="原密码" required>
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码 (不少于6位)" show-password />
            </el-form-item>
            <el-form-item label="确认密码" required>
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="danger" @click="savePassword">确认修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

      </el-tabs>

    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

// 页签与用户信息状态
const router = useRouter() // 引入路由用于跳转
const activeName = ref('info')
const currentUser = JSON.parse(localStorage.getItem('student-user') || '{}')
const userInfo = ref({ auditStatus: 2 }) // 默认给2防止白屏闪烁
const editForm = ref({})
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

// 新手引导用到的变量
const majorList = ref([])
const selectedMajor = ref('')

// 格式化角色标签
const getRoleName = (role) => {
  if (role === 'ADMIN') return '超级管理员'
  if (role === 'TEACHER') return '教职员工'
  return '学生'
}
const getRoleType = (role) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  return 'success'
}

// 1. 加载数据
const loadInfo = () => {
  request.get(`/user/info/${currentUser.userNo}`).then(res => {
    userInfo.value = res.data
    // 深拷贝一份给修改表单使用
    editForm.value = JSON.parse(JSON.stringify(res.data))

    // 如果处于待完善状态，额外加载专业列表供其选择
    if (res.data.auditStatus === 0 || res.data.auditStatus === 3) {
      request.get('/major/list').then(mRes => {
        majorList.value = mRes.data
      })
    }
  })
}

// 2. 提交专业审核
const submitMajor = () => {
  if (!selectedMajor.value) {
    ElMessage.warning('请选择专业')
    return
  }
  request.put('/student/submit-major', {
    id: userInfo.value.id,
    major: selectedMajor.value
  }).then(res => {
    ElMessage.success('提交成功！')
    loadInfo() // 刷新页面状态进入“审核中”
  })
}

// 3. 保存个人基本信息
const saveProfile = () => {
  if (!editForm.value.phone) {
    ElMessage.warning('手机号不能为空')
    return
  }
  request.put('/user/profile', {
    id: editForm.value.id,
    phone: editForm.value.phone,
    address: editForm.value.address
  }).then(res => {
    ElMessage.success('个人基本信息更新成功')
    loadInfo()
    activeName.value = 'info' // 保存后切回信息展示页签
  })
}

// 4. 保存新密码
const savePassword = () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (pwdForm.newPassword === pwdForm.oldPassword) {
    ElMessage.warning('新密码不能与原密码相同！')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致！')
    return
  }

  request.put('/user/password', {
    id: userInfo.value.id,
    oldPassword: pwdForm.oldPassword,
    newPassword: pwdForm.newPassword
  }).then(res => {
    // 假设你的拦截器已经在错误时拦截了，走到这里代表成功
    ElMessage.success('密码修改成功，请重新登录')
    // 强制登出
    localStorage.removeItem('student-user')
    router.push('/login')
  })
}

onMounted(() => {
  loadInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.box-card {
  max-width: 900px;
  margin: 0 auto;
  min-height: 500px;
}
</style>