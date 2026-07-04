import { formatDuration, formatPace } from '@/utils/pace'
import type { Workout } from '@/api/workout'

export function drawShareCard(canvasId: string, workout: Workout): Promise<string> {
  return new Promise((resolve, reject) => {
    const ctx = uni.createCanvasContext(canvasId)
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, 300, 400)
    ctx.setStrokeStyle('#0096D6')
    ctx.setLineWidth(2)
    ctx.strokeRect(10, 10, 280, 380)

    ctx.setFillStyle('#999')
    ctx.setFontSize(14)
    ctx.fillText('🏊 泳记', 120, 50)

    ctx.setFillStyle('#0096D6')
    ctx.setFontSize(32)
    ctx.setTextAlign('center')
    ctx.fillText(`${workout.totalDistance}m`, 150, 110)

    ctx.setFillStyle('#333')
    ctx.setFontSize(16)
    ctx.fillText(
      `${workout.date} · ${workout.stroke} · ${workout.poolType === 1 ? '25米池' : '50米池'}`,
      150,
      150,
    )

    ctx.setFillStyle('#0096D6')
    ctx.setFontSize(14)
    ctx.fillText(
      `配速 ${formatPace(workout.avgPace)} · 用时 ${formatDuration(workout.totalDuration)}`,
      150,
      180,
    )

    ctx.draw(false, () => {
      uni.canvasToTempFilePath({
        canvasId,
        success: (res) => resolve(res.tempFilePath),
        fail: reject,
      })
    })
  })
}
