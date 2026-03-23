<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-tag type="warning" size="large">请假审批中心</el-tag>
      <span style="margin-left: 15px; color: #666; font-size: 14px;">仅显示处于“待审批”状态的请假单</span>
      <el-button type="primary" style="float: right;" @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="reason" label="请假事由" min-width="150" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="150" />
      <el-table-column prop="endTime" label="结束时间" width="150" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleAudit(scope.row, 1)">同意</el-button>
          <el-button type="danger" size="small" @click="handleAudit(scope.row, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const user = JSON.parse(localStorage.getItem('student-user') || '{}')

const loadData = () => {
  // 仅查询状态为 0 (待审批) 的记录
  request.get('/leave/list', { params: { status: 0 } }).then(res => {
    tableData.value = res.data
  })
}

const handleAudit = (row, status) => {
  const actionText = status === 1 ? '同意' : '驳回'
  ElMessageBox.confirm(`确定要${actionText}该学生的请假申请吗？`, '审批确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: status === 1 ? 'success' : 'warning'
  }).then(() => {
    request.put('/leave/audit', {
      id: row.id,
      status: status,
      approver: user.name // 记录是哪位老师/管理员审批的
    }).then(res => {
      ElMessage.success(`操作成功`)
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>