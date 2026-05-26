<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, ShoppingCart, Money, Medal, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getBusinessStatistics, getMemberStatistics, getMonthlyTrend } from '@/api/dashboard'
import { getPointsStatistics } from '@/api/points'
import { getCouponsStatistics } from '@/api/coupon'

const dateRange = ref<[string, string] | null>(null)
const loading = ref(false)

const stats = reactive({
  totalMembers: '--',
  totalRevenue: '--',
  totalOrders: '--',
  avgOrderAmount: '--'
})

const memberChartRef = ref<HTMLDivElement>()
const revenueChartRef = ref<HTMLDivElement>()
const pointsChartRef = ref<HTMLDivElement>()
const couponChartRef = ref<HTMLDivElement>()

let memberChart: echarts.ECharts | null = null
let revenueChart: echarts.ECharts | null = null
let pointsChart: echarts.ECharts | null = null
let couponChart: echarts.ECharts | null = null

function formatMonthLabels(trendData: Array<{ month: string }>) {
  return trendData.map(d => {
    const parts = d.month.split('-')
    return `${parseInt(parts[1])}月`
  })
}

function updateMemberChart(memberTrend: Array<{ month: string; newMembers: number }>) {
  if (!memberChart) return
  const labels = formatMonthLabels(memberTrend)
  const data = memberTrend.map(d => d.newMembers)
  memberChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ name: '新增会员', type: 'line', smooth: true, data, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' } }]
  })
}

function updateRevenueChart(revenueTrend: Array<{ month: string; revenue: number }>) {
  if (!revenueChart) return
  const labels = formatMonthLabels(revenueTrend)
  const data = revenueTrend.map(d => d.revenue)
  revenueChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ name: '营业额', type: 'bar', data, itemStyle: { color: '#67c23a', borderRadius: [4,4,0,0] } }]
  })
}

function updatePointsChart(pointsTrend: Array<{ month: string; earned: number; used: number }>) {
  if (!pointsChart || !pointsTrend.length) return
  const labels = formatMonthLabels(pointsTrend)
  pointsChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['积分发放', '积分使用'] },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [
      { name: '积分发放', type: 'line', smooth: true, data: pointsTrend.map(d => d.earned), itemStyle: { color: '#409eff' } },
      { name: '积分使用', type: 'line', smooth: true, data: pointsTrend.map(d => d.used), itemStyle: { color: '#e6a23c' } }
    ]
  })
}

function updateCouponChart(couponData: any) {
  if (couponChart && couponData) {
    const used = Number(couponData.usedCoupons) || 0
    const issued = Number(couponData.issuedCoupons) || 0
    const total = Number(couponData.totalCoupons) || 0
    const unused = Math.max(0, issued - used)
    const expired = Math.max(0, total - issued)
    couponChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: [
          { value: used || 1, name: '已使用', itemStyle: { color: '#67c23a' } },
          { value: unused || 1, name: '未使用', itemStyle: { color: '#409eff' } },
          { value: expired || 1, name: '已过期', itemStyle: { color: '#909399' } }
        ],
        label: { formatter: '{b}: {d}%' }
      }]
    })
  }
}

function initCharts() {
  if (memberChartRef.value) memberChart = echarts.init(memberChartRef.value)
  if (revenueChartRef.value) revenueChart = echarts.init(revenueChartRef.value)
  if (pointsChartRef.value) pointsChart = echarts.init(pointsChartRef.value)
  if (couponChartRef.value) couponChart = echarts.init(couponChartRef.value)
}

async function fetchStats() {
  loading.value = true
  let couponData: any = null
  try {
    const [bizRes, memberRes, pointsRes, couponRes, trendRes] = await Promise.allSettled([
      getBusinessStatistics(),
      getMemberStatistics(),
      getPointsStatistics(),
      getCouponsStatistics(),
      getMonthlyTrend(12)
    ])
    if (bizRes.status === 'fulfilled' && bizRes.value.data) {
      const d = bizRes.value.data
      stats.totalRevenue = d.totalRevenue ?? '--'
      stats.totalOrders = d.totalOrders ?? '--'
      stats.avgOrderAmount = d.averageOrderValue ?? '--'
    }
    if (memberRes.status === 'fulfilled' && memberRes.value.data) {
      stats.totalMembers = (memberRes.value.data as any).totalMembers ?? '--'
    }
    if (pointsRes.status === 'fulfilled' && pointsRes.value.data) {
      // points stats available if needed
    }
    if (couponRes.status === 'fulfilled' && couponRes.value.data) couponData = couponRes.value.data

    // 用月度趋势数据更新图表
    if (trendRes.status === 'fulfilled' && trendRes.value.data) {
      const trend = trendRes.value.data as any
      updateMemberChart(trend.memberTrend || [])
      updateRevenueChart(trend.revenueTrend || [])
      updatePointsChart(trend.pointsTrend || [])
    }
  } catch (e) {
    console.warn('获取统计数据失败', e)
  }
  loading.value = false

  await nextTick()
  updateCouponChart(couponData)
}

function handleRefresh() {
  fetchStats()
  ElMessage.success('数据刷新成功')
}

function handleResize() {
  memberChart?.resize()
  revenueChart?.resize()
  pointsChart?.resize()
  couponChart?.resize()
}

onMounted(async () => {
  await fetchStats()
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  memberChart?.dispose()
  revenueChart?.dispose()
  pointsChart?.dispose()
  couponChart?.dispose()
})
</script>