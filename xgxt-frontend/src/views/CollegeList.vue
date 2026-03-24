<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchName" placeholder="按学院名称搜索" style="width: 200px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增学院</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%;">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="collegeName" label="学院名称" min-width="200">
        <template #default="scope">
          <span>{{ scope.row.collegeName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="dean" label="院长/负责人" width="150" align="center" />
<!--      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />-->
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学院' : '新增学院'" width="450px">
      <el-form :model="form" label-width="100px" @submit.prevent>
        <el-form-item label="学院名称" required>
          <el-input v-model="form.collegeName" placeholder=" " />
        </el-form-item>
        <el-form-item label="院长/负责人">
          <el-input v-model="form.dean" placeholder="请输入负责人姓名" @keyup.enter="save" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定保存</el-button>
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
  request.get('/college/list', { params: { collegeName: searchName.value } }).then(res => {
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
  if (!form.value.collegeName) {
    ElMessage.warning('请输入学院名称')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/college', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此学院吗？请确保该学院下没有关联的专业。', '警告', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }).then(() => {
    request.delete(`/college/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
})
</script>