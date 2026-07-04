export const BASE_URL = import.meta.env.VITE_API_BASE_URL

export function request<T>(options: {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
}): Promise<T> {
  const token = uni.getStorageSync('token') as string
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json',
      },
      success: (res) => {
        const data = res.data as { code: number; msg: string; data: T }
        if (res.statusCode === 401 || data.code === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(data)
          return
        }
        if (data.code === 0) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
          reject(data)
        }
      },
      fail: () => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(new Error('network'))
      },
    })
  })
}
