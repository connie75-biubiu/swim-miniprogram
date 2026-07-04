import { request } from '@/utils/request'



export interface Profile {

  nickname: string | null

  phone: string | null

  role: string | null

  gender: number | null

  birthMonth: string | null

}



export function getProfile() {

  return request<Profile>({ url: '/api/user/profile' })

}



export function updateProfile(data: {

  nickname?: string

  phone?: string

  gender?: number

  birthMonth?: string

}) {

  return request<Profile>({ url: '/api/user/profile', method: 'PUT', data })

}



export function selectRole(role: 'student' | 'coach') {

  return request<Profile>({ url: '/api/user/role', method: 'POST', data: { role } })

}



export interface CoachAuthItem {

  id: number

  coachName: string

  coachPhone: string

  createdAt: string

}



export function getCoachAuthList() {

  return request<CoachAuthItem[]>({ url: '/api/user/coach-auth' })

}



export function addCoachAuth(coachPhone: string) {

  return request<CoachAuthItem>({

    url: '/api/user/coach-auth',

    method: 'POST',

    data: { coachPhone },

  })

}



export function revokeCoachAuth(id: number) {

  return request<null>({ url: `/api/user/coach-auth/${id}`, method: 'DELETE' })

}

