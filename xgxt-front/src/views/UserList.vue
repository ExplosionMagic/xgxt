<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchName" placeholder="按姓名或账号搜索" style="width: 200px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">高级注册 (新增用户)</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="userNo" label="账号" width="100" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 'TEACHER'" type="warning">教师</el-tag>
          <el-tag v-else>学生</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="className" label="班级" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="changeStatus(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户信息' : '高级注册'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色" required>
          <el-select v-model="form.role" placeholder="选择角色" :disabled="!!form.id" style="width: 100%;">
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

        <template v-if="form.role == 'STUDENT'">
          <el-form-item label="专业">
            <el-select v-model="form.major" placeholder="选择专业" style="width: 100%;" @change="handleMajorChange">
              <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="form.className" placeholder="选择班级" style="width: 100%;">
              <el-option v-for="item in dialogClassList" :key="item.id" :label="item.className" :value="item.className" />
            </el-select>
          </el-form-item>
        </template>
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

// 字典数据
const majorList = ref([])
const allClassList = ref([])
const dialogClassList = ref([]) // 弹窗中过滤后的班级

const loadData = () => {
  request.get('/user/list', { params: { name: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

const loadDicts = async () => {
  const majorRes = await request.get('/major/list')
  majorList.value = majorRes.data
  const classRes = await request.get('/class_info/list')
  allClassList.value = classRes.data
}

// 弹窗里的联动过滤
const handleMajorChange = (majorName) => {
  form.value.className = ''
  dialogClassList.value = allClassList.value.filter(c => c.majorName === majorName)
}

const handleAdd = () => {
  form.value = { gender: '男', role: 'STUDENT', status: 1 }
  dialogClassList.value = [] // 新增时清空班级选项
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  form.value.password = ''

  // 编辑时，根据用户现有的专业，提前加载好对应的班级列表
  if (row.major) {
    dialogClassList.value = allClassList.value.filter(c => c.majorName === row.major)
  } else {
    dialogClassList.value = []
  }

  dialogVisible.value = true
}

const save = () => {
  if (!form.value.name || !form.value.phone) {
    ElMessage.warning('姓名和手机号不能为空')
    return
  }

  // 匹配自动获取年级
  if (form.value.className) {
    const selectedClass = allClassList.value.find(c => c.className === form.value.className)
    if (selectedClass) {
      form.value.grade = selectedClass.grade
    }
  }

  if (form.value.id) {
    request.put('/user/admin-update', form.value).then(res => {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadData()
    })
  } else {
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
  loadDicts()
  loadData()
})
</script>