<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
      <el-input v-model="searchKeyword" placeholder="姓名/学号/奖项" style="width: 220px;" clearable prefix-icon="Search" />
      <el-select v-model="searchStatus" placeholder="所有状态" clearable style="width: 160px;">
        <el-option label="待初审" :value="0" />
        <el-option label="初审驳回" :value="1" />
        <el-option label="初审通过待终审" :value="2" />
        <el-option label="终审驳回" :value="3" />
        <el-option label="终审通过" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData">查询记录</el-button>
      <el-button type="warning" @click="exportData" v-if="user.role !== 'STUDENT'">导出报表</el-button>
    </div>

    <el-table :data="tableData" border stripe :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="studentName" label="姓名" min-width="100" />
      <el-table-column prop="honorName" label="申请奖项" min-width="150" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" />

      <el-table-column label="最终状态" min-width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.status === 0">待初审</span>
          <span v-else-if="scope.row.status === 1">初审驳回</span>
          <span v-else-if="scope.row.status === 2">初审同意待终审</span>
          <span v-else-if="scope.row.status === 3">终审驳回</span>
          <span v-else-if="scope.row.status === 4">已同意</span>
        </template>
      </el-table-column>

      <el-table-column label="经办人记录" min-width="160">
        <template #default="scope">
          <div style="font-size: 13px; color: #606266;">
            <div>初审：{{ scope.row.teacherApprover || '等待处理' }}</div>
            <div>终审：{{ scope.row.adminApprover || '等待处理' }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" plain size="small" @click="openDetail(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="荣誉申报档案" width="650px" top="10vh">
      <div v-if="currentHonor">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentHonor.studentName }}</el-descriptions-item>
          <el-descriptions-item label="奖项名称"><b>{{ currentHonor.honorName }}</b></el-descriptions-item>
        </el-descriptions>
        <div style="border: 1px solid #EBEEF5; padding: 18px; border-radius: 6px; background-color: #fcfcfc;">
          <div style="font-weight: bold; margin-bottom: 12px;">主要事迹说明：</div>
          <div style="line-height: 1.8; color: #606266; white-space: pre-wrap;">{{ currentHonor.reason }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const tableData = ref([])
const searchKeyword = ref('')
const searchStatus = ref(null)
const detailVisible = ref(false)
const currentHonor = ref(null)

const loadData = () => {
  request.get('/honor/list', {
    params: { role: user.role, status: searchStatus.value, keyword: searchKeyword.value }
  }).then(res => tableData.value = res.data)
}

const openDetail = (row) => {
  currentHonor.value = row
  detailVisible.value = true
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
    const response = await fetch('http://localhost:8080/api/honor/export', {
      method: 'GET',
      headers: {
        'Authorization': user.token
      }
    })

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error(errorData.msg || '导出失败，后端拒绝访问')
      return
    }

    const blob = await response.blob()

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生荣誉申报报表.xlsx' // 导出的文件名
    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('报表导出成功！')

  } catch (error) {
    console.error('导出失败详情:', error)
    ElMessage.error('网络请求失败，请检查后端服务')
  }
}

onMounted(() => loadData())
</script>