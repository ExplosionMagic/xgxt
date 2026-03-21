<template>
  <div>
    <h2>欢迎使用高校学生工作管理系统！</h2>
    <el-divider />

    <div style="display: flex; gap: 20px;">
      <el-card v-if="user.role !== 'STUDENT'" shadow="hover" style="flex: 1; height: 400px;">
        <div ref="pieChartRef" style="width: 100%; height: 100%;"></div>
      </el-card>

      <el-card shadow="hover" :style="{ flex: user.role === 'STUDENT' ? 'none' : '1', width: user.role === 'STUDENT' ? '100%' : 'auto', height: '400px' }">
        <template #header>
          <div style="font-weight: bold; font-size: 16px;">
            <el-icon color="#E6A23C" style="margin-right: 5px;"><Bell /></el-icon>
            系统公告：
          </div>
        </template>

        <el-scrollbar height="300px">
          <div v-if="announcements.length === 0" style="text-align: center; color: #999; margin-top: 50px;">
            暂无最新公告！
          </div>
          <div v-for="item in announcements" :key="item.id" class="notice-item" @click="viewNotice(item)">
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-meta">
              <span>发布人：{{ item.publisher }}</span>
              <span>{{ item.createTime }}</span>
            </div>
          </div>
        </el-scrollbar>
      </el-card>
    </div>

    <el-dialog v-model="noticeDialogVisible" :title="currentNotice.title" width="50%">
      <div style="text-align: center; color: #999; margin-bottom: 20px; font-size: 13px;">
        发布人：{{ currentNotice.publisher }} | 发布时间：{{ currentNotice.createTime }}
      </div>
      <el-divider />
      <div class="notice-content" v-html="currentNotice.content.replace(/\n/g, '<br/>')"></div>
      <template #footer>
        <el-button type="primary" @click="noticeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { Bell } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const pieChartRef = ref(null)

// --- 通知公告逻辑 ---
const announcements = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const loadAnnouncements = () => {
  request.get('/announcement/my', {
    params: {
      role: user.role,
      major: user.major,
      className: user.className
    }
  }).then(res => {
    announcements.value = res.data
  })
}

const viewNotice = (item) => {
  currentNotice.value = item
  noticeDialogVisible.value = true
}

// --- Echarts 原有逻辑 ---
const initPieChart = (data) => {
  const myChart = echarts.init(pieChartRef.value)
  const option = {
    title: { text: '各专业学生人数占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c}人 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{ name: '专业人数', type: 'pie', radius: '50%', data: data }]
  }
  myChart.setOption(option)
}

onMounted(() => {
  loadAnnouncements()

  if (user.role !== 'STUDENT') {
    request.get('/data/echarts').then(res => {
      if (res.data && res.data.majorData) {
        // 使用 nextTick 确保 DOM 渲染完毕后再画图
        nextTick(() => {
          initPieChart(res.data.majorData)
        })
      }
    })
  }
})
</script>

<style scoped>
.notice-item {
  padding: 12px 10px;
  border-bottom: 1px dashed #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}
.notice-item:hover {
  background-color: #f5f7fa;
}
.notice-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notice-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
}
.notice-content {
  line-height: 1.8;
  font-size: 15px;
  color: #333;
}
</style>