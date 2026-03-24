<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchTitle" placeholder="按标题搜索" style="width: 200px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">发布新公告</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="targetType" label="发送范围" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.targetType === 'ALL'" type="danger">全校可见</el-tag>
          <el-tag v-else-if="scope.row.targetType === 'MAJOR'" type="warning">特定专业</el-tag>
          <el-tag v-else type="success">特定班级</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetValue" label="具体接收方" width="150">
        <template #default="scope">{{ scope.row.targetValue || '--' }}</template>
      </el-table-column>
      <el-table-column prop="publisher" label="发布人" width="100" />
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布新公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="发送范围" required>
          <el-radio-group v-model="form.targetType" @change="handleTypeChange">
            <el-radio label="ALL">全校</el-radio>
            <el-radio label="MAJOR">指定专业</el-radio>
            <el-radio label="CLASS">指定班级</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择专业" v-if="form.targetType === 'MAJOR'" required>
          <el-select v-model="form.targetValue" placeholder="请选择专业" style="width: 100%;">
            <el-option v-for="m in majorList" :key="m.id" :label="m.majorName" :value="m.majorName" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择班级" v-if="form.targetType === 'CLASS'" required>
          <el-select v-model="form.targetValue" placeholder="请选择班级" style="width: 100%;">
            <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.className" />
          </el-select>
        </el-form-item>

        <el-form-item label="正文内容" required>
          <el-input type="textarea" v-model="form.content" :rows="8" placeholder="请输入正文内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchTitle = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})
const user = JSON.parse(localStorage.getItem('student-user') || '{}')

// 字典数据
const majorList = ref([])
const classList = ref([])

const loadDicts = async () => {
  const mRes = await request.get('/major/list')
  majorList.value = mRes.data
  const cRes = await request.get('/class_info/list')
  classList.value = cRes.data
}

const loadData = () => {
  request.get('/announcement/list', { params: { title: searchTitle.value } }).then(res => {
    tableData.value = res.data
  })
}

const handleTypeChange = () => {
  form.value.targetValue = '' // 切换范围时清空具体选择
}

const handleAdd = () => {
  form.value = { targetType: 'ALL', publisher: user.name }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const save = () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('标题和正文不能为空')
    return
  }
  if (form.value.targetType !== 'ALL' && !form.value.targetValue) {
    ElMessage.warning('请选择具体的接收专业或班级')
    return
  }
  request.post('/announcement', form.value).then(res => {
    ElMessage.success('发布成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此公告吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'}).then(() => {
    request.delete(`/announcement/${id}`).then(res => {
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