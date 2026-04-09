<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-alert title="荣誉奖项申请需经过【初审】与【复审】多级审批，请如实填写相关信息。" type="success" show-icon style="margin-bottom: 15px;" />
      <el-button type="primary" @click="openApplyDialog">申请荣誉奖项</el-button>
      <el-button type="success" @click="loadData">刷新状态</el-button>
    </div>

    <el-table :data="tableData" border stripe size="large" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column prop="honorName" label="申请奖项" width="180" />
      <el-table-column prop="reason" label="主要事迹" min-width="200" show-overflow-tooltip />
      <el-table-column label="当前进度" width="160" align="center">
        <template #default="scope">
          <span v-if="scope.row.status === 0">待初审</span>
          <span v-else-if="scope.row.status === 1">初审驳回</span>
          <span v-else-if="scope.row.status === 2">初审通过待复审</span>
          <span v-else-if="scope.row.status === 3">复审驳回</span>
          <span v-else-if="scope.row.status === 4">复审通过</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="handleCancel(scope.row.id)">撤销</el-button>
          <span v-else style="color: #999; font-size: 12px;">已锁定</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="填写荣誉奖项申请表" width="600px">
      <el-form :model="form" label-width="90px">
        <el-descriptions :column="2" border style="margin-bottom: 15px;">
          <el-descriptions-item label="姓名">{{ user.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ user.userNo }}</el-descriptions-item>
        </el-descriptions>

        <el-form-item label="申请奖项" required>
          <el-input v-model="form.honorName" placeholder="例如：国家奖学金、优秀学生干部" />
        </el-form-item>

        <el-form-item label="主要事迹" required>
          <el-input type="textarea" v-model="form.reason" placeholder="请详细描述申请该奖项的理由及个人事迹" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const userInfo = ref({})
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/honor/list', { params: { role: 'STUDENT', studentNo: user.userNo } }).then(res => {
    tableData.value = res.data
  })
  request.get(`/user/info/${user.userNo}`).then(res => userInfo.value = res.data || {})
}

const openApplyDialog = () => {
  if (!userInfo.value.major) return ElMessage.warning('学籍未完善，无法申请')
  form.value = {
    studentNo: user.userNo, studentName: user.name,
    majorName: userInfo.value.major, className: userInfo.value.className,
    honorName: '', reason: ''
  }
  dialogVisible.value = true
}

const submitApply = () => {
  if (!form.value.honorName || !form.value.reason) return ElMessage.warning('请将表单填写完整')
  request.post('/honor/apply', form.value).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('撤销后需重新排队，确认撤销吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/honor/${id}`).then(() => {
      ElMessage.success('撤销成功')
      loadData()
    })
  })
}

onMounted(() => loadData())
</script>