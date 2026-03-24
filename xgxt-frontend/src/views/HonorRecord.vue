<template>
  <div>
    <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap;">
      <el-input v-model="searchKeyword" placeholder="姓名/学号/奖项" style="width: 220px;" clearable prefix-icon="Search" />
      <el-select v-model="searchStatus" placeholder="所有状态" clearable style="width: 160px;">
        <el-option label="待初审" :value="0" />
        <el-option label="初审驳回" :value="1" />
        <el-option label="初审通过待终审" :value="2" />
        <el-option label="终审驳回" :value="3" />
        <el-option label="终审通过" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData">查询记录</el-button>
    </div>

    <el-table :data="tableData" border stripe size="large">
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="studentName" label="姓名" min-width="100" />
      <el-table-column prop="honorName" label="申请奖项" min-width="150" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" />

      <el-table-column label="最终状态" min-width="140" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning" size="large">待初审</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="danger" size="large">初审驳回</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="primary" size="large">初审通过待终审</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="danger" size="large">终审驳回</el-tag>
          <el-tag v-else-if="scope.row.status === 4" type="success" effect="dark" size="large">终审通过</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="经办人记录" min-width="160">
        <template #default="scope">
          <div style="font-size: 13px; color: #606266;">
            <div>初审：{{ scope.row.teacherApprover || '等待处理' }}</div>
            <div>终审：{{ scope.row.adminApprover || '等待处理' }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" plain size="small" @click="openDetail(scope.row)">查看事迹</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="荣誉申报档案" width="650px" top="10vh">
      <div v-if="currentHonor">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="姓名">{{ currentHonor.studentName }}</el-descriptions-item>
          <el-descriptions-item label="奖项名称"><b>{{ currentHonor.honorName }}</b></el-descriptions-item>
        </el-descriptions>
        <div style="border: 1px solid #EBEEF5; padding: 18px; border-radius: 6px; background-color: #fcfcfc;">
          <div style="font-weight: bold; margin-bottom: 12px;">主要事迹说明：</div>
          <div style="line-height: 1.8; color: #606266; white-space: pre-wrap;">{{ currentHonor.reason }}</div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭档案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const user = JSON.parse(localStorage.getItem('student-user') || '{}')
const tableData = ref([])
const searchKeyword = ref('')
const searchStatus = ref(null)
const detailVisible = ref(false)
const currentHonor = ref(null)

const loadData = () => {
  request.get('/honor/list', {
    params: { role: user.role, status: searchStatus.value, keyword: searchKeyword.value }
  }).then(res => tableData.value = res.data)
}

const openDetail = (row) => {
  currentHonor.value = row
  detailVisible.value = true
}

onMounted(() => loadData())
</script>