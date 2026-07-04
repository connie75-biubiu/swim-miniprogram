import { request } from '@/utils/request'



export const SOURCE_TRAIN = 1

export const SOURCE_COMPETE = 2



export interface SplitForm {

  seq: number

  stroke: string

  distance: number

  duration: number

}



export interface WorkoutForm {

  date: string

  stroke: string

  poolType: number

  sourceType?: number

  note?: string

  splits: SplitForm[]

}



export interface Workout {

  id: number

  date: string

  stroke: string

  poolType: number

  sourceType: number

  totalDistance: number

  totalDuration: number

  avgPace: number

  note?: string

}



export interface Split {

  id: number

  seq: number

  stroke: string

  distance: number

  duration: number

  pace: number

}



export interface CompetitionSummary {

  stroke: string

  count: number

  bestPace: number | null

}



export function getWorkouts(page = 1, size = 20, sourceType?: number) {

  let url = `/api/workouts?page=${page}&size=${size}`

  if (sourceType != null) url += `&sourceType=${sourceType}`

  return request<{ records: Workout[]; total: number }>({ url })

}



export function getCompetitionSummary() {

  return request<CompetitionSummary[]>({ url: '/api/competitions/summary' })

}



export function getCompetitions(stroke: string, page = 1, size = 20) {

  return request<{

    records: Workout[]

    total: number

    totalCount: number

    bestPace: number | null

  }>({

    url: `/api/competitions?stroke=${encodeURIComponent(stroke)}&page=${page}&size=${size}`,

  })

}



export function getWorkout(id: number) {

  return request<{ workout: Workout; splits: Split[] }>({

    url: `/api/workouts/${id}`,

  })

}



export function createWorkout(data: WorkoutForm) {

  return request<Workout>({ url: '/api/workouts', method: 'POST', data })

}



export function updateWorkout(id: number, data: WorkoutForm) {

  return request<Workout>({ url: `/api/workouts/${id}`, method: 'PUT', data })

}



export function deleteWorkout(id: number) {

  return request<null>({ url: `/api/workouts/${id}`, method: 'DELETE' })

}

