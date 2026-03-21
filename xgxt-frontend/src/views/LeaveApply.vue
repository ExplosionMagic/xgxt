<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-button type="primary" @click="handleAdd">发起请假申请</el-button>
      <el-button type="success" @click="loadData">刷新记录</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="reason" label="请假事由" min-width="150" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已同意</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">已驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approver" label="审批人" width="100" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="handleCancel(scope.row.id)">撤销</el-button>
          <span v-else style="color: #999; font-size: 12px;">不可操作</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="填写请假单" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="请假事由" required>
          <el-input type="textarea" v-model="form.reason" placeholder="请详细描述请假原因" :rows="3" />
        </el-form-item>
        <el-form-item label="起止时间" required>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
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
const dialogVisible = ref(false)
const form = ref({})
const dateRange = ref([])

const loadData = () => {
  request.get('/leave/list', { params: { studentNo: user.userNo } }).then(res => {
    tableData.value = res.data
  })
}

const handleAdd = () => {
  form.value = { studentNo: user.userNo, studentName: user.name }
  dateRange.value = []
  dialogVisible.value = true
}

const submitApply = () => {
  if (!form.value.reason || !dateRange.value || dateRange.value.length === 0) {
    ElMessage.warning('请填写事由并选择完整的起止时间')
    return
  }
  form.value.startTime = dateRange.value[0]
  form.value.endTime = dateRange.value[1]

  request.post('/leave/apply', form.value).then(res => {
    ElMessage.success('请假申请已提交')
    dialogVisible.value = false
    loadData()
  })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要撤销这条请假申请吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/leave/${id}`).then(res => {
      ElMessage.success('撤销成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>