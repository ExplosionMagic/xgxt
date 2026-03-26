<template>
  <div>
    <div style="margin-bottom: 20px;">
      <span style="margin-left: 15px; color: #666; font-size: 14px;">
        仅显示当前需要您处理的审批单
      </span>
      <el-button type="primary" style="float: right;" @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="tableData" border stripe :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="majorName" label="专业" width="140" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" width="150" />

      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button type="info" size="small" plain @click="openDetail(scope.row)">查看详情</el-button>

          <template v-if="user.role === 'TEACHER'">
            <el-button type="success" size="small" @click="handleTeacherAudit(scope.row, 2)">通过</el-button>
            <el-button type="danger" size="small" @click="handleTeacherAudit(scope.row, 1)">驳回</el-button>
          </template>

          <template v-if="user.role === 'ADMIN'">
            <el-button type="success" size="small" @click="handleAdminAudit(scope.row, 4)">通过</el-button>
            <el-button type="danger" size="small" @click="handleAdminAudit(scope.row, 3)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="资助申请详情阅读" width="650px">
      <div v-if="currentAid">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentAid.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentAid.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentAid.majorName }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentAid.className }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentAid.applyTime }}</el-descriptions-item>
        </el-descriptions>

        <div style="border: 1px solid #EBEEF5; padding: 15px; border-radius: 4px; background-color: #fcfcfc;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #303133;">家庭情况及申请理由说明：</div>
          <div style="line-height: 1.8; color: #606266; font-size: 14px; white-space: pre-wrap;">{{ currentAid.reason }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="user.role === 'TEACHER'">
          <el-button type="danger" @click="handleTeacherAudit(currentAid, 1)">驳回</el-button>
          <el-button type="success" @click="handleTeacherAudit(currentAid, 2)">批准</el-button>
        </template>
        <template v-if="user.role === 'ADMIN'">
          <el-button type="danger" @click="handleAdminAudit(currentAid, 3)">驳回</el-button>
          <el-button type="success" @click="handleAdminAudit(currentAid, 4)">批准</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const tableData = ref([])

// 详情弹窗状态
const detailVisible = ref(false)
const currentAid = ref(null)

const loadData = () => {
  const targetStatus = user.role === 'TEACHER' ? 0 : 2;
  request.get('/aid/list', {
    params: { role: user.role, status: targetStatus }
  }).then(res => {
    tableData.value = res.data
  })
}

// 【新增】打开详情弹窗
const openDetail = (row) => {
  currentAid.value = row
  detailVisible.value = true
}

const handleTeacherAudit = (row, newStatus) => {
  const actionText = newStatus === 2 ? '初审通过并流转给管理员' : '驳回'
  ElMessageBox.confirm(`确定要${actionText}吗？`, '初审确认', { type: newStatus === 2 ? 'success' : 'warning' }).then(() => {
    request.put('/aid/audit/teacher', {
      id: row.id,
      status: newStatus,
      teacherApprover: user.name
    }).then(res => {
      ElMessage.success(res.msg)
      detailVisible.value = false // 关掉弹窗
      loadData()
    })
  })
}

const handleAdminAudit = (row, newStatus) => {
  const actionText = newStatus === 4 ? '终审批准并列入资助名单' : '驳回'
  ElMessageBox.confirm(`确定要${actionText}吗？`, '终审确认', { type: newStatus === 4 ? 'success' : 'warning' }).then(() => {
    request.put('/aid/audit/admin', {
      id: row.id,
      status: newStatus,
      adminApprover: user.name
    }).then(res => {
      ElMessage.success(res.msg)
      detailVisible.value = false // 关掉弹窗
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>