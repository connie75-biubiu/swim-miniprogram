import { request } from '@/utils/request'

export function getSummary(period: 'week' | 'month') {
  return request<{ totalDistance: number; totalDuration: number; count: number }>({
    url: `/api/stats/summary?period=${period}`,
  })
}

export function getTrend(days = 30) {
  return request<{ date: string; distance: number }[]>({
    url: `/api/stats/trend?days=${days}`,
  })
}

export function getPr() {
  return request<{ stroke: string; bestPace: number; maxDistance: number }[]>({
    url: '/api/stats/pr',
  })
}

export function getHeatmap(year: number) {
  return request<{ date: string; distance: number }[]>({
    url: `/api/stats/heatmap?year=${year}`,
  })
}

export function getStrokeTrend(stroke: string, distance: number, weeks = 4) {
  return request<{ week: string; avgPace: number }[]>({
    url: `/api/stats/stroke-trend?stroke=${encodeURIComponent(stroke)}&distance=${distance}&weeks=${weeks}`,
  })
}

export interface StrokeOverviewItem {
  stroke: string
  distance: number
  latestPace: number | null
  changeSeconds: number | null
  trend: { week: string; avgPace: number }[]
}

export function getStrokeOverview(weeks = 4) {
  return request<StrokeOverviewItem[]>({
    url: `/api/stats/stroke-overview?weeks=${weeks}`,
  })
}

export function getStrokeImprove(distance = 100, weeks = 4) {
  return request<{ stroke: string; improveSeconds: number }[]>({
    url: `/api/stats/stroke-improve?distance=${distance}&weeks=${weeks}`,
  })
}
