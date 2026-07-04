# 游泳训练记录小程序 - 一期设计文档

> 日期：2026-06-29  
> 状态：待审阅

## 1. 项目概述

面向游泳学员的微信小程序，一期核心功能：记录训练数据、统计报表、可视化、分享。

| 项 | 决策 |
|----|------|
| 目标用户 | 游泳学员 |
| 平台 | 微信小程序 |
| 小程序框架 | uni-app + Vue3 + TypeScript |
| 后端 | Java Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 部署 | 国内云服务器 + Nginx + HTTPS（需 ICP 备案） |

## 2. 整体架构

```
┌─────────────┐      HTTPS/JSON       ┌──────────────────┐
│  小程序端    │  ←─────────────────→  │   后端服务        │
│  uni-app    │                       │  Spring Boot     │
│ (Vue3+TS)   │                       │  RESTful API     │
└─────────────┘                       └────────┬─────────┘
                                                │
                                       ┌────────▼─────────┐
                                       │      MySQL       │
                                       │  user/workout/   │
                                       │  split           │
                                       └──────────────────┘
```

### 目录结构

```
游泳小程序/
├── miniprogram/          # uni-app 小程序
│   ├── pages/
│   │   ├── login/
│   │   ├── index/        # 首页(训练列表)
│   │   ├── record/       # 新增/编辑记录
│   │   ├── detail/       # 训练详情
│   │   └── stats/        # 统计报表
│   ├── components/
│   ├── api/
│   ├── store/
│   └── utils/
└── server/               # Spring Boot
    └── src/main/java/com/swim/
        ├── controller/
        ├── service/
        ├── mapper/
        ├── entity/
        ├── dto/
        ├── config/
        └── common/
```

## 3. 数据模型

### user 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| openid | VARCHAR(64) UNIQUE | 微信 openid |
| phone | VARCHAR(11) | 手机号 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像 URL |
| created_at | DATETIME | 注册时间 |

### workout 训练记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| user_id | BIGINT FK | 用户 |
| date | DATE | 训练日期 |
| stroke | VARCHAR(20) | 主泳姿（自由泳/蛙泳/仰泳/蝶泳/混合） |
| pool_type | TINYINT | 泳池类型：1=25米，2=50米 |
| total_distance | INT | 总距离（米） |
| total_duration | INT | 总时长（秒） |
| avg_pace | DECIMAL(6,2) | 平均配速（秒/100m，后端自动计算） |
| note | VARCHAR(200) | 备注 |
| created_at | DATETIME | 创建时间 |

### split 分段表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| workout_id | BIGINT FK | 所属训练 |
| seq | INT | 段序号（1,2,3...） |
| stroke | VARCHAR(20) | 该段泳姿 |
| distance | INT | 距离（米） |
| duration | INT | 时长（秒） |
| pace | DECIMAL(6,2) | 配速（秒/100m，后端自动计算） |

### 计算规则

- 配速：`pace = duration / (distance / 100)`，distance 为 0 时不计算
- workout 的 total_distance、total_duration、avg_pace 由 splits 汇总计算

### 索引

- `workout(user_id, date)`
- `split(workout_id, seq)`

## 4. 页面与功能

| 页面 | 路径 | 功能 |
|------|------|------|
| 登录 | `/pages/login` | 微信授权 → 手机号授权 → 存 token |
| 首页 | `/pages/index` | 泳姿进步卡片（可拖动排序）+ 训练列表（按日期倒序） |
| 新增/编辑 | `/pages/record` | 表单：日期、主泳姿、泳池类型、分段列表 |
| 详情 | `/pages/detail` | 单次训练详情 + 分段明细 + 分享 |
| 统计 | `/pages/stats` | 周/月报表、趋势曲线、PR、热力图 |

**TabBar：** 首页 | 统计

### 首页 · 泳姿进步

- 四种泳姿卡片：自由泳、蛙泳、仰泳、蝶泳
- 每种展示 50/100/200/400 米配速及较上周变化
- **长按拖动自定义排序**，松手后保存顺序
- 排序持久化：`uni.setStorageSync('stroke_order')`，登录后同步至服务端 `user.stroke_order`（JSON 数组）
- 点击进入该泳姿速度趋势页

### 新增记录流程

1. 选日期、主泳姿、泳池类型（25m/50m）
2. 添加分段：泳姿 + 距离 + 时长（可增删）
3. 前端实时预览总距离/总时长/平均配速
4. 提交保存

### 统计页

- 周/月：总距离、总时长、训练次数
- 趋势图：按日/周距离折线
- PR：各泳姿最快配速、最长距离
- 热力图：日历格子，颜色深浅 = 当天训练量

### 分享

详情页按钮 → canvas 生成训练摘要卡片 → 微信转发

## 5. API 设计

### 统一响应

```json
{ "code": 0, "msg": "ok", "data": {} }
```

### 鉴权

Header: `Authorization: Bearer <token>`，JWT 有效期 7 天

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/wx-login` | 微信 code 换 openid，返回 token |
| POST | `/api/auth/bind-phone` | 绑定手机号（需登录） |
| GET | `/api/workouts` | 训练列表 `?page=1&size=20` |
| POST | `/api/workouts` | 新增训练（含 splits 数组） |
| GET | `/api/workouts/{id}` | 训练详情 |
| PUT | `/api/workouts/{id}` | 编辑训练 |
| DELETE | `/api/workouts/{id}` | 删除训练 |
| GET | `/api/stats/summary` | 统计汇总 `?period=week\|month` |
| GET | `/api/stats/trend` | 趋势数据 `?days=30` |
| GET | `/api/stats/pr` | 各泳姿 PR |
| GET | `/api/stats/heatmap` | 热力图数据 `?year=2026` |

### 新增训练请求体

```json
{
  "date": "2026-06-29",
  "stroke": "自由泳",
  "pool_type": 1,
  "note": "",
  "splits": [
    { "seq": 1, "stroke": "自由泳", "distance": 200, "duration": 240 },
    { "seq": 2, "stroke": "蛙泳", "distance": 100, "duration": 150 }
  ]
}
```

## 6. 登录流程

```
用户打开小程序
  → uni.login() 拿 code
  → POST /api/auth/wx-login { code }
  → 后端调微信 code2session 换 openid，查/建 user，返回 JWT
  → 若 phone 为空，弹 getPhoneNumber 授权组件
  → POST /api/auth/bind-phone { code }
  → 后端调微信接口解密手机号，更新 user
  → 跳转首页
```

## 7. 技术选型

| 层 | 选型 | 协议 |
|----|------|------|
| 小程序 | uni-app + Vue3 + TS | Apache 2.0 |
| UI | uni-ui | MIT |
| 图表 | uCharts | 免费商用 |
| 状态 | Pinia | MIT |
| 后端 | Spring Boot 3.2 + JDK 17 | Apache 2.0 |
| ORM | MyBatis-Plus | Apache 2.0 |
| 数据库 | MySQL 8.0 | GPL（社区版） |
| 鉴权 | JWT（jjwt 0.12） | Apache 2.0 |

## 8. 合规要求

- 首次打开弹《隐私政策》+《用户协议》，同意后才收集数据
- 小程序后台配置隐私保护指引
- API 域名加入微信后台 request 合法域名白名单
- 强制 HTTPS
- 手机号收集用途在隐私指引中声明
- 需企业主体小程序才能使用 getPhoneNumber

## 9. 错误处理

- 后端 `@RestControllerAdvice` 统一异常拦截
- 401 前端清 token 跳登录页
- 网络失败显示重试按钮
- 表单校验：距离 > 0、时长 > 0、至少 1 段

## 10. 一期不做（YAGNI）

- 社交/评论
- 教练/课程/约课
- 智能手表同步
- 支付功能
