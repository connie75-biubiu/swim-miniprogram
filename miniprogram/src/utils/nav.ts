export function afterLogin(res: {
  needSelectRole: boolean
  role: string | null
}) {
  routeByRole(res.needSelectRole, res.role)
}

function routeByRole(needSelectRole: boolean, role: string | null) {
  if (needSelectRole) {
    uni.reLaunch({ url: '/pages/role-select/index' })
    return
  }
  if (role === 'coach') {
    uni.reLaunch({ url: '/pages/coach-home/index' })
    return
  }
  uni.reLaunch({ url: '/pages/index/index' })
}
