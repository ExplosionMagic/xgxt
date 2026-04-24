<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
      <el-input v-model="searchParams.majorName" placeholder="按专业名称搜索" style="width: 200px;" clearable />

      <el-select v-model="searchParams.collegeName" placeholder="按学院筛选" clearable style="width: 200px;">
        <el-option v-for="c in collegeList" :key="c.id" :label="c.collegeName" :value="c.collegeName" />
      </el-select>

      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增专业</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%;" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="id" label="ID" width="80" align="center" />

      <el-table-column prop="collegeName" label="所属学院" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.collegeName || '--未分配--' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="majorName" label="专业名称" min-width="180" />
      <el-table-column prop="majorCode" label="专业代码" width="120" align="center" />

      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专业' : '新增专业'" width="450px">
      <el-form :model="form" label-width="100px" @submit.prevent>

        <el-form-item label="所属学院" required>
          <el-select v-model="form.collegeName" placeholder="请选择所属学院" style="width: 100%;">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.collegeName" :value="c.collegeName" />
          </el-select>
        </el-form-item>

        <el-form-item label="专业名称" required>
          <el-input v-model="form.majorName" placeholder="例如：软件工程" />
        </el-form-item>
        <el-form-item label="专业代码">
          <el-input v-model="form.majorCode" placeholder="例如：080902" @keyup.enter="save" />
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

const searchParams = ref({ majorName: '', collegeName: '' })
const tableData = ref([])
const collegeList = ref([]) // 存放学院下拉数据
const dialogVisible = ref(false)
const form = ref({})

// 加载专业列表
const loadData = () => {
  request.get('/major/list', { params: searchParams.value }).then(res => {
    tableData.value = res.data
  })
}

// 获取字典数据：加载所有学院，供下拉框使用
const loadDicts = () => {
  request.get('/college/list').then(res => {
    collegeList.value = res.data
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
  if (!form.value.collegeName || !form.value.majorName) {
    ElMessage.warning('请选择所属学院并填写专业名称')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/major', form.value).then(res => {
    ElMessage.success(res.msg || '保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此专业吗？', '警告', { type: 'warning' }).then(() => {
    request.delete(`/major/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadDicts() // 页面加载时先拉取学院列表
  loadData()
})
</script>