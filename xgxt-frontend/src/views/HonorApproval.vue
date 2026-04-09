<template>
  <div>
    <div style="margin-bottom: 20px;">
      <span style="margin-left: 15px; color: #666; font-size: 14px;">仅显示待您处理的审批单</span>
      <el-button type="primary" style="float: right;" @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="tableData" border stripe :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="majorName" label="专业" min-width="140" show-overflow-tooltip />
      <el-table-column prop="honorName" label="申请奖项" min-width="150" />
      <el-table-column prop="applyTime" label="申请时间" width="160" />

      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button type="info" size="small" plain @click="openDetail(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="荣誉奖项申请" width="650px" top="10vh">
      <div v-if="currentHonor">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentHonor.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentHonor.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="专业班级" :span="2">{{ currentHonor.majorName }} - {{ currentHonor.className }}</el-descriptions-item>
          <el-descriptions-item label="申请荣誉奖项" :span="2"><span>{{ currentHonor.honorName }}</span></el-descriptions-item>
        </el-descriptions>

        <div style="border: 1px solid #EBEEF5; padding: 18px; border-radius: 6px; background-color: #fcfcfc;">
          <div style="font-weight: bold; margin-bottom: 12px; font-size: 15px;">主要事迹：</div>
          <div style="line-height: 1.8; color: #606266; font-size: 15px; white-space: pre-wrap;">{{ currentHonor.reason }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <template v-if="user.role === 'TEACHER'">
          <el-button type="danger" @click="handleAudit(currentHonor, 1, 'teacher')">驳回</el-button>
          <el-button type="success" @click="handleAudit(currentHonor, 2, 'teacher')">同意</el-button>
        </template>
        <template v-if="user.role === 'ADMIN'">
          <el-button type="danger" @click="handleAudit(currentHonor, 3, 'admin')">驳回</el-button>
          <el-button type="success" @click="handleAudit(currentHonor, 4, 'admin')">同意</el-button>
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
const detailVisible = ref(false)
const currentHonor = ref(null)

const loadData = () => {
  const targetStatus = user.role === 'TEACHER' ? 0 : 2;
  request.get('/honor/list', { params: { role: user.role, status: targetStatus } }).then(res => {
    tableData.value = res.data
  })
}

const openDetail = (row) => {
  currentHonor.value = row
  detailVisible.value = true
}

const handleAudit = (row, newStatus, roleType) => {
  const apiPath = roleType === 'teacher' ? '/honor/audit/teacher' : '/honor/audit/admin'
  const actionText = (newStatus === 2 || newStatus === 4) ? '同意' : '驳回'

  ElMessageBox.confirm(`确定要${actionText}吗？`, '审批确认', { type: (newStatus === 2 || newStatus === 4) ? 'success' : 'warning' }).then(() => {
    const payload = { id: row.id, status: newStatus }
    if (roleType === 'teacher') payload.teacherApprover = user.name
    else payload.adminApprover = user.name

    request.put(apiPath, payload).then(res => {
      ElMessage.success(res.msg)
      detailVisible.value = false
      loadData()
    })
  })
}

onMounted(() => loadData())
</script>