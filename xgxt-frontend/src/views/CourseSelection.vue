<template>
  <div class="selection-container">
    <el-tabs v-model="activeTab" type="border-card">

      <el-tab-pane label="推荐课表选课" name="recommend">
        <el-alert title="系统已为您生成专属专业推荐课表，点击课程区块即可选课。" type="success" show-icon style="margin-bottom: 15px;" />

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
            <td v-for="day in 7" :key="day" class="course-cell" @click="handleRecommendSelect(getRecommendCourse(day, section))">
              <div v-if="getRecommendCourse(day, section)" class="course-card recommend-card">
                <div class="course-name">{{ getRecommendCourse(day, section).courseName }}</div>
                <div class="course-meta">
                  <el-tag size="small" :type="getRecommendCourse(day, section).nature === '必修' ? 'danger' : 'warning'">
                    {{ getRecommendCourse(day, section).nature }}
                  </el-tag>
                </div>
                <div class="course-meta" style="margin-top: 5px;">
                    <span :style="{ color: getRecommendCourse(day, section).enrolled >= getRecommendCourse(day, section).capacity ? 'red' : '#67C23A', fontWeight: 'bold' }">
                      余量: {{ getRecommendCourse(day, section).capacity - getRecommendCourse(day, section).enrolled }}
                    </span>
                </div>
              </div>
              <div v-else class="empty-cell"> </div>
            </td>
          </tr>
          </tbody>
        </table>
      </el-tab-pane>

      <el-tab-pane label="自主选课" name="independent">
        <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
          <el-select v-model="searchParams.nature" placeholder="课程性质" clearable style="width: 120px;">
            <el-option label="必修" value="必修" />
            <el-option label="选修" value="选修" />
          </el-select>
          <el-select v-model="searchParams.dayOfWeek" placeholder="星期几" clearable style="width: 120px;">
            <el-option v-for="(day, index) in weekDays" :key="index" :label="day" :value="index + 1" />
          </el-select>
          <el-select v-model="searchParams.section" placeholder="节次" clearable style="width: 120px;">
            <el-option label="第1-2节" :value="1" />
            <el-option label="第3-4节" :value="2" />
            <el-option label="第5-6节" :value="3" />
            <el-option label="第7-8节" :value="4" />
            <el-option label="第9-10节" :value="5" />
          </el-select>
          <el-checkbox v-model="searchParams.hasCapacity" border>只看有余量</el-checkbox>
          <el-button type="primary" @click="searchCourses">筛选</el-button>
        </div>

        <el-table :data="independentCourses" border stripe highlight-current-row>
          <el-table-column prop="courseNo" label="课程代码" width="90" />
          <el-table-column prop="courseName" label="课程名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="majorName" label="所属专业" width="140" show-overflow-tooltip />
          <el-table-column prop="nature" label="课程性质" width="140" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.nature === '必修' ? 'danger' : 'warning'">{{ scope.row.nature }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上课时间" width="140" align="center">
            <template #default="scope">周{{ scope.row.dayOfWeek }} 第{{ scope.row.section * 2 - 1 }}-{{ scope.row.section * 2 }}节</template>
          </el-table-column>
          <el-table-column prop="teacherName" label="上课教师" width="90" />
          <el-table-column prop="location" label="上课地点" width="110" show-overflow-tooltip />
          <el-table-column label="余量" width="80" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.enrolled >= scope.row.capacity ? 'red' : '#67C23A', fontWeight: 'bold' }">
                {{ scope.row.capacity - scope.row.enrolled }} / {{ scope.row.capacity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="scope">
              <el-button
                  type="primary"
                  size="small"
                  :disabled="scope.row.enrolled >= scope.row.capacity"
                  @click="executeSelectCourse(scope.row)">
                {{ scope.row.enrolled >= scope.row.capacity ? '已满' : '选课' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="选课记录" name="records">
        <el-alert title="若任课老师已录入期末成绩，将无法退课。" type="info" show-icon style="margin-bottom: 15px;" />

        <el-table :data="myRecords" border stripe>
          <el-table-column prop="courseName" label="课程名称" min-width="150" />
          <el-table-column prop="teacherName" label="上课教师" width="120" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column label="选课状态" width="120" align="center">
            <template #default>
              <el-tag type="success">成功</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.score === null" type="danger" size="small" @click="handleDropCourse(scope.row.id)">退课</el-button>
              <span v-else style="color: #999; font-size: 12px;">已结课</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的课表" name="myTimetable">
        <div style="margin-bottom: 10px; text-align: right;">
          <el-button type="success" size="small" @click="loadMyTimetable">刷新课表</el-button>
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
              <div v-if="getMyCourse(day, section)" class="course-card my-card">
                <div class="course-name">{{ getMyCourse(day, section).courseName }}</div>
                <div class="course-meta">地点: {{ getMyCourse(day, section).location }}</div>
                <div class="course-meta">教师: {{ getMyCourse(day, section).teacherName }}</div>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const activeTab = ref('independent')
const weekDays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']

// 数据源
const recommendCourses = ref([])
const independentCourses = ref([])
const myRecords = ref([]) // 选课记录数据
const myCourses = ref([]) // 网格课表数据

// 1. 加载推荐课表
const loadRecommendCourses = () => {
  request.get(`/user/info/${user.userNo}`).then(userRes => {
    request.get('/course/list', {
      params: { majorName: userRes.data.major, targetGrade: userRes.data.grade }
    }).then(res => {
      recommendCourses.value = res.data
    })
  })
}
const getRecommendCourse = (day, section) => recommendCourses.value.find(c => c.dayOfWeek === day && c.section === section)

// 2. 自主选课大厅查询
const searchParams = ref({ nature: '', dayOfWeek: null, section: null, hasCapacity: false })
const searchCourses = () => {
  request.get('/course/search', { params: searchParams.value }).then(res => {
    independentCourses.value = res.data
  })
}

// 3. 加载选课记录 (列表)
const loadMyRecords = () => {
  request.get('/score/list', { params: { studentNo: user.userNo } }).then(res => {
    myRecords.value = res.data
  })
}

// 4. 加载我的已选课表 (网格)
const loadMyTimetable = () => {
  request.get('/course/timetable', { params: { userNo: user.userNo, role: 'STUDENT' } }).then(res => {
    myCourses.value = res.data
  })
}
const getMyCourse = (day, section) => myCourses.value.find(c => c.dayOfWeek === day && c.section === section)

// --- 核心：一键选课执行逻辑 ---
const executeSelectCourse = (course) => {
  if (course.enrolled >= course.capacity) {
    ElMessage.warning('该课程名额已满！')
    return
  }

  request.post('/score/selectCourse', { studentNo: user.userNo, courseId: course.id }).then(res => {
    ElMessage.success( '选课成功！')
    // 选课成功后，刷新所有数据，并切到选课记录
    refreshAllData()
    activeTab.value = 'records'
  })
}

const handleRecommendSelect = (course) => {
  if (!course) return
  executeSelectCourse(course)
}

// --- 核心：退课执行逻辑 ---
const handleDropCourse = (id) => {
  ElMessageBox.confirm('确定要退选这门课吗？', '确认退课', { type: 'warning', confirmButtonText: '退课', cancelButtonText: '取消' }).then(() => {
    request.delete(`/score/${id}`).then(res => {
      ElMessage.success('退课成功')
      refreshAllData() // 退课后同步刷新所有数据
    })
  })
}

// 统一刷新所有页面数据
const refreshAllData = () => {
  loadRecommendCourses()
  searchCourses()
  loadMyRecords()
  loadMyTimetable()
}

onMounted(() => {
  refreshAllData()
})
</script>

<style scoped>
.timetable { width: 100%; border-collapse: collapse; text-align: center; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #ebeef5; padding: 10px; }
.timetable th { background-color: #f5f7fa; color: #606266; font-weight: bold; }
.section-col { background-color: #fafafa; color: #909399; font-size: 14px; }
.course-cell { height: 100px; vertical-align: top; cursor: pointer; transition: all 0.2s; }
.course-cell:hover { background-color: #f0f9eb; box-shadow: inset 0 0 5px rgba(103, 194, 58, 0.2); }
.empty-cell { color: #dcdfe6; font-size: 12px; height: 100%; display: flex; align-items: center; justify-content: center; }

.course-card { border-radius: 4px; padding: 8px; text-align: left; height: 100%; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between;}
.recommend-card { background-color: #fdf6ec; border-left: 4px solid #E6A23C; }
.my-card { background-color: #ecf5ff; border-left: 4px solid #409EFF; }

.course-name { font-weight: bold; color: #303133; margin-bottom: 5px; font-size: 13px; }
.course-meta { font-size: 11px; color: #666; margin-top: 2px; }
</style>