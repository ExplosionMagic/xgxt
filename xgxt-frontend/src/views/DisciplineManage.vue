<template>
  <div class="discipline-container">

    <div v-if="user.role === 'STUDENT'">
      <div style="margin-bottom: 20px;">
        <el-tag type="danger" size="large">我的违纪与处分记录</el-tag>
        <span style="margin-left: 15px; color: #666; font-size: 14px;">遵守校规校纪是每位学生的基本义务</span>
        <el-button type="primary" style="float: right;" @click="loadData">刷新记录</el-button>
      </div>

      <el-alert v-if="tableData.length === 0" title="您当前没有任何违纪或处分记录，请继续保持！" type="success" show-icon style="margin-bottom: 20px;" />

      <el-table v-else :data="tableData" border stripe size="large" style="width: 100%;" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column prop="createTime" label="记录时间" min-width="160" />
        <el-table-column prop="violationType" label="违纪类型" min-width="150" />
        <el-table-column prop="description" label="违纪事实描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="当前状态" min-width="150" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning" size="large">管理员核实中</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="danger" effect="dark" size="large">处分已生效</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info" size="large">指控撤销/驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="上报人" min-width="120" />
      </el-table>
    </div>


    <div v-else>
      <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
        <el-input v-model="searchKeyword" placeholder="按姓名或学号搜索" style="width: 250px;" clearable prefix-icon="Search" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button v-if="user.role === 'TEACHER'" type="danger" @click="openReportDialog">新增违纪上报</el-button>
      </div>

      <el-table :data="tableData" border stripe size="large" style="width: 100%;" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="majorName" label="专业" min-width="180" show-overflow-tooltip />
        <el-table-column prop="violationType" label="违纪类型" min-width="140">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold;">{{ scope.row.violationType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" min-width="160" />
        <el-table-column label="状态" min-width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待核实</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="danger" effect="dark">已处分</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" plain @click="openDetail(scope.row)">详情</el-button>
            <el-button v-if="user.role === 'TEACHER' && scope.row.status === 0" type="danger" size="small" plain @click="handleDelete(scope.row.id)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="reportDialogVisible" title="填写违纪上报单" width="550px">
      <el-alert title="输入学号后点击空白处，系统会自动匹配该学生信息。" type="info" show-icon style="margin-bottom: 15px;" />

      <el-form :model="form" label-width="90px">
        <el-form-item label="违纪学号" required>
          <el-input v-model="form.studentNo" placeholder="请输入违纪学生的学号" @blur="fetchStudentInfo" />
        </el-form-item>

        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" disabled placeholder="自动获取" />
        </el-form-item>
        <el-form-item label="专业班级">
          <el-input :value="(form.majorName || '') + ' - ' + (form.className || '')" disabled placeholder="自动获取" />
        </el-form-item>

        <el-form-item label="违纪类型" required>
          <el-select v-model="form.violationType" placeholder="请选择违纪类型" style="width: 100%;">
            <el-option label="考试作弊" value="考试作弊" />
            <el-option label="旷课迟到" value="旷课迟到" />
            <el-option label="寻衅滋事" value="寻衅滋事" />
            <el-option label="夜不归宿" value="夜不归宿" />
            <el-option label="损坏公物" value="损坏公物" />
            <el-option label="其他违纪" value="其他违纪" />
          </el-select>
        </el-form-item>
        <el-form-item label="违纪事实" required>
          <el-input type="textarea" v-model="form.description" placeholder="请客观详细描述违纪发生的时间、地点及具体经过..." :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">立即上报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="违纪案件档案" width="600px" top="10vh">
      <div v-if="currentRecord">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="违纪类型">
            <span style="color: red; font-weight: bold;">{{ currentRecord.violationType }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ currentRecord.createTime }}</el-descriptions-item>
          <el-descriptions-item label="上报教师">{{ currentRecord.reporter }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag v-if="currentRecord.status === 0" type="warning">待审批</el-tag>
            <el-tag v-else-if="currentRecord.status === 1" type="danger">处分生效</el-tag>
            <el-tag v-else-if="currentRecord.status === 2" type="info">已驳回/已撤销</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div style="border: 1px solid #fde2e2; padding: 15px; border-radius: 4px; background-color: #fef0f0;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #F56C6C;">违纪事实与经过：</div>
          <div style="line-height: 1.8; color: #606266; white-space: pre-wrap;">{{ currentRecord.description }}</div>
        </div>
      </div>

      <template #footer>
        <el-button size="large" @click="detailVisible = false">关闭预览</el-button>

        <template v-if="user.role === 'ADMIN' && currentRecord">
          <template v-if="currentRecord.status === 0">
            <el-button size="large" type="info" @click="handleAudit(currentRecord, 2)">驳回上报 (指控不成立)</el-button>
            <el-button size="large" type="danger" @click="handleAudit(currentRecord, 1)">批准上报 (处分生效)</el-button>
          </template>

          <template v-else-if="currentRecord.status === 1">
            <el-button size="large" type="warning" @click="handleRevoke(currentRecord)">撤销该处分</el-button>
          </template>
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
const searchKeyword = ref('')

// 弹窗控制
const reportDialogVisible = ref(false)
const detailVisible = ref(false)
const form = ref({})
const currentRecord = ref(null)

// 加载数据
const loadData = () => {
  request.get('/discipline/list', {
    params: {
      role: user.role,
      userNo: user.userNo,
      userName: user.name,
      keyword: searchKeyword.value
    }
  }).then(res => {
    tableData.value = res.data
  })
}

// 教师打开上报弹窗
const openReportDialog = () => {
  form.value = { reporter: user.name, violationType: '考试作弊' }
  reportDialogVisible.value = true
}

// 通过学号自动拉取学生信息
const fetchStudentInfo = () => {
  if (!form.value.studentNo) return
  request.get(`/user/info/${form.value.studentNo}`).then(res => {
    if (res.data && res.data.role === 'STUDENT') {
      form.value.studentName = res.data.name
      form.value.majorName = res.data.major
      form.value.className = res.data.className
      ElMessage.success('已成功匹配到该学生信息')
    } else {
      form.value.studentName = ''
      form.value.majorName = ''
      form.value.className = ''
      ElMessage.warning('未找到该学号对应的学生，请检查输入')
    }
  }).catch(() => {})
}

// 提交上报
const submitReport = () => {
  if (!form.value.studentName || !form.value.description) {
    ElMessage.warning('请确保已匹配到学生姓名，并填写违纪事实')
    return
  }
  request.post('/discipline/report', form.value).then(res => {
    ElMessage.success(res.msg)
    reportDialogVisible.value = false
    loadData()
  })
}

// 打开详情
const openDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 管理员审批
const handleAudit = (row, newStatus) => {
  const actionText = newStatus === 1 ? '批准处分生效' : '驳回指控'
  ElMessageBox.confirm(`确定要 ${actionText} 吗？`, '审批确认', {
    type: newStatus === 1 ? 'error' : 'warning',
    confirmButtonText: '确定执行',
    cancelButtonText: '取消'
  }).then(() => {
    request.put('/discipline/audit', {
      id: row.id,
      status: newStatus,
      adminApprover: user.name
    }).then(res => {
      ElMessage.success(res.msg)
      detailVisible.value = false
      loadData()
    })
  }).catch(() => {}) // 加上 catch 防止取消时报错
}

// 管理员撤销已生效的处分
const handleRevoke = (row) => {
  ElMessageBox.confirm('确定要撤销该学生的违纪处分吗？撤销后该记录将变为"已撤销"状态，不再计入生效处分。', '撤销处分确认', {
    type: 'warning',
    confirmButtonText: '确定撤销',
    cancelButtonText: '暂不处理'
  }).then(() => {
    request.put('/discipline/audit', {
      id: row.id,
      status: 2, // 状态2即代表“已撤销/已驳回”
      adminApprover: user.name
    }).then(res => {
      ElMessage.success(res.msg || '处分已成功撤销')
      detailVisible.value = false
      loadData()
    })
  }).catch(() => {}) // 加上 catch 防止取消时报错
}

// 教师撤销上报
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要撤销这条违纪上报吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/discipline/${id}`).then(res => {
      ElMessage.success('已撤销')
      loadData()
    })
  }).catch(() => {}) // 加上 catch 防止取消时报错
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.discipline-container {
  padding: 10px;
}
</style>