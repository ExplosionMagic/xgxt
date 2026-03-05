<template>
  <div>
    <h2>欢迎来到高校学生工作管理系统！</h2>
    <el-divider />

    <el-card shadow="hover" style="width: 50%; height: 400px; margin-top: 20px;">
      <div ref="pieChartRef" style="width: 100%; height: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts' // 引入 Echarts
import request from '../utils/request' // 引入你封装的 axios

// 拿到图表容器的 DOM 引用
const pieChartRef = ref(null)

// 初始化饼图的方法
const initPieChart = (data) => {
  // 1. 基于准备好的 dom，初始化 echarts 实例
  const myChart = echarts.init(pieChartRef.value)

  // 2. 指定图表的配置项和数据
  const option = {
    title: {
      text: '各专业学生人数占比',
      subtext: '实时数据统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c}人 ({d}%)' // 悬浮提示框的格式
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '专业人数',
        type: 'pie',
        radius: '50%',
        data: data, // 把后端查出来的数据塞到这里
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  // 3. 使用刚指定的配置项和数据显示图表
  myChart.setOption(option)
}

// 页面挂载后，发请求拿数据，然后画图
onMounted(() => {
  request.get('/data/echarts').then(res => {
    // 假设你的后端数据存放在 res.data.majorData 里
    if (res.data && res.data.majorData) {
      initPieChart(res.data.majorData)
    }
  })
})
</script>

<style scoped>
/* 这里可以写一些简单的样式 */
</style>