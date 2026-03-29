<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap;">
      <el-input v-model="searchName" placeholder="按姓名或账号搜索" style="width: 200px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">高级注册</el-button>
      <el-upload
          v-if="user.role !== 'STUDENT'"
          action="http://localhost:8080/api/user/import"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          accept=".xlsx, .xls"
          style="margin-right: 10px;"
      >
        <el-button type="warning" plain style="margin-left: 10px">导入报表（格式为.xlsx, .xls）</el-button>
      </el-upload>

      <el-button type="warning" plain @click="exportData" v-if="user.role !== 'STUDENT'">导出报表</el-button>
      <el-button type="info" plain @click="downloadTemplate" v-if="user.role !== 'STUDENT'" style="margin-right: 10px;">
        下载报表模板
      </el-button>
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%" :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
      <el-table-column prop="userNo" label="账号" width="100" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column prop="role" label="角色" width="100" align="center">
        <template #default="scope">
          <span v-if="scope.row.role === 'ADMIN'">管理员</span>
          <span v-else-if="scope.row.role === 'TEACHER'">教师</span>
          <span v-else>学生</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="major" label="专业" min-width="120" show-overflow-tooltip />
      <el-table-column prop="className" label="班级" min-width="120" show-overflow-tooltip />
      <el-table-column prop="status" label="账号状态" width="90" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="changeStatus(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户信息' : '高级注册'" width="450px" @closed="resetForm">
      <el-form ref="userFormRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="选择角色" :disabled="!!form.id" style="width: 100%;">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入11位手机号码" maxlength="11" />
        </el-form-item>

        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="form.password" type="password" placeholder="为空则不修改密码 (新增必填)" show-password />
        </el-form-item>

        <template v-if="form.role === 'STUDENT'">
          <el-form-item label="专业" prop="major">
            <el-select v-model="form.major" placeholder="选择专业" style="width: 100%;" @change="handleMajorChange">
              <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级" prop="className">
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
import { ref, onMounted, reactive, nextTick } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'

// 【修复核心】：在最外层提取 user 变量，供全局和模板使用
const user = JSON.parse(localStorage.getItem('student-user') || '{}')

const searchName = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})
const userFormRef = ref(null)

// 字典数据
const majorList = ref([])
const allClassList = ref([])
const dialogClassList = ref([]) // 弹窗中过滤后的班级

// 统一的表单校验规则
const rules = reactive({
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号码不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '格式错误！请输入正确的11位手机号', trigger: 'blur' }
  ],
  major: [{ required: true, message: '请选择专业', trigger: 'change' }],
  className: [{ required: true, message: '请选择班级', trigger: 'change' }]
})

// 加载表格数据
const loadData = () => {
  request.get('/user/list', { params: { name: searchName.value } }).then(res => {
    tableData.value = res.data
  })
}

// 加载专业和班级字典
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

// 新增按钮
const handleAdd = () => {
  form.value = { gender: '男', role: 'STUDENT', status: 1 }
  dialogClassList.value = []
  dialogVisible.value = true
  // 延迟清除以前的校验红字
  nextTick(() => {
    userFormRef.value?.clearValidate()
  })
}

// 编辑按钮
const handleEdit = (row) => {
  form.value = { ...row }
  form.value.password = ''

  if (row.major) {
    dialogClassList.value = allClassList.value.filter(c => c.majorName === row.major)
  } else {
    dialogClassList.value = []
  }

  dialogVisible.value = true
  nextTick(() => {
    userFormRef.value?.clearValidate()
  })
}

// 弹窗彻底关闭时触发
const resetForm = () => {
  userFormRef.value?.clearValidate()
}

// 提交保存
const save = () => {
  if (!userFormRef.value) return

  // 1. 如果是新增，强校验密码不能为空
  if (!form.value.id && !form.value.password) {
    ElMessage.warning('新增用户必须设置初始密码')
    return
  }

  // 2. 触发全局规则校验
  userFormRef.value.validate((valid) => {
    if (valid) {
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
    } else {
      ElMessage.warning('请检查带*的必填项是否有误！')
      return false
    }
  })
}

// 切换账号停用/启用状态
const changeStatus = (row) => {
  request.put('/user/admin-update', { id: row.id, status: row.status }).then(res => {
    ElMessage.success(row.status === 1 ? '账号已启用' : '账号已停用')
  })
}

// 导入报表
// 1. 动态获取 Token，组装上传请求的 Headers
const uploadHeaders = computed(() => {
  return {
    // 这里的 'Authorization' 必须和你在导出时查到的那个名字一模一样！
    'Authorization': user.token
  }
})

// 2. 上传成功的回调
const handleImportSuccess = (res) => {
  if (res.code === 200) { // 这里的 code 要看你后端的 Result 封装结构，一般是 200 或 0
    ElMessage.success('导入成功')
    loadData() // 导入成功后，自动刷新表格数据
  } else {
    ElMessage.error('导入失败')
  }
}

// 3. 上传失败的回调 (比如网络断开、后端报错等)
const handleImportError = (err) => {
  console.error('上传报错:', err)
  ElMessage.error('上传文件失败，请检查网络或后端配置')
}

// 导出报表（调用原生Fetch）
const exportData = async () => {
  if (!user.token) {
    ElMessage.error('获取登录状态失败，请重新登录')
    return
  }

  ElMessage.info('正在请求生成报表，请稍候...')

  try {
    const response = await fetch('http://localhost:8080/api/user/export', {
      method: 'GET',
      headers: {
        // 注意：这里用的是你之前代码里的 Authorization
        'Authorization': user.token
      }
    })

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error(errorData.msg || '导出失败，后端拒绝访问')
      return
    }

    const blob = await response.blob()

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '系统用户报表.xlsx'
    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('报表导出成功！')

  } catch (error) {
    console.error('导出失败详情:', error)
    ElMessage.error('网络请求失败，请检查后端服务')
  }
}

// 下载报表模板
const downloadTemplate = async () => {
  if (!user.token) {
    ElMessage.error('获取登录状态失败，请重新登录')
    return
  }

  try {
    const response = await fetch('http://localhost:8080/api/user/template', {
      method: 'GET',
      headers: {
        'Authorization': user.token // 确保和导出时使用的 Token 名称一致
      }
    })

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      ElMessage.error(errorData.msg || '模板下载失败，后端拒绝访问')
      return
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户信息导入模板.xlsx'
    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

  } catch (error) {
    console.error('下载模板失败详情:', error)
    ElMessage.error('网络请求失败，请检查后端服务')
  }
}

onMounted(() => {
  loadDicts()
  loadData()
})
</script>