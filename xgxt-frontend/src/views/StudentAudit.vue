<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-tag type="primary" size="large">新生学籍审核</el-tag>
      <span style="margin-left: 15px; color: #666; font-size: 14px;">
        审核通过后，系统将自动把学生分配到对应专业的班级中
      </span>
      <el-button type="primary" style="float: right;" @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="userNo" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="major" label="申请就读专业" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleAudit(scope.row.id, 2)">审核通过</el-button>
          <el-button type="danger" size="small" @click="handleAudit(scope.row.id, 3)">驳回重选</el-button>
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

const loadData = () => {
  request.get('/student/audit-list').then(res => {
    tableData.value = res.data
  })
}

const handleAudit = (id, status) => {
  const actionText = status === 2 ? '通过并自动分班' : '驳回'
  ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: status === 2 ? 'success' : 'warning'
  }).then(() => {
    request.put('/student/audit', { id, auditStatus: status }).then(res => {
      // 后端通过后会返回自动分配的班级名提示
      ElMessage.success('操作成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>