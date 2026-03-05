<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchName" placeholder="按课程名称搜索" style="width: 200px;" clearable />
      <el-select v-model="searchMajor" placeholder="按专业筛选" clearable style="width: 200px;">
        <el-option v-for="m in majorList" :key="m.id" :label="m.majorName" :value="m.majorName" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增课程</el-button>
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%">
      <el-table-column prop="courseNo" label="课程代码" width="120" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="majorName" label="所属专业" />
      <el-table-column prop="credit" label="学分" width="80" />
      <el-table-column prop="teacherName" label="任课教师" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="课程代码" required>
          <el-input v-model="form.courseNo" placeholder="例如：CS101" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="课程名称" required>
          <el-input v-model="form.courseName" placeholder="例如：高等数学" />
        </el-form-item>

        <el-form-item label="所属专业" required>
          <el-select v-model="form.majorName" placeholder="请选择该课程所属专业" style="width: 100%;">
            <el-option v-for="m in majorList" :key="m.id" :label="m.majorName" :value="m.majorName" />
          </el-select>
        </el-form-item>

        <el-form-item label="学分" required>
          <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="任课教师" required>
          <el-select v-model="form.teacherName" placeholder="请选择任课教师" style="width: 100%;">
            <el-option v-for="t in teacherList" :key="t.id" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchName = ref('')
const searchMajor = ref('')
const tableData = ref([])
const teacherList = ref([])
const majorList = ref([]) // 存放专业名单
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/course/list', {
    params: { courseName: searchName.value, majorName: searchMajor.value }
  }).then(res => {
    tableData.value = res.data
  })
}

// 加载字典数据：教师和专业
const loadDicts = async () => {
  const tRes = await request.get('/user/list')
  teacherList.value = tRes.data.filter(u => u.role === 'TEACHER')

  const mRes = await request.get('/major/list')
  majorList.value = mRes.data
}

const handleAdd = () => {
  form.value = { credit: 2.0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const save = () => {
  if (!form.value.courseNo || !form.value.courseName || !form.value.teacherName || !form.value.majorName) {
    ElMessage.warning('请填写所有必填信息')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/course', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这门课程吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/course/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadDicts()
  loadData()
})
</script>