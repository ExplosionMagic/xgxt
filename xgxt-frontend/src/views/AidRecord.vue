<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
      <el-input v-model="searchKeyword" placeholder="输入姓名或学号搜索" style="width: 220px;" clearable prefix-icon="Search" />
      <el-select v-model="searchStatus" placeholder="所有审批状态" clearable style="width: 160px;">
        <el-option label="待初审" :value="0" />
        <el-option label="初审驳回" :value="1" />
        <el-option label="初审通过待复审" :value="2" />
        <el-option label="复审驳回" :value="3" />
        <el-option label="复审通过" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData">查询档案</el-button>
      <el-button type="warning" @click="exportData" v-if="user.role !== 'STUDENT'">导出报表</el-button>
    </div>

    <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%;"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }"
    >
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="studentName" label="姓名" min-width="140" />
      <el-table-column prop="majorName" label="专业" min-width="180" show-overflow-tooltip />
      <el-table-column prop="className" label="班级" min-width="140" />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" />

      <el-table-column label="最终状态" min-width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.status === 0">待初审</span>
          <span v-else-if="scope.row.status === 1">初审驳回</span>
          <span v-else-if="scope.row.status === 2">初审同意待复审</span>
          <span v-else-if="scope.row.status === 3">复审驳回</span>
          <span v-else-if="scope.row.status === 4">复审通过</span>
        </template>
      </el-table-column>

      <el-table-column label="经办人记录" min-width="180">
        <template #default="scope">
          <div style="font-size: 13px; color: #606266; line-height: 1.6;">
            <div style="display: flex; align-items: center; margin-bottom: 4px;">
              <span>初审：{{ scope.row.teacherApprover || '等待处理' }}</span>
            </div>
            <div style="display: flex; align-items: center;">
              <span>复审：{{ scope.row.adminApprover || '等待处理' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="130" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" plain @click="openDetail(scope.row)">查看档案</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="资助申请档案" width="650px" top="10vh">
      <div v-if="currentAid">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentAid.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentAid.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentAid.majorName }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentAid.className }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentAid.applyTime }}</el-descriptions-item>
        </el-descriptions>

        <div style="border: 1px solid #EBEEF5; padding: 18px; border-radius: 6px; background-color: #fcfcfc;">
          <div style="font-weight: bold; margin-bottom: 12px; color: #303133; font-size: 15px;">个人当前状况及申请理由：</div>
          <div style="line-height: 1.8; color: #606266; font-size: 15px; white-space: pre-wrap;">{{ currentAid.reason }}</div>
        </div>
      </div>
      <template #footer>
        <el-button size="large" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const tableData = ref([])

const searchKeyword = ref('')
const searchStatus = ref(null)

const detailVisible = ref(false)
const currentAid = ref(null)

const loadData = () => {
  request.get('/aid/list', {
    params: {
      role: user.role,
      status: searchStatus.value,
      keyword: searchKeyword.value
    }
  }).then(res => {
    tableData.value = res.data
  })
}

const openDetail = (row) => {
  currentAid.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})

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
    const response = await fetch('http://localhost:8080/api/aid/export', {
      method: 'GET',
      headers: {
        'Authorization': user.token
      }
    })

    // 1. 检查是不是由于没权限导致后端返回了 JSON 报错
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error('导出失败，后端拒绝访问')
      return
    }

    // 2. 正常接收二进制文件流
    const blob = await response.blob()

    // 3. 触发浏览器原生下载
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生资助申请记录表.xlsx' // 导出的文件名
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

</script>