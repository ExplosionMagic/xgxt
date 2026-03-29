<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-alert title="资助申请需经过【初审】与【终审】两级审批，请如实填写。" type="success" show-icon style="margin-bottom: 15px;" />
      <el-button type="primary" @click="openApplyDialog" v-if="user.role === 'STUDENT'">发起资助申请</el-button>
      <el-button type="success" @click="loadData">刷新状态</el-button>
    </div>

    <el-table :data="tableData" border stripe :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
      <el-table-column label="当前进度" width="180" align="center">
        <template #default="scope">
          <span v-if="scope.row.status === 0">待初审</span>
          <span v-else-if="scope.row.status === 1">初审驳回</span>
          <span v-else-if="scope.row.status === 2">初审同意待终审</span>
          <span v-else-if="scope.row.status === 3">终审驳回</span>
          <span v-else-if="scope.row.status === 4">已同意</span>
        </template>
      </el-table-column>
      <el-table-column label="经办人" width="150" v-if="user.role !== 'STUDENT'">
        <template #default="scope">
          <div style="font-size: 12px; color: #666;">
            <div>初审: {{ scope.row.teacherApprover || '--' }}</div>
            <div>终审: {{ scope.row.adminApprover || '--' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="handleCancel(scope.row.id)">撤销</el-button>
          <span v-else style="color: #999; font-size: 12px;">不可操作</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="填写资助申请单" width="600px">
      <el-form :model="form" label-width="80px">
        <el-descriptions :column="2" border style="margin-bottom: 15px;">
          <el-descriptions-item label="姓名">{{ user.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ user.userNo }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ userInfo.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ userInfo.className }}</el-descriptions-item>
        </el-descriptions>

        <el-form-item label="申请理由" required>
          <el-input type="textarea" v-model="form.reason" placeholder="请详细描述家庭经济状况及申请资助的理由 (不少于50字)" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">确认提交表单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const userInfo = ref({}) // 存放实时查出来的详细学籍
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/aid/list', { params: { role: 'STUDENT', studentNo: user.userNo } }).then(res => {
    tableData.value = res.data
  })
  // 获取最新学籍信息，用于表单展示
  request.get(`/user/info/${user.userNo}`).then(res => {
    userInfo.value = res.data || {}
  })
}

const openApplyDialog = () => {
  if (!userInfo.value.major || !userInfo.value.className) {
    ElMessage.warning('您的学籍信息尚未完善，无法申请')
    return
  }
  form.value = {
    studentNo: user.userNo,
    studentName: user.name,
    majorName: userInfo.value.major,
    className: userInfo.value.className,
    reason: ''
  }
  dialogVisible.value = true
}

const submitApply = () => {
  if (!form.value.reason || form.value.reason.length < 10) {
    ElMessage.warning('请认真填写申请理由')
    return
  }
  request.post('/aid/apply', form.value).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('撤销后需重新排队审批，确认撤销吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/aid/${id}`).then(res => {
      ElMessage.success('撤销成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>