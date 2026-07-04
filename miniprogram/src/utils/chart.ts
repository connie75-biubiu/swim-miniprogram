export interface ChartSeries {
  name: string
  color: string
  data: number[]
  dashed?: boolean
}

export function drawLineChart(
  canvasId: string,
  width: number,
  height: number,
  labels: string[],
  series: ChartSeries[],
) {
  const ctx = uni.createCanvasContext(canvasId)
  const padL = 36
  const padR = 12
  const padT = 16
  const padB = 28
  const chartW = width - padL - padR
  const chartH = height - padT - padB

  ctx.setFillStyle('#ffffff')
  ctx.fillRect(0, 0, width, height)

  const allVals = series.flatMap((s) => s.data).filter((v) => v != null && !Number.isNaN(v))
  if (!allVals.length) {
    ctx.setFillStyle('#ccc')
    ctx.setFontSize(12)
    ctx.fillText('暂无数据', width / 2 - 24, height / 2)
    ctx.draw()
    return
  }

  let min = Math.min(...allVals)
  let max = Math.max(...allVals)
  if (min === max) {
    min -= 5
    max += 5
  }
  const range = max - min

  ctx.setStrokeStyle('#eee')
  ctx.setLineWidth(1)
  ctx.beginPath()
  ctx.moveTo(padL, padT)
  ctx.lineTo(padL, padT + chartH)
  ctx.lineTo(padL + chartW, padT + chartH)
  ctx.stroke()

  const stepX = labels.length > 1 ? chartW / (labels.length - 1) : chartW

  series.forEach((s) => {
    if (!s.data.length) return
    ctx.setStrokeStyle(s.color)
    ctx.setLineWidth(2.5)
    if (s.dashed) ctx.setLineDash([6, 4])
    else ctx.setLineDash([])
    ctx.beginPath()
    s.data.forEach((v, i) => {
      const x = padL + stepX * i
      const y = padT + chartH - ((v - min) / range) * chartH
      if (i === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    })
    ctx.stroke()
    s.data.forEach((v, i) => {
      const x = padL + stepX * i
      const y = padT + chartH - ((v - min) / range) * chartH
      ctx.setFillStyle(s.color)
      ctx.beginPath()
      ctx.arc(x, y, 3, 0, Math.PI * 2)
      ctx.fill()
    })
  })

  ctx.setFillStyle('#999')
  ctx.setFontSize(10)
  labels.forEach((lb, i) => {
    const x = padL + stepX * i - 8
    ctx.fillText(lb, x, padT + chartH + 18)
  })

  ctx.draw()
}

export function heatLevel(distance: number, max: number): number {
  if (distance <= 0 || max <= 0) return 0
  const r = distance / max
  if (r < 0.33) return 1
  if (r < 0.66) return 2
  return 3
}

export const HEAT_COLORS = ['#eee', '#B3E5FC', '#4FC3F7', '#0096D6']

export const STROKE_CHART_COLORS: Record<string, string> = {
  自由泳: '#2563EB',
  蛙泳: '#16A34A',
  仰泳: '#EA580C',
  蝶泳: '#9333EA',
}
