export const STROKES = ['自由泳', '蛙泳', '仰泳', '蝶泳'] as const
export const DISTANCES = [50, 100, 200, 400] as const
export const STROKE_ICONS: Record<string, string> = {
  自由泳: '🏊',
  蛙泳: '🐸',
  仰泳: '🔙',
  蝶泳: '🦋',
}

export const DEFAULT_STROKE_ORDER = [...STROKES]
