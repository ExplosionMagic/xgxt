<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchParams.courseName" placeholder="按课程名称搜索" style="width: 200px;" clearable />
      <el-input v-if="user.role !== 'STUDENT'" v-model="searchParams.studentNo" placeholder="按学号精确搜索" style="width: 200px;" clearable />

      <el-button type="primary" @click="loadData">搜索</el-button>

      <el-button v-if="user.role === 'STUDENT'" type="success" @click="openSelectCourseDialog">我要选课</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="major" label="专业" width="150" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="teacherName" label="任课教师" width="100" />
      <el-table-column prop="credit" label="学分" width="60" align="center" />

      <el-table-column prop="status" label="选修状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">已驳回</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="score" label="成绩" width="100" align="center">
        <template #default="scope">
          <span v-if="scope.row.status !== 1" style="color: #999;">--</span>
          <el-tag v-else-if="scope.row.score === null" type="info">未出分</el-tag>
          <el-tag v-else :type="scope.row.score >= 60 ? 'success' : 'danger'">
            <span style="font-weight: bold; font-size: 14px;">{{ scope.row.score }}</span>
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button v-if="user.role === 'STUDENT' && scope.row.score === null" type="danger" size="small" @click="handleDelete(scope.row.id)">退课/撤销</el-button>

          <el-button v-if="user.role !== 'STUDENT' && scope.row.status === 1" type="primary" size="small" @click="openGradeDialog(scope.row)">录入成绩</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="courseDialogVisible" title="选择课程" width="400px">
      <el-form label-width="80px">
        <el-form-item label="选择课程">
          <el-select v-model="selectedCourseId" placeholder="请选择要选修的课程" style="width: 100%;">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.courseName} (${c.teacherName}老师 - ${c.credit}学分)`" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSelectCourse">确认选修</el-button>
      </template>
    </el-dialog>

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
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const searchParams = ref({ courseName: '', studentNo: '' })
const tableData = ref([])

// 页面加载获取数据
const loadData = () => {
  // 如果是学生，强制只查自己的学号
  if (user.role === 'STUDENT') {
    searchParams.value.studentNo = user.userNo
  }
  if (user.role === 'TEACHER') {
    searchParams.teacherName = user.name
  }
  request.get('/score/list', { params: searchParams.value }).then(res => {
    tableData.value = res.data
  })
}

// ==== 学生选课逻辑 ====
const courseDialogVisible = ref(false)
const courseList = ref([])
const selectedCourseId = ref(null)

const openSelectCourseDialog = () => {
  // 由于刚分班的学生 localStorage 里可能没有最新 major
  // 为了绝对准确，我们先去查一下学生最新的个人信息
  request.get(`/user/info/${user.userNo}`).then(userRes => {
    const studentMajor = userRes.data.major;

    if (!studentMajor) {
      ElMessage.warning('您尚未分配专业，无法选课');
      return;
    }

    // 然后带着该学生的专业去请求课程列表
    request.get('/course/list', { params: { majorName: studentMajor } }).then(res => {
      courseList.value = res.data

      if (courseList.value.length === 0) {
        ElMessage.info('当前专业暂无可修课程');
      }

      selectedCourseId.value = null
      courseDialogVisible.value = true
    })
  })
}

const submitSelectCourse = () => {
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择一门课程')
    return
  }
  request.post('/score/selectCourse', {
    studentNo: user.userNo,
    courseId: selectedCourseId.value
  }).then(res => {
    ElMessage.success('选课成功')
    courseDialogVisible.value = false
    loadData()
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

// ==== 退课逻辑 ====
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要退选这门课吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/score/${id}`).then(res => {
      ElMessage.success('退课成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>