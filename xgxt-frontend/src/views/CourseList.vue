<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
      <el-input v-model="searchName" placeholder="按课程名称搜索" style="width: 200px;" clearable />
      <el-select v-model="searchMajor" placeholder="按专业筛选" clearable style="width: 200px;">
        <el-option v-for="m in majorList" :key="m.id" :label="m.majorName" :value="m.majorName" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增课程</el-button>
    </div>

    <el-table :data="tableData" border stripe fit style="width: 100%">
      <el-table-column prop="courseNo" label="课程代码" width="100" />
      <el-table-column prop="courseName" label="课程名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="majorName" label="所属专业" width="150" show-overflow-tooltip />
      <el-table-column prop="nature" label="课程性质" width="140" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.nature === '必修' ? 'danger' : 'warning'">{{ scope.row.nature || '选修' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetGrade" label="年级" width="100" align="center">
        <template #default="scope">{{ scope.row.targetGrade || '--' }}</template>
      </el-table-column>
      <el-table-column label="上课时间" width="160" align="center">
        <template #default="scope">
          <span v-if="scope.row.dayOfWeek">周{{ scope.row.dayOfWeek }} 第{{ scope.row.section * 2 - 1 }}-{{ scope.row.section * 2 }}节</span>
          <span v-else style="color: #999;">未排课</span>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="上课地点" width="120" show-overflow-tooltip />
      <el-table-column prop="teacherName" label="上课教师" width="100" />
      <el-table-column label="已选/容量" width="100" align="center">
        <template #default="scope">
          <span :style="{ color: scope.row.enrolled >= scope.row.capacity ? 'red' : '#67C23A', fontWeight: 'bold' }">
            {{ scope.row.enrolled || 0 }} / {{ scope.row.capacity || 50 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="650px">
      <el-form :model="form" label-width="100px" style="display: flex; flex-wrap: wrap; justify-content: space-between;">

        <div style="width: 48%;">
          <el-form-item label="课程代码" required>
            <el-input v-model="form.courseNo" placeholder="例如：CS101" :disabled="!!form.id" />
          </el-form-item>
          <el-form-item label="课程名称" required>
            <el-input v-model="form.courseName" placeholder="例如：高等数学" />
          </el-form-item>
          <el-form-item label="所属专业" required>
            <el-select v-model="form.majorName" placeholder="选择专业" style="width: 100%;">
              <el-option v-for="m in majorList" :key="m.id" :label="m.majorName" :value="m.majorName" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程性质" required>
            <el-radio-group v-model="form.nature">
              <el-radio label="必修">必修</el-radio>
              <el-radio label="选修">选修</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="推荐年级" required>
            <el-select v-model="form.targetGrade" placeholder="选择年级" style="width: 100%;">
              <el-option v-for="g in gradeList" :key="g.id" :label="g.gradeName" :value="g.gradeName" />
            </el-select>
          </el-form-item>
        </div>

        <div style="width: 48%;">
          <el-form-item label="任课教师" required>
            <el-select v-model="form.teacherName" placeholder="选择教师" style="width: 100%;">
              <el-option v-for="t in teacherList" :key="t.id" :label="t.name" :value="t.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="学分" required>
            <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="课程容量" required>
            <el-input-number v-model="form.capacity" :min="10" :max="300" :step="10" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="上课时间" required>
            <div style="display: flex; gap: 5px; width: 100%;">
              <el-select v-model="form.dayOfWeek" placeholder="星期" style="flex: 1;">
                <el-option label="周一" :value="1" />
                <el-option label="周二" :value="2" />
                <el-option label="周三" :value="3" />
                <el-option label="周四" :value="4" />
                <el-option label="周五" :value="5" />
                <el-option label="周六" :value="6" />
                <el-option label="周日" :value="7" />
              </el-select>
              <el-select v-model="form.section" placeholder="节次" style="flex: 1;">
                <el-option label="1-2节" :value="1" />
                <el-option label="3-4节" :value="2" />
                <el-option label="5-6节" :value="3" />
                <el-option label="7-8节" :value="4" />
                <el-option label="9-10节" :value="5" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="上课地点" required>
            <el-input v-model="form.location" placeholder="例如：A楼301" />
          </el-form-item>
        </div>

        <div style="width: 100%; margin-top: 10px;">
          <el-form-item label="备注说明">
            <el-input type="textarea" v-model="form.remark" :rows="2" placeholder="填写课程相关的特殊要求或说明（选填）" />
          </el-form-item>
        </div>

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
const searchMajor = ref('')
const tableData = ref([])
const teacherList = ref([])
const majorList = ref([])
const dialogVisible = ref(false)
const form = ref({})
const gradeList = ref([]) // 存放动态年级数据

const loadData = () => {
  request.get('/course/list', {
    params: { courseName: searchName.value, majorName: searchMajor.value }
  }).then(res => {
    tableData.value = res.data
  })
}

const loadDicts = async () => {
  const tRes = await request.get('/user/list')
  teacherList.value = tRes.data.filter(u => u.role === 'TEACHER')
  const mRes = await request.get('/major/list')
  majorList.value = mRes.data
  const gRes = await request.get('/grade/list')
  gradeList.value = gRes.data
}

const handleAdd = () => {
  // 赋初始默认值，方便快速录入
  form.value = {
    credit: 2.0,
    nature: '选修',
    capacity: 50,
    enrolled: 0,
    dayOfWeek: 1,
    section: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const save = () => {
  // 校验新增的必填字段
  if (!form.value.courseNo || !form.value.courseName || !form.value.teacherName ||
      !form.value.majorName || !form.value.targetGrade || !form.value.location ||
      !form.value.dayOfWeek || !form.value.section) {
    ElMessage.warning('请将带*的必填信息填写完整')
    return
  }
  const method = form.value.id ? 'put' : 'post'
  request[method]('/course', form.value).then(res => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这门课程吗？', '警告', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'}).then(() => {
    request.delete(`/course/${id}`).then(res => {
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

<style scoped>
/* 可以在此调整表单等元素的样式 */
.el-form-item {
  margin-bottom: 18px;
}
</style>