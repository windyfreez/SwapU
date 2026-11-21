<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">平台运营概览：核心指标、流量趋势与商品结构</p>
      </div>
      <div class="page-actions">
        <select v-model.number="range" class="select" @change="loadTrend">
          <option :value="7">近 7 天</option>
          <option :value="14">近 14 天</option>
          <option :value="30">近 30 天</option>
        </select>
        <button class="btn" type="button" :disabled="loading" @click="load">
          {{ loading ? '加载中…' : '刷新数据' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="error-box">{{ error }}</div>

    <!-- 核心指标 -->
    <div class="stat-grid">
      <div class="stat-card accent">
        <div class="stat-label">在售商品</div>
        <div class="stat-value">{{ overview.onSaleCount ?? '—' }}</div>
        <div class="stat-foot">
          <router-link to="/admin/audit/products">去商品审核 →</router-link>
        </div>
      </div>

      <div class="stat-card accent">
        <div class="stat-label">待审核商品</div>
        <div class="stat-value">{{ overview.pendingAuditCount ?? '—' }}</div>
        <div class="stat-foot">
          <router-link to="/admin/audit/products">去处理 →</router-link>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-label">交易完成</div>
        <div class="stat-value">{{ overview.soldCount ?? '—' }}</div>
        <div class="stat-foot">笔（已收货）</div>
      </div>

      <div class="stat-card">
        <div class="stat-label">累计成交额</div>
        <div class="stat-value">¥{{ money(overview.totalAmount) }}</div>
        <div class="stat-foot">已完成订单合计</div>
      </div>

      <div class="stat-card">
        <div class="stat-label">商品总浏览</div>
        <div class="stat-value">{{ overview.totalViewCount ?? '—' }}</div>
        <div class="stat-foot">全部商品累计</div>
      </div>

      <div class="stat-card">
        <div class="stat-label">注册用户</div>
        <div class="stat-value">{{ overview.totalUserCount ?? '—' }}</div>
        <div class="stat-foot">
          <router-link to="/admin/users">查看用户 →</router-link>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-label">今日新增订单</div>
        <div class="stat-value">{{ overview.todayOrderCount ?? '—' }}</div>
        <div class="stat-foot">按下单时间</div>
      </div>

      <div class="stat-card">
        <div class="stat-label">今日成交额</div>
        <div class="stat-value">¥{{ money(overview.todayAmount) }}</div>
        <div class="stat-foot">按完成时间</div>
      </div>
    </div>

    <!-- 流量趋势 -->
    <div class="panel mt-16">
      <div class="panel-head">
        <h2 class="panel-title">流量趋势（近 {{ range }} 天）</h2>
        <span class="hint">浏览量按登录用户的浏览行为统计</span>
      </div>
      <div class="panel-body">
        <div ref="trendRef" class="chart"></div>
      </div>
    </div>

    <!-- 商品状态 + 分类销售 -->
    <div class="chart-grid mt-16">
      <div class="panel">
        <div class="panel-head">
          <h2 class="panel-title">商品状态分布</h2>
        </div>
        <div class="panel-body">
          <!-- 图表容器必须常驻并有真实尺寸：若用 v-show 隐藏后初始化，ECharts 会拿到 0×0 画布 -->
          <div class="chart-box">
            <div ref="statusRef" class="chart"></div>
            <div v-if="!statusStats.length" class="chart-empty">暂无商品数据</div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2 class="panel-title">分类成交额 Top 5</h2>
        </div>
        <div class="panel-body">
          <div class="chart-box">
            <div ref="categoryRef" class="chart"></div>
            <div v-if="!categorySales.length" class="chart-empty">暂无成交数据</div>
          </div>
        </div>
      </div>
    </div>

    <div class="notice mt-16">
      数据口径：成交额只统计「已收货」订单；流量按登录用户的浏览行为计数（游客浏览不落行为日志，因此是下限）。
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getCategorySales,
  getProductStatusStats,
  getStatisticsOverview,
  getTrafficTrend
} from '../api/statistics'

// 按需引入，只打包折线/饼/条形三种图 + 必要组件
echarts.use([
  LineChart,
  PieChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

const loading = ref(false)
const error = ref('')
const range = ref(7)

const overview = reactive({
  onSaleCount: null,
  pendingAuditCount: null,
  soldCount: null,
  totalAmount: 0,
  totalViewCount: null,
  totalUserCount: null,
  todayOrderCount: null,
  todayAmount: 0
})

const statusStats = ref([])
const categorySales = ref([])

const trendRef = ref(null)
const statusRef = ref(null)
const categoryRef = ref(null)
// ECharts 实例不放进 ref，避免被 Vue 代理后性能下降
let trendChart = null
let statusChart = null
let categoryChart = null

function cssVar(name, fallback) {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name)
  return (value || '').trim() || fallback
}

function money(value) {
  const num = Number(value || 0)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fmtDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

async function loadOverview() {
  const data = await getStatisticsOverview()
  Object.assign(overview, data || {})
}

async function loadTrend() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - (range.value - 1))

  const points = (await getTrafficTrend({
    startDate: fmtDate(start),
    endDate: fmtDate(end)
  })) || []
  renderTrend(points)
}

async function loadDistribution() {
  const [statusList, categoryList] = await Promise.all([
    getProductStatusStats(),
    getCategorySales(5)
  ])
  statusStats.value = statusList || []
  categorySales.value = categoryList || []

  // 等 DOM 更新完成再初始化图表，确保容器已经有真实宽高（否则 canvas 会是 0×0）
  await nextTick()
  renderStatus()
  renderCategory()
}

function renderTrend(points) {
  if (!trendRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendRef.value)
  }
  const primary = cssVar('--c-primary', '#2563eb')
  const success = cssVar('--c-success', '#16a34a')
  const text3 = cssVar('--c-text-3', '#9ca3af')
  const border = cssVar('--c-border', '#e5e7eb')

  trendChart.setOption({
    color: [primary, success],
    tooltip: { trigger: 'axis' },
    legend: { right: 8, top: 0, textStyle: { color: text3 } },
    grid: { left: 12, right: 16, top: 40, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((item) => item.date),
      axisLine: { lineStyle: { color: border } },
      axisLabel: { color: text3 }
    },
    yAxis: [
      {
        type: 'value',
        name: '浏览量',
        nameTextStyle: { color: text3 },
        splitLine: { lineStyle: { color: border, type: 'dashed' } },
        axisLabel: { color: text3 }
      },
      {
        type: 'value',
        name: '订单数',
        nameTextStyle: { color: text3 },
        splitLine: { show: false },
        axisLabel: { color: text3 }
      }
    ],
    series: [
      {
        name: '浏览量',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        areaStyle: { opacity: 0.12 },
        data: points.map((item) => item.viewCount)
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 6,
        data: points.map((item) => item.orderCount)
      }
    ]
  })
}

function renderStatus() {
  if (!statusRef.value) return
  if (!statusChart) {
    statusChart = echarts.init(statusRef.value)
  }
  if (!statusStats.value.length) {
    statusChart.clear()
    return
  }
  const text3 = cssVar('--c-text-3', '#9ca3af')

  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 件（{d}%）' },
    legend: { bottom: 0, textStyle: { color: text3 } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c} 件' },
        data: statusStats.value.map((item) => ({
          name: item.statusDesc,
          value: item.count
        }))
      }
    ]
  })
  // 容器可能刚从"暂无数据"切到有数据，补一次 resize 让画布重新测量尺寸
  statusChart.resize()
}

function renderCategory() {
  if (!categoryRef.value) return
  if (!categoryChart) {
    categoryChart = echarts.init(categoryRef.value)
  }
  if (!categorySales.value.length) {
    categoryChart.clear()
    return
  }
  const primary = cssVar('--c-primary', '#2563eb')
  const text3 = cssVar('--c-text-3', '#9ca3af')
  const border = cssVar('--c-border', '#e5e7eb')
  // 横向条形图从下往上排，先反转让成交额最高的在顶部
  const rows = [...categorySales.value].reverse()

  categoryChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        return `${item.name}<br/>成交额：¥${money(item.value)}`
      }
    },
    grid: { left: 8, right: 40, top: 8, bottom: 8, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: border, type: 'dashed' } },
      axisLabel: { color: text3 }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.categoryName || `分类${item.categoryId}`),
      axisLine: { lineStyle: { color: border } },
      axisLabel: { color: text3 }
    },
    series: [
      {
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: primary, borderRadius: [0, 6, 6, 0] },
        label: {
          show: true,
          position: 'right',
          color: text3,
          formatter: ({ value }) => `¥${money(value)}`
        },
        data: rows.map((item) => Number(item.salesAmount || 0))
      }
    ]
  })
  // 容器可能刚从"暂无数据"切到有数据，补一次 resize 让画布重新测量尺寸
  categoryChart.resize()
}

async function load() {
  loading.value = true
  error.value = ''

  const results = await Promise.allSettled([loadOverview(), loadTrend(), loadDistribution()])
  const failed = results
    .filter((item) => item.status === 'rejected')
    .map((item) => item.reason && item.reason.message)
    .filter(Boolean)
  if (failed.length) {
    error.value = `部分数据加载失败：${failed.join('；')}`
  }

  loading.value = false
}

function handleResize() {
  trendChart && trendChart.resize()
  statusChart && statusChart.resize()
  categoryChart && categoryChart.resize()
}

onMounted(async () => {
  await load()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  statusChart && statusChart.dispose()
  categoryChart && categoryChart.dispose()
  trendChart = null
  statusChart = null
  categoryChart = null
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 300px;
}

/* 图表容器常驻，空状态用绝对定位覆盖层表达，避免隐藏容器导致画布尺寸为 0 */
.chart-box {
  position: relative;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-text-3);
  font-size: 13px;
  background: var(--c-card);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 1100px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
