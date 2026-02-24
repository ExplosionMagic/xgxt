<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
      <el-input v-model="searchParams.keyword" placeholder="搜姓名/学号" style="width: 200px;" clearable />

      <el-select v-model="searchParams.majorName" placeholder="全部专业" clearable style="width: 180px;" @change="handleMajorChangeSearch">
        <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
      </el-select>

      <el-select v-model="searchParams.className" placeholder="全部班级" clearable style="width: 180px;">
        <el-option v-for="item in searchClassList" :key="item.id" :label="item.className" :value="item.className" />
      </el-select>

      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="warning" @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="userNo" label="学号" width="100" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column prop="className" label="班级" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">学籍调整</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="学籍信息调整" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.userNo" disabled />
        </el-form-item>

        <el-form-item label="转专业" required>
          <el-select v-model="form.major" placeholder="选择专业" style="width: 100%;" @change="handleMajorChangeForm">
            <el-option v-for="item in majorList" :key="item.id" :label="item.majorName" :value="item.majorName" />
          </el-select>
        </el-form-item>

        <el-form-item label="调班级" required>
          <el-select v-model="form.className" placeholder="选择班级" style="width: 100%;">
            <el-option v-for="item in formClassList" :key="item.id" :label="item.className" :value="item.className" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const searchParams = ref({ keyword: '', majorName: '', className: '' })
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({})

// 存放所有专业和班级的数据
const majorList = ref([])
const allClassList = ref([])

// 存放联动过滤后的班级数据
const searchClassList = ref([]) // 搜索栏的班级下拉选项
const formClassList = ref([])   // 弹窗里的班级下拉选项

// 1. 加载字典数据 (专业和班级)
const loadDicts = async () => {
  const majorRes = await request.get('/major/list')
  majorList.value = majorRes.data

  const classRes = await request.get('/class_info/list')
  allClassList.value = classRes.data
}

// 2. 加载学生数据
const loadData = () => {
  request.get('/student/list', { params: searchParams.value }).then(res => {
    tableData.value = res.data
  })
}

// 3. 搜索栏联动：选中专业后，过滤出该专业的班级
const handleMajorChangeSearch = (majorName) => {
  searchParams.value.className = '' // 切换专业时，清空已选的班级
  if (majorName) {
    searchClassList.value = allClassList.value.filter(c => c.majorName === majorName)
  } else {
    searchClassList.value = []
  }
}

// 4. 弹窗联动：选中专业后，过滤出该专业的班级
const handleMajorChangeForm = (majorName) => {
  form.value.className = ''
  if (majorName) {
    formClassList.value = allClassList.value.filter(c => c.majorName === majorName)
  } else {
    formClassList.value = []
  }
}

const resetSearch = () => {
  searchParams.value = { keyword: '', majorName: '', className: '' }
  searchClassList.value = []
  loadData()
}

const handleEdit = (row) => {
  form.value = { ...row }
  // 打开弹窗时，先初始化班级下拉框（包含该学生原专业的班级）
  if (row.major) {
    formClassList.value = allClassList.value.filter(c => c.majorName === row.major)
  }
  dialogVisible.value = true
}

const save = () => {
  if (!form.value.major || !form.value.className) {
    ElMessage.warning('专业和班级不能为空')
    return
  }

  // 顺便把对应的年级查出来赋给学生
  const selectedClass = allClassList.value.find(c => c.className === form.value.className)
  if (selectedClass) {
    form.value.grade = selectedClass.grade
  }

  request.put('/student/update', form.value).then(res => {
    ElMessage.success('学籍信息更新成功')
    dialogVisible.value = false
    loadData()
  })
}

onMounted(async () => {
  await loadDicts() // 先加载专业班级字典
  loadData()        // 再加载表格数据
})
</script>