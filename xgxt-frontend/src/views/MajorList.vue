<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchName" placeholder="按专业名称搜索" style="width: 200px; margin-right: 10px;" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增专业</el-button>
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="majorName" label="专业名称" min-width="120" />
      <el-table-column prop="college" label="所属学院" min-width="120" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专业' : '新增专业'" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="专业名称" required>
          <el-input v-model="form.majorName" placeholder="例如：软件工程" />
        </el-form-item>
        <el-form-item label="所属学院">
          <el-input v-model="form.college" placeholder="例如：计算机学院" />
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
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/major/list', { params: { majorName: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

const handleAdd = () => {
  form.value = {}
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const save = () => {
  if (!form.value.majorName) {
    ElMessage.warning('专业名称不能为空')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/major', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个专业吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/major/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>