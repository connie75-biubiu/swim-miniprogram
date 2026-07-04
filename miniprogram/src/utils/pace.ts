export function calcPace(distance: number, duration: number): number | null {
  if (distance <= 0) return null
  return Math.round((duration / (distance / 100)) * 100) / 100
}

export function formatPace(pace: number | null): string {
  if (pace == null) return '-'
  const min = Math.floor(pace / 60)
  const sec = Math.round(pace % 60)
  return `${min}'${sec.toString().padStart(2, '0')}"/100m`
}

export function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}'${s.toString().padStart(2, '0')}"`
}
