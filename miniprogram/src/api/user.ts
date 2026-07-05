import { request } from '@/utils/request'



export interface Profile {

  nickname: string | null

  role: string | null

  gender: number | null

  birthMonth: string | null

}



export function getProfile() {

  return request<Profile>({ url: '/api/user/profile' })

}



export function updateProfile(data: {

  nickname?: string

  gender?: number

  birthMonth?: string

}) {

  return request<Profile>({ url: '/api/user/profile', method: 'PUT', data })

}



export function selectRole(role: 'student' | 'coach') {

  return request<Profile>({ url: '/api/user/role', method: 'POST', data: { role } })

}
