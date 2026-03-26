<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchParams.courseName" placeholder="按课程名称搜索" style="width: 200px;" clearable />
      <el-input v-if="user.role !== 'STUDENT'" v-model="searchParams.studentNo" placeholder="按学号精确搜索" style="width: 200px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="140" />
<!--      <el-table-column prop="major" label="专业" width="150" />-->
      <el-table-column prop="courseName" label="课程名称" />
<!--      <el-table-column prop="teacherName" label="上课教师" width="120" />-->
      <el-table-column prop="credit" label="学分" width="80" align="center" />

      <el-table-column prop="score" label="最终成绩" width="120" align="center">
        <template #default="scope">
          <span v-if="scope.row.status !== 1" style="color: #999;">尚未入班</span>
          <el-tag v-else-if="scope.row.score === null" type="info">未出分</el-tag>
          <el-tag v-else :type="scope.row.score >= 60 ? 'success' : 'danger'">
            <span style="font-weight: bold; font-size: 14px;">{{ scope.row.score }}</span>
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center" v-if="user.role !== 'STUDENT'">
        <template #default="scope">
          <el-button v-if="scope.row.status === 1" type="primary" size="small" @click="openGradeDialog(scope.row)">录入成绩</el-button>
          <span v-else style="color: #ccc; font-size: 12px;">无法打分</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="gradeDialogVisible" title="录入成绩" width="300px">
      <el-form label-width="80px">
        <el-form-item label="当前学生">
          <el-input :value="gradeForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="最终成绩">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">确认打分</el-button>
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

const loadData = () => {
  if (user.role === 'STUDENT') {
    searchParams.value.studentNo = user.userNo
  }
  if (user.role === 'TEACHER') {
    searchParams.teacherName = user.name
  }
  // 成绩模块不再需要看“待审核”的数据，强制只查状态为1(已通过)的数据
  request.get('/score/list', { params: { ...searchParams.value, status: 1 } }).then(res => {
    tableData.value = res.data
  })
}

// ==== 教师打分逻辑 ====
const gradeDialogVisible = ref(false)
const gradeForm = ref({})

const openGradeDialog = (row) => {
  gradeForm.value = { id: row.id, studentName: row.studentName, score: row.score || 0 }
  gradeDialogVisible.value = true
}

const submitGrade = () => {
  request.put('/score/grade', {
    id: gradeForm.value.id,
    score: gradeForm.value.score
  }).then(res => {
    ElMessage.success('成绩录入成功')
    gradeDialogVisible.value = false
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>