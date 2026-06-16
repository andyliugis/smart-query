<template>
  <div class="chart-container">
    <div class="chart-header">
      <div class="chart-type-selector">
        <el-button
          v-for="type in chartTypes"
          :key="type.value"
          :type="currentType === type.value ? 'primary' : 'default'"
          size="small"
          @click="currentType = type.value"
        >
          {{ type.icon }} {{ type.label }}
        </el-button>
      </div>
    </div>
    <div class="chart-content" ref="chartRef"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

interface Props {
  columns: string[]
  data: Record<string, any>[]
}

const props = defineProps<Props>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const chartTypes = [
  { value: 'bar', label: '柱状图', icon: '📊' },
  { value: 'line', label: '折线图', icon: '📈' },
  { value: 'pie', label: '饼图', icon: '🥧' },
  { value: 'table', label: '表格', icon: '📋' }
]

const currentType = ref('bar')

function detectChartType(): string {
  if (!props.columns.length || !props.data.length) return 'table'
  
  const numericCols = props.columns.filter(col => {
    const values = props.data.map(row => row[col]).filter(v => v != null)
    return values.length > 0 && values.every(v => typeof v === 'number' || !isNaN(Number(v)))
  })
  
  const categoricalCols = props.columns.filter(col => {
    const values = props.data.map(row => row[col]).filter(v => v != null)
    return values.length > 0 && values.every(v => typeof v === 'string')
  })
  
  const timeCols = props.columns.filter(col => {
    const values = props.data.map(row => row[col]).filter(v => v != null)
    return values.length > 0 && values.some(v => {
      const str = String(v)
      return /\d{4}[-/]\d{1,2}[-/]\d{1,2}/.test(str) || /\d{1,2}[-/]\d{1,2}/.test(str)
    })
  })
  
  if (timeCols.length >= 1 && numericCols.length >= 1) {
    return 'line'
  } else if (categoricalCols.length >= 1 && numericCols.length >= 1) {
    if (categoricalCols.length === 1 && props.data.length <= 8) {
      return 'pie'
    }
    return 'bar'
  }
  
  return 'table'
}

function renderChart() {
  if (!chartRef.value || currentType.value === 'table') {
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
    return
  }
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const numericCols = props.columns.filter(col => {
    const values = props.data.map(row => row[col]).filter(v => v != null)
    return values.length > 0 && values.every(v => typeof v === 'number' || !isNaN(Number(v)))
  })
  
  const categoricalCols = props.columns.filter(col => {
    const values = props.data.map(row => row[col]).filter(v => v != null)
    return values.length > 0 && values.every(v => typeof v === 'string')
  })
  
  const categoryCol = categoricalCols[0] || props.columns[0]
  const valueCol = numericCols[0] || props.columns[1]
  
  const categories = props.data.map(row => String(row[categoryCol]))
  const values = props.data.map(row => Number(row[valueCol]) || 0)
  
  let option: echarts.EChartsOption = {}
  
  if (currentType.value === 'bar') {
    option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: categories,
        axisLabel: { rotate: categories.length > 5 ? 45 : 0 }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: values,
        itemStyle: { color: '#3b82f6' }
      }],
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
    }
  } else if (currentType.value === 'line') {
    option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: categories,
        axisLabel: { rotate: categories.length > 5 ? 45 : 0 }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        data: values,
        smooth: true,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#3b82f6' }
      }],
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
    }
  } else if (currentType.value === 'pie') {
    const pieData = props.data.map(row => ({
      name: String(row[categoryCol]),
      value: Number(row[valueCol]) || 0
    }))
    
    option = {
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: pieData
      }]
    }
  }
  
  chartInstance.setOption(option, true)
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  currentType.value = detectChartType()
  nextTick(() => renderChart())
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

watch(currentType, () => {
  nextTick(() => renderChart())
})

watch(() => [props.columns, props.data], () => {
  currentType.value = detectChartType()
  nextTick(() => renderChart())
}, { deep: true })
</script>

<style scoped>
.chart-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-header {
  padding: 8px 0;
}

.chart-type-selector {
  display: flex;
  gap: 4px;
}

.chart-content {
  flex: 1;
  min-height: 250px;
}
</style>
