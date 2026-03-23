<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchName" placeholder="按年级名称搜索" style="width: 200px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增年级</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%; max-width: 800px;">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="gradeName" label="年级名称" align="center">
        <template #default="scope">
          <el-tag size="large" type="primary">{{ scope.row.gradeName }}</el-tag>
        </template>
      </el-table-column>
<!--      <el-table-column prop="createTime" label="创建时间" align="center" />-->
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑年级' : '新增年级'" width="400px">
      <el-form :model="form" label-width="80px" @submit.prevent>
        <el-form-item label="年级名称" required>
          <el-input v-model="form.gradeName" placeholder="例如：2024级" @keyup.enter="save" />
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
  request.get('/grade/list', { params: { gradeName: searchName.value } }).then(res => {
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
  if (!form.value.gradeName) {
    ElMessage.warning('请输入年级名称')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/grade', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此年级吗？请确保没有关联的班级或课程。', '警告', { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }).then(() => {
    request.delete(`/grade/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>