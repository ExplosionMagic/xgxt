<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-tag type="warning" size="large">选修课程审核中心</el-tag>
      <span style="margin-left: 15px; color: #666; font-size: 14px;">
        仅显示处于“待审核”状态的选课申请
      </span>
      <el-button type="primary" style="float: right;" @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="140" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="courseName" label="申请课程" />
      <el-table-column prop="teacherName" label="任课教师" width="100" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleAudit(scope.row.id, 1)">通过</el-button>
          <el-button type="danger" size="small" @click="handleAudit(scope.row.id, 2)">驳回</el-button>
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
  // 只查询 status = 0 (待审核) 的记录
  const params = { status: 0 }
  // 如果是教师登录，只能审核自己的课程；管理员则可以看全部
  if (user.role === 'TEACHER') {
    params.teacherName = user.name
  }

  request.get('/score/list', { params }).then(res => {
    tableData.value = res.data
  })
}

const handleAudit = (id, status) => {
  const actionText = status === 1 ? '通过' : '驳回'
  ElMessageBox.confirm(`确定要${actionText}该选修申请吗？`, '审核确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: status === 1 ? 'success' : 'warning'
  }).then(() => {
    request.put('/score/audit', { id, status }).then(res => {
      ElMessage.success(`操作成功`)
      loadData() // 审核后刷新列表
    })
  })
}

onMounted(() => {
  loadData()
})
</script>