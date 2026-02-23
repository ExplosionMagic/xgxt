<template>
  <div>
    <!-- 顶部操作栏 -->
    <div style="margin-bottom: 20px">
      <el-input v-model="searchName" placeholder="请输入学生姓名" style="width: 200px; margin-right: 10px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="handleAdd">新增学生</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="studentNo" label="学号" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="grade" label="年级" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" title="学生信息">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学号"> <el-input v-model="form.studentNo" /> </el-form-item>
        <el-form-item label="姓名"> <el-input v-model="form.name" /> </el-form-item>
        <el-form-item label="专业"> <el-input v-model="form.major" /> </el-form-item>
        <el-form-item label="年级"> <el-input v-model="form.grade" /> </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchName = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({})

// 加载数据
const loadData = () => {
  request.get('/student/list', { params: { name: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

// 新增按钮
const handleAdd = () => {
  Object.keys(form).forEach(key => form[key] = '') // 清空表单
  delete form.id
  dialogVisible.value = true
}

// 编辑按钮
const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

// 保存（新增或更新）
const save = () => {
  const method = form.id ? 'put' : 'post'
  request[method]('/student', form).then(res => {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除吗?', '提示', { type: 'warning' }).then(() => {
    request.delete(`/student/${id}`).then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>