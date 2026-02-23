<template>
  <div>
    <div style="margin-bottom: 20px">
      <el-button type="success" @click="handleAdd">录入成绩</el-button>
    </div>

    <!-- 成绩展示表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentName" label="学生姓名">
        <template #default="scope">
          {{ scope.row.studentName }} ({{ scope.row.studentNo }})
        </template>
      </el-table-column>
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="scoreNum" label="分数">
        <template #default="scope">
          <!-- 分数大于60显示绿色，小于60显示红色 -->
          <span :style="{ color: scope.row.scoreNum >= 60 ? 'green' : 'red', fontWeight: 'bold' }">
            {{ scope.row.scoreNum }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 录入成绩弹窗 -->
    <el-dialog v-model="dialogVisible" title="录入/修改成绩" width="40%">
      <el-form :model="form" label-width="100px">

        <!-- 下拉选择学生 -->
        <el-form-item label="选择学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 100%">
            <el-option
                v-for="item in studentOptions"
                :key="item.id"
                :label="item.name + ' (' + item.studentNo + ')'"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 下拉选择课程 -->
        <el-form-item label="选择课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
                v-for="item in courseOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 输入分数 -->
        <el-form-item label="考试分数">
          <el-input-number v-model="form.scoreNum" :min="0" :max="100" :precision="1" />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getScoreList, saveScore, deleteScore } from '../api/score'
import { getStudentList } from '../api/student'
import { getCourseList } from '../api/course' // 复用API
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const studentOptions = ref([]) // 学生下拉列表数据
const courseOptions = ref([])  // 课程下拉列表数据
const dialogVisible = ref(false)

const form = reactive({
  studentId: null,
  courseId: null,
  scoreNum: 0
})

// 1. 加载成绩列表
const loadTable = async () => {
  const res = await getScoreList()
  tableData.value = res.data
}

// 2. 加载下拉框的字典数据 (学生和课程)
const loadOptions = async () => {
  const sRes = await getStudentList()
  studentOptions.value = sRes.data

  const cRes = await getCourseList()
  courseOptions.value = cRes.data
}

const handleAdd = () => {
  form.studentId = null
  form.courseId = null
  form.scoreNum = 0
  dialogVisible.value = true
}

const submitForm = async () => {
  if(!form.studentId || !form.courseId) {
    ElMessage.warning('请选择学生和课程')
    return
  }
  await saveScore(form)
  ElMessage.success('成绩录入成功')
  dialogVisible.value = false
  loadTable() // 刷新列表
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除这条成绩记录吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteScore(id)
    ElMessage.success('删除成功')
    loadTable()
  })
}

// 页面挂载时，同时加载列表和下拉框数据
onMounted(() => {
  loadTable()
  loadOptions()
})
</script>