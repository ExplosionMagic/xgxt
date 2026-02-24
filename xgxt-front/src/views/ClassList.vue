<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchName" placeholder="按班级或专业搜索" style="width: 200px; margin-right: 10px;" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增班级</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="className" label="班级名称" />
      <el-table-column prop="grade" label="年级" width="120" />
      <el-table-column prop="majorName" label="所属专业" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="班级名称" required>
          <el-input v-model="form.className" placeholder="例如：软件1班" />
        </el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
            <el-option label="2021级" value="2021级" />
            <el-option label="2022级" value="2022级" />
            <el-option label="2023级" value="2023级" />
            <el-option label="2024级" value="2024级" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业" required>
          <el-select v-model="form.majorName" placeholder="请选择专业" style="width: 100%;">
            <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
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
const tableData = ref([])
const majorList = ref([]) // 存放专业列表
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/class_info/list', { params: { keyword: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

// 加载所有专业（供下拉框使用）
const loadMajors = () => {
  request.get('/major/list').then(res => {
    majorList.value = res.data
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
  if (!form.value.className || !form.value.grade || !form.value.majorName) {
    ElMessage.warning('请将必填信息填写完整')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/class_info', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个班级吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/class_info/${id}`).then(res => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
  loadData()
  loadMajors() // 页面加载时一并获取专业列表
})
</script>