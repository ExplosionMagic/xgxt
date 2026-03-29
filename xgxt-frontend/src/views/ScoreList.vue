<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
      <el-input v-model="searchParams.courseName" placeholder="按课程名称搜索" style="width: 200px;" clearable />
      <el-input v-if="user.role !== 'STUDENT'" v-model="searchParams.studentNo" placeholder="按学号精确搜索" style="width: 200px;" clearable />

      <el-button type="primary" @click="loadData">查询数据</el-button>

<!--      <el-button type="warning" @click="exportData" v-if="user.role !== 'STUDENT'">导出成绩单</el-button>-->
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="studentNo" label="学号" width="120" v-if="user.role !== 'STUDENT'" />
      <el-table-column prop="studentName" label="姓名" width="140" v-if="user.role !== 'STUDENT'" />

      <el-table-column prop="courseName" label="课程名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="credit" label="学分" width="80" align="center" />

      <el-table-column prop="score" label="最终成绩" width="120" align="center">
        <template #default="scope" style="font-size: 15px;">
          <span v-if="scope.row.score === null || scope.row.score === undefined">未出分</span>
          <span>{{ scope.row.score }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center" v-if="user.role !== 'STUDENT'" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" plain @click="openGradeDialog(scope.row)">
            {{ scope.row.score != null ? '修改成绩' : '录入成绩' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="gradeDialogVisible" :title="gradeForm.score != null ? '修改成绩' : '录入成绩'" width="350px">
      <el-form label-width="80px">
        <el-form-item label="当前学生">
          <el-input :value="gradeForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="最终成绩" required>
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" :precision="1" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const searchParams = ref({ courseName: '', studentNo: '' })
const tableData = ref([])

// 加载表格数据
const loadData = () => {
  // 无论什么角色，都无条件把 role 传给后端，满足后端的必填校验！
  searchParams.value.role = user.role

  // 针对不同角色，追加特殊查询条件
  if (user.role === 'STUDENT') {
    searchParams.value.studentNo = user.userNo
  } else if (user.role === 'TEACHER') {
    searchParams.value.teacherName = user.name
  }

  // 发送请求
  request.get('/score/list', { params: searchParams.value }).then(res => {
    tableData.value = res.data
  })
}

// ==== 教师打分逻辑 ====
const gradeDialogVisible = ref(false)
const gradeForm = ref({})

const openGradeDialog = (row) => {
  gradeForm.value = {
    id: row.id,
    studentName: row.studentName,
    score: row.score // 如果没分，这里是 null，弹窗里显示为空，方便老师直接输入
  }
  gradeDialogVisible.value = true
}

const submitGrade = () => {
  if (gradeForm.value.score === null || gradeForm.value.score === undefined) {
    ElMessage.warning('请输入具体分数')
    return
  }

  request.put('/score/grade', {
    id: gradeForm.value.id,
    score: gradeForm.value.score
  }).then(res => {
    ElMessage.success('成绩保存成功')
    gradeDialogVisible.value = false
    loadData()
  })
}

// ==== 导出成绩单功能 ====
const exportData = async () => {
  if (!user.token) {
    ElMessage.error('获取登录状态失败，请重新登录')
    return
  }

  ElMessage.info('正在生成成绩报表，请稍候...')

  try {
    const response = await fetch('http://localhost:8080/api/score/export', {
      method: 'GET',
      headers: {
        'Authorization': user.token // 请确保使用你项目里生效的通行证名字
      }
    })

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error('导出失败，后端拒绝访问')
      return
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生成绩报表.xlsx'
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

onMounted(() => {
  loadData()
})
</script>