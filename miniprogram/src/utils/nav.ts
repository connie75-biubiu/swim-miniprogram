export function afterLogin(res: {
  needSelectRole: boolean
  role: string | null
}) {
  routeByRole(res.needSelectRole, res.role)
}

function routeByRole(needSelectRole: boolean, role: string | null) {
  // 首次登录（未选身份）让用户在「选择身份」页确认；老用户直接进首页
  if (needSelectRole) {
    uni.reLaunch({ url: '/pages/role-select/index' })
    return
  }
  uni.reLaunch({ url: '/pages/index/index' })
}
