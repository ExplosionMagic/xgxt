<template>
  <div class="timetable-wrapper">
    <el-tabs v-model="activeTab" type="border-card">

      <el-tab-pane label="我的课表" name="timetable">
        <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
          <span style="color: #666; font-size: 14px;">
            {{ user.role === 'STUDENT' ? '仅显示审核通过的课程' : '本学期课程安排' }}
          </span>
          <el-button type="primary" size="small" plain @click="loadTimetable">刷新课表</el-button>
        </div>

        <table class="timetable">
          <thead>
          <tr>
            <th style="width: 10%;">节次 \ 星期</th>
            <th v-for="day in weekDays" :key="day">{{ day }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="section in 5" :key="section">
            <td class="section-col">第 {{ section * 2 - 1 }}-{{ section * 2 }} 节</td>
            <td v-for="day in 7" :key="day" class="course-cell">
              <div v-if="getCourse(day, section)" class="course-card">
                <div class="course-name">{{ getCourse(day, section).courseName }}</div>
                <div class="course-meta">
                  <el-icon><Location /></el-icon> {{ getCourse(day, section).location }}
                </div>
                <div class="course-meta">
                  <el-icon><Avatar v-if="user.role === 'STUDENT'"/><Collection v-else/></el-icon>
                  {{ user.role === 'STUDENT' ? getCourse(day, section).teacherName : getCourse(day, section).majorName }}
                </div>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </el-tab-pane>

      <el-tab-pane label="选修课程" name="select" v-if="user.role === 'STUDENT'">
        <el-alert title="请根据您的专业规划选择课程，选修后需等待任课教师审核。" type="info" show-icon style="margin-bottom: 15px;" />

        <el-table :data="availableCourses" border stripe>
          <el-table-column prop="courseNo" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="teacherName" label="任课教师" width="120" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column label="上课时间" width="180">
            <template #default="scope">
              周{{ scope.row.dayOfWeek }} 第{{ scope.row.section * 2 - 1 }}-{{ scope.row.section * 2 }}节
            </template>
          </el-table-column>
          <el-table-column prop="location" label="上课地点" width="150" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button type="success" size="small" @click="handleSelectCourse(scope.row.id)">申请选修</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="申请记录" name="records" v-if="user.role === 'STUDENT'">
        <el-table :data="myRecords" border stripe>
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="teacherName" label="任课教师" width="120" />
          <el-table-column prop="status" label="审核状态" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
              <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
              <el-tag v-else-if="scope.row.status === 2" type="danger">已驳回</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.status === 2" type="primary" size="small" @click="handleResubmit(scope.row)">重新申请</el-button>
              <el-button v-if="scope.row.score === null" type="danger" size="small" @click="handleDropCourse(scope.row.id)">退课/撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { Location, Avatar, Collection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const activeTab = ref('timetable')
const weekDays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']

// 数据源
const courseList = ref([])         // 课表数据
const availableCourses = ref([])   // 可选课程数据
const myRecords = ref([])          // 我的选课记录数据

// 1. 加载课表
const loadTimetable = () => {
  request.get('/course/timetable', { params: { userNo: user.userNo, role: user.role } }).then(res => {
    courseList.value = res.data
  })
}
const getCourse = (day, section) => courseList.value.find(c => c.dayOfWeek === day && c.section === section)

// 2. 加载选课中心 (学生专享)
const loadAvailableCourses = () => {
  if (user.role !== 'STUDENT') return
  request.get(`/user/info/${user.userNo}`).then(userRes => {
    const studentMajor = userRes.data.major
    if (studentMajor) {
      request.get('/course/list', { params: { majorName: studentMajor } }).then(res => {
        availableCourses.value = res.data
      })
    }
  })
}

// 3. 加载选课记录 (学生专享)
const loadMyRecords = () => {
  if (user.role !== 'STUDENT') return
  request.get('/score/list', { params: { studentNo: user.userNo } }).then(res => {
    myRecords.value = res.data
  })
}

// 4. 申请选修
const handleSelectCourse = (courseId) => {
  request.post('/score/selectCourse', { studentNo: user.userNo, courseId: courseId }).then(res => {
    ElMessage.success(res.msg || '选课申请已提交')
    loadMyRecords() // 刷新记录
    activeTab.value = 'records' // 自动跳转到记录页签
  })
}

// 5. 重新提交被驳回的申请
const handleResubmit = (row) => {
  request.post('/score/selectCourse', { studentNo: user.userNo, courseId: row.courseId }).then(res => {
    ElMessage.success(res.msg || '已重新提交')
    loadMyRecords()
  })
}

// 6. 退课 / 撤销
const handleDropCourse = (id) => {
  ElMessageBox.confirm('确定要退选这门课或撤销申请吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'}).then(() => {
    request.delete(`/score/${id}`).then(res => {
      ElMessage.success('操作成功')
      loadMyRecords()
      loadTimetable() // 退课后同步刷新课表
    })
  })
}

onMounted(() => {
  loadTimetable()
  loadAvailableCourses()
  loadMyRecords()
})
</script>

<style scoped>
/* 课表网格样式 */
.timetable { width: 100%; border-collapse: collapse; text-align: center; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #ebeef5; padding: 10px; }
.timetable th { background-color: #f5f7fa; color: #606266; font-weight: bold; }
.section-col { background-color: #fafafa; color: #909399; font-size: 14px; }
.course-cell { height: 100px; vertical-align: top; }
.course-card { background-color: #ecf5ff; border-left: 4px solid #409EFF; border-radius: 4px; padding: 8px; text-align: left; height: 100%; box-sizing: border-box; }
.course-name { font-weight: bold; color: #303133; margin-bottom: 5px; font-size: 14px; }
.course-meta { font-size: 12px; color: #666; display: flex; align-items: center; gap: 4px; margin-top: 4px; }
</style>