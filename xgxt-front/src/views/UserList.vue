<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchName" placeholder="按姓名或账号搜索" style="width: 200px; margin-right: 10px;" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">高级注册 (新增用户)</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="userNo" label="账号" width="100" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="role" label="角色">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 'TEACHER'" type="warning">教师</el-tag>
          <el-tag v-else>学生</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="className" label="班级" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="changeStatus(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户信息' : '高级注册'">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色" required>
          <el-select v-model="form.role" placeholder="选择角色" :disabled="!!form.id">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="form.password" type="password" placeholder="不填则不修改" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const searchName = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadData = () => {
  request.get('/user/list', { params: { name: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

const handleAdd = () => {
  form.value = { gender: '男', role: 'STUDENT', status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  form.value.password = '' // 编辑时清空密码框
  dialogVisible.value = true
}

const save = () => {
  if (form.value.id) {
    // 管理员修改其他人
    request.put('/user/admin-update', form.value).then(res => {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadData()
    })
  } else {
    // 高级注册
    request.post('/register', form.value).then(res => {
      ElMessage.success('账号创建成功')
      dialogVisible.value = false
      loadData()
    })
  }
}

const changeStatus = (row) => {
  request.put('/user/admin-update', { id: row.id, status: row.status }).then(res => {
    ElMessage.success(row.status === 1 ? '账号已启用' : '账号已停用')
  })
}

onMounted(() => {
  loadData()
})
</script>