<template>
  <div>
    <div style="margin-bottom: 20px">
      <el-button type="primary" @click="handleAdd">新建课程</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="课程名称" />
      <el-table-column prop="teacherName" label="任课教师" />
      <el-table-column prop="credit" label="学分" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" title="课程信息" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="任课教师">
          <el-input v-model="form.teacherName" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="form.credit" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCourseList, saveCourse, deleteCourse } from '../api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ name: '', teacherName: '', credit: 2 })

const loadData = async () => {
  const res = await getCourseList()
  tableData.value = res.data
}

const handleAdd = () => {
  form.id = undefined // 清除ID表示新增
  form.name = ''
  form.teacherName = ''
  form.credit = 2
  dialogVisible.value = true
}

const submitForm = async () => {
  await saveCourse(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteCourse(id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => loadData())
</script>