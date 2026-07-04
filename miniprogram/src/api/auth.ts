import { request } from '@/utils/request'



export function wxLogin(code: string) {

  return request<{

    token: string

    needBindPhone: boolean

    needSelectRole: boolean

    role: string | null

  }>({

    url: '/api/auth/wx-login',

    method: 'POST',

    data: { code },

  })

}



export function getStrokeOrder() {

  return request<string[]>({ url: '/api/user/stroke-order' })

}



export function updateStrokeOrder(order: string[]) {

  return request<string[]>({

    url: '/api/user/stroke-order',

    method: 'PUT',

    data: { order },

  })

}

