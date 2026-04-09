<template>
  <div class="discipline-container">

    <div v-if="user.role === 'STUDENT'">
      <div style="margin-bottom: 20px;">
        <span style="margin-left: 15px; color: #666; font-size: 14px;">违纪处分记录</span>
        <el-button type="primary" style="float: right;" @click="loadData">刷新记录</el-button>
      </div>

      <el-alert v-if="tableData.length === 0" title="您当前没有任何违纪处分。" type="success" show-icon style="margin-bottom: 20px;" />

      <el-table v-else :data="tableData" border stripe size="large" style="width: 100%;" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column prop="createTime" label="记录时间" min-width="160" />
        <el-table-column prop="violationType" label="违纪类型" min-width="140" />
        <el-table-column prop="description" label="违纪事实描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="当前状态/处分级别" min-width="180" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 0">待核查</span>
            <span v-else-if="scope.row.status === 1">
              {{ scope.row.punishmentType }}
            </span>
            <span v-else-if="scope.row.status === 2">已撤销</span>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="举报人" min-width="100" v-if="user.role !== 'STUDENT'" />
      </el-table>
    </div>


    <div v-else>
      <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
        <el-input v-model="searchKeyword" placeholder="按姓名或学号搜索" style="width: 250px;" clearable prefix-icon="Search" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button v-if="user.role === 'TEACHER'" type="danger" @click="openReportDialog">违纪行为上报</el-button>
        <el-button type="warning" @click="exportData" v-if="user.role === 'ADMIN'">导出报表</el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%;" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="majorName" label="专业" min-width="160" show-overflow-tooltip />
        <el-table-column prop="violationType" label="违纪类型" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.violationType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" min-width="160" />
        <el-table-column label="状态与处分" min-width="160" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 0">待核查</span>

            <span v-else-if="scope.row.status === 1">
              {{ scope.row.punishmentType || '已处分' }}
            </span>

            <span v-else-if="scope.row.status === 2" type="info">已撤销</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" plain @click="openDetail(scope.row)">查看详情</el-button>
            <el-button v-if="user.role === 'TEACHER' && scope.row.status === 0" type="danger" size="small" plain @click="handleDelete(scope.row.id)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="reportDialogVisible" title="学生违纪行为上报" width="550px">
      <el-alert title="输入学号后，系统会自动获取该学生信息。" type="info" show-icon style="margin-bottom: 15px;" />

      <el-form :model="form" label-width="90px">
        <el-form-item label="违纪学号" required>
          <el-input v-model="form.studentNo" placeholder="请输入违纪学生的学号" @blur="fetchStudentInfo" />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" disabled placeholder="系统自动获取" />
        </el-form-item>
        <el-form-item label="专业班级">
          <el-input :value="(form.majorName || '') + ' - ' + (form.className || '')" disabled placeholder="系统自动获取" />
        </el-form-item>

        <el-form-item label="违纪类型" required>
          <el-select v-model="form.violationType" placeholder="请选择违纪行为类型" style="width: 100%;">
            <el-option label="考试作弊" value="考试作弊" />
            <el-option label="旷课迟到" value="旷课迟到" />
            <el-option label="寻衅滋事" value="寻衅滋事" />
            <el-option label="夜不归宿" value="夜不归宿" />
            <el-option label="损坏公物" value="损坏公物" />
            <el-option label="其他违纪" value="其他违纪" />
          </el-select>
        </el-form-item>
        <el-form-item label="违纪事实" required>
          <el-input type="textarea" v-model="form.description" placeholder="请描述违纪发生的时间、地点及具体经过..." :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">立即上报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="学生违纪行为档案" width="650px" top="10vh" @closed="auditPunishment = ''">
      <div v-if="currentRecord">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="违纪类型">
            <span>{{ currentRecord.violationType }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ currentRecord.createTime }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentRecord.reporter }}</el-descriptions-item>
          <el-table-column label="当前状态/处分级别" min-width="180" align="center">
            <template #default="scope">
              <span v-if="scope.row.status === 0">待核查</span>

              <span v-else-if="scope.row.status === 1">
                {{ scope.row.punishmentType ? '：' + scope.row.punishmentType : '' }}
              </span>

              <span v-else-if="scope.row.status === 2">已撤销</span>
            </template>
          </el-table-column>
        </el-descriptions>

        <div style="border: 1px solid #fde2e2; padding: 15px; border-radius: 4px; background-color: #fef0f0;">
          <div>违纪事实与经过：</div>
          <div style="line-height: 1.8; color: #606266; white-space: pre-wrap;">{{ currentRecord.description }}</div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">


          <div v-if="user.role === 'ADMIN' && currentRecord" style="float:right">


            <div v-if="currentRecord.status === 0" style="display: flex; gap: 10px; align-items: center;">
              <div>经学工处决定，给予该生</div>
              <el-select v-model="auditPunishment" placeholder="选择处分类型" style="width: 140px;" size="large">
                <el-option label="警告" value="警告" />
                <el-option label="严重警告" value="严重警告" />
                <el-option label="记过" value="记过" />
                <el-option label="留校察看" value="留校察看" />
                <el-option label="开除学籍" value="开除学籍" />
              </el-select>
              <div>处分</div>
              <el-button  type="danger" @click="handleAudit(currentRecord, 1)">确定</el-button>
              <el-button  type="info" @click="handleAudit(currentRecord, 2)">撤销</el-button>
              <el-button @click="detailVisible = false">关闭</el-button>
            </div>

            <div v-else-if="currentRecord.status === 1">
              <el-button type="warning" @click="handleRevoke(currentRecord)">撤销</el-button>
              <el-button @click="detailVisible = false">关闭</el-button>
            </div>


          </div>
        </div>
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

// 管理员选择的处分级别
const auditPunishment = ref('')

const loadData = () => {
  request.get('/discipline/list', {
    params: { role: user.role, userNo: user.userNo, userName: user.name, keyword: searchKeyword.value }
  }).then(res => {
    tableData.value = res.data
  })
}

const openReportDialog = () => {
  form.value = { reporter: user.name, violationType: '考试作弊' }
  reportDialogVisible.value = true
}

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

const submitReport = () => {
  if (!form.value.studentName || !form.value.description) {
    ElMessage.warning('请确认学生姓名无误，并填写违纪事实')
    return
  }
  request.post('/discipline/report', form.value).then(res => {
    ElMessage.success(res.msg)
    reportDialogVisible.value = false
    loadData()
  })
}

const openDetail = (row) => {
  currentRecord.value = row
  auditPunishment.value = '' // 打开详情时清空选择框
  detailVisible.value = true
}

// 【重构】管理员审批逻辑 (加入处分级别校验)
const handleAudit = (row, newStatus) => {
  // 如果是同意下达处分，必须选择处分级别
  if (newStatus === 1 && !auditPunishment.value) {
    ElMessage.warning('给予处分前，必须先选择【处分类型】！')
    return
  }

  const actionText = newStatus === 1 ? `决定给予该生【${auditPunishment.value}】处分` : '驳回'
  ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', {
    type: newStatus === 1 ? 'error' : 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {

    // 构建发送给后端的参数
    const payload = {
      id: row.id,
      status: newStatus,
      adminApprover: user.name
    }
    // 只有下达处分时，才向后端传递处分级别
    if (newStatus === 1) {
      payload.punishmentType = auditPunishment.value
    }

    request.put('/discipline/audit', payload).then(res => {
      ElMessage.success(res.msg)
      detailVisible.value = false
      loadData()
    })
  }).catch(() => {})
}

// 管理员撤销已生效的处分
const handleRevoke = (row) => {
  ElMessageBox.confirm(`确定要撤销该学生的【${row.punishmentType}】处分吗？`, '确认撤销', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    request.put('/discipline/audit', {
      id: row.id,
      status: 2,
      adminApprover: user.name
    }).then(res => {
      ElMessage.success('处分已撤销')
      detailVisible.value = false
      loadData()
    })
  }).catch(() => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('正在等待管理员核查，确定要撤回这条上报记录吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/discipline/${id}`).then(res => {
      ElMessage.success('已撤回')
      loadData()
    })
  }).catch(() => {})
}

// 终极版：绕过所有拦截器的原生 Fetch 导出
const exportData = async () => {
  const user = JSON.parse(localStorage.getItem('student-user') || '{}')

  if (!user.token) {
    ElMessage.error('获取登录状态失败，请重新登录')
    return
  }

  ElMessage.info('正在请求生成报表，请稍候...')

  try {
    // 使用浏览器原生的 fetch，彻底摆脱 axios 拦截器的干扰
    const response = await fetch('http://localhost:8080/api/discipline/export', {
      method: 'GET',
      headers: {
        'Authorization': user.token
      }
    })

    // 1. 检查是不是由于没权限导致后端返回了 JSON 报错
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error(errorData.msg || '导出失败，后端拒绝访问')
      return
    }

    // 2. 正常接收二进制文件流
    const blob = await response.blob()

    // 3. 触发浏览器原生下载
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生违纪处分报表.xlsx' // 导出的文件名
    document.body.appendChild(link)
    link.click()

    // 4. 清理内存
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('报表导出成功！')

  } catch (error) {
    console.error('导出失败详情:', error)
    ElMessage.error('网络请求失败，请检查后端服务')
  }
}

onMounted(() => {
  loadData()
})
</script>