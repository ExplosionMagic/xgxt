<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-tag type="primary" size="large">全校数据统计大屏</el-tag>
    </div>

    <el-card shadow="hover" style="width: 50%; height: 450px;">
      <div ref="pieChartRef" style="width: 100%; height: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const pieChartRef = ref(null)

const initPieChart = (data) => {
  const myChart = echarts.init(pieChartRef.value)
  const option = {
    title: { text: '各专业学生人数占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c}人 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '专业人数', type: 'pie', radius: '50%', data: data,
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
      }
    ]
  }
  myChart.setOption(option)
}

onMounted(() => {
  request.get('/data/echarts').then(res => {
    if (res.data && res.data.majorData) {
      nextTick(() => {
        initPieChart(res.data.majorData)
      })
    }
  })
})
</script>