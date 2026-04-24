<template>
  <div>
    <h2>欢迎使用高校学生工作管理系统！</h2>
    <el-divider />

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" style="height: 600px;">
          <template #header>
            <div style="font-weight: bold; font-size: 16px;">
              <el-icon color="#E6A23C" style="margin-right: 5px;"><Bell /></el-icon>
              系统公告
            </div>
          </template>

          <el-scrollbar height="500px">
            <div v-if="announcements.length === 0" style="text-align: center; color: #999; margin-top: 50px;">
              暂无最新公告
            </div>
            <div v-for="item in announcements" :key="item.id" class="notice-item" @click="viewNotice(item)">
              <div class="notice-title">{{ item.title }}</div>
              <div class="notice-meta">
                <span>{{ item.publisher }}</span>
                <span>{{ item.createTime }}</span>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover" style="height: 600px; display: flex; flex-direction: column;">
          <template #header>
            <div style="font-weight: bold; font-size: 16px; display: flex; justify-content: space-between; align-items: center;">
              <div>
                <el-icon color="#409EFF" style="margin-right: 5px;"><Grid /></el-icon>
                我的课表
                <span v-if="user.role !== 'ADMIN'" style="margin-left: 10px; color: #999; font-size: 12px; font-weight: normal;">
<!--                  {{ user.role === 'STUDENT' ? '仅显示审核通过的课程' : '课程安排' }}-->
                </span>
              </div>
              <el-button v-if="user.role !== 'ADMIN'" type="primary" size="small" plain @click="loadTimetable">刷新课表</el-button>
            </div>
          </template>

          <div v-if="user.role === 'ADMIN'" style="text-align: center; color: #999; margin-top: 150px;">
            <el-icon size="50"><Monitor /></el-icon>
            <p>空</p>
          </div>

          <div v-else style="flex: 1; overflow-y: auto;">
            <table class="timetable">
              <thead>
              <tr>
                <th style="width: 12%;">节次 / 星期</th>
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
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="noticeDialogVisible" :title="currentNotice.title" width="50%">
      <div style="text-align: center; color: #999; margin-bottom: 20px; font-size: 13px;">
        作者：{{ currentNotice.publisher }} | 发布时间：{{ currentNotice.createTime }}
      </div>
      <el-divider />
      <div class="notice-content" v-html="currentNotice.content ? currentNotice.content.replace(/\n/g, '<br/>') : ''"></div>
      <template #footer>
        <el-button type="primary" @click="noticeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { Bell, Grid, Location, Avatar, Collection, Monitor } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')

// ===== 公告逻辑 =====
const announcements = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const loadAnnouncements = () => {
  // 如果是管理员，直接查全部列表即可
  if (user.role === 'ADMIN') {
    request.get('/announcement/list').then(res => {
      announcements.value = res.data
    })
    return
  }

  // 如果是老师或学生，先实时获取其最新的个人信息（确保能拿到准确的 majorName 和 className）
  request.get(`/user/info/${user.userNo}`).then(userRes => {
    const userInfo = userRes.data || {}

    // 注意：这里的字段名要和后端 User 实体类对应，通常是 majorName 或 major
    const actualMajor = userInfo.majorName || userInfo.major
    const actualClass = userInfo.className

    // 拿到准确参数后，再去请求公告
    request.get('/announcement/my', {
      params: {
        role: user.role,
        major: actualMajor,
        className: actualClass
      }
    }).then(res => {
      announcements.value = res.data
    })
  })
}

const viewNotice = (item) => {
  currentNotice.value = item
  noticeDialogVisible.value = true
}

// ===== 课表逻辑 =====
const courseList = ref([])
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const loadTimetable = () => {
  if (user.role === 'ADMIN') return // 管理员不查课表
  request.get('/course/timetable', {
    params: { userNo: user.userNo, role: user.role }
  }).then(res => {
    courseList.value = res.data
  })
}

const getCourse = (day, section) => {
  return courseList.value.find(c => c.dayOfWeek === day && c.section === section)
}

onMounted(() => {
  loadAnnouncements()
  loadTimetable()
})
</script>

<style scoped>
/* 公告样式 */
.notice-item {
  padding: 12px 10px; border-bottom: 1px dashed #ebeef5; cursor: pointer; transition: background-color 0.3s;
}
.notice-item:hover { background-color: #f5f7fa; }
.notice-title { font-size: 14px; color: #303133; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.notice-meta { font-size: 12px; color: #909399; display: flex; justify-content: space-between; }
.notice-content { line-height: 1.8; font-size: 15px; color: #333; }

/* 课表样式 */
.timetable { width: 100%; border-collapse: collapse; text-align: center; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #ebeef5; padding: 6px; }
.timetable th { background-color: #f5f7fa; color: #606266; font-weight: bold; font-size: 14px; }
.section-col { background-color: #fafafa; color: #909399; font-size: 13px; }
.course-cell { height: 90px; vertical-align: top; padding: 4px !important; }
.course-card { background-color: #ecf5ff; border-left: 4px solid #409EFF; border-radius: 4px; padding: 6px; text-align: left; height: 100%; box-sizing: border-box; overflow: hidden; }
.course-name { font-weight: bold; color: #303133; margin-bottom: 4px; font-size: 12px; }
.course-meta { font-size: 11px; color: #666; display: flex; align-items: center; gap: 2px; margin-top: 2px; }
</style>