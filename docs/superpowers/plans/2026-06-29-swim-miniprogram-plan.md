# 游泳训练记录小程序 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付一期游泳训练记录微信小程序（uni-app）+ Spring Boot 后端，支持训练记录 CRUD、统计可视化、分享。

**Architecture:** uni-app 小程序通过 HTTPS 调 Spring Boot REST API；后端 MyBatis-Plus 操作 MySQL；JWT 鉴权；workout + split 主子表存储训练数据。

**Tech Stack:** uni-app + Vue3 + TS + Pinia + uni-ui + uCharts | Spring Boot 3.2 + JDK 17 + MyBatis-Plus + MySQL 8 + jjwt 0.12

## Global Constraints

- 平台：微信小程序（中国大陆）
- 小程序框架：uni-app + Vue3 + TypeScript
- UI：uni-ui（MIT）
- 图表：uCharts（免费商用）
- 状态：Pinia
- 后端：Spring Boot 3.2 + JDK 17 + MyBatis-Plus
- 数据库：MySQL 8.0
- 鉴权：JWT 7 天有效期，Header `Authorization: Bearer <token>`
- 响应格式：`{ "code": 0, "msg": "ok", "data": {} }`
- 泳姿枚举：自由泳/蛙泳/仰泳/蝶泳/混合
- 泳池类型：1=25米，2=50米
- 配速公式：`pace = duration / (distance / 100)`，distance=0 不算
- 合规：首次打开弹隐私政策+用户协议
- 一期不做：社交、教练课程、手表同步、支付

---

## 文件结构总览

```
游泳小程序/
├── miniprogram/
│   ├── pages/login/index.vue
│   ├── pages/index/index.vue
│   ├── pages/record/index.vue
│   ├── pages/detail/index.vue
│   ├── pages/stats/index.vue
│   ├── api/auth.ts
│   ├── api/workout.ts
│   ├── api/stats.ts
│   ├── store/user.ts
│   ├── utils/request.ts
│   ├── utils/pace.ts
│   ├── utils/share-card.ts
│   ├── components/privacy-popup.vue
│   ├── components/split-form.vue
│   ├── pages.json
│   └── manifest.json
├── server/
│   ├── pom.xml
│   ├── src/main/resources/application.yml
│   ├── src/main/resources/db/schema.sql
│   └── src/main/java/com/swim/
│       ├── SwimApplication.java
│       ├── common/Result.java
│       ├── common/BusinessException.java
│       ├── common/GlobalExceptionHandler.java
│       ├── config/JwtConfig.java
│       ├── config/WebConfig.java
│       ├── config/WxConfig.java
│       ├── util/JwtUtil.java
│       ├── util/PaceCalculator.java
│       ├── entity/User.java
│       ├── entity/Workout.java
│       ├── entity/Split.java
│       ├── mapper/UserMapper.java
│       ├── mapper/WorkoutMapper.java
│       ├── mapper/SplitMapper.java
│       ├── dto/WxLoginRequest.java
│       ├── dto/BindPhoneRequest.java
│       ├── dto/WorkoutRequest.java
│       ├── dto/SplitRequest.java
│       ├── dto/LoginResponse.java
│       ├── service/AuthService.java
│       ├── service/WxService.java
│       ├── service/WorkoutService.java
│       ├── service/StatsService.java
│       ├── controller/AuthController.java
│       ├── controller/WorkoutController.java
│       └── controller/StatsController.java
└── server/src/test/java/com/swim/
    ├── util/PaceCalculatorTest.java
    ├── service/WorkoutServiceTest.java
    └── controller/AuthControllerTest.java
```

---

### Task 1: 后端脚手架 + 数据库

**Files:**
- Create: `server/pom.xml`
- Create: `server/src/main/resources/application.yml`
- Create: `server/src/main/resources/db/schema.sql`
- Create: `server/src/main/java/com/swim/SwimApplication.java`
- Create: `server/src/main/java/com/swim/common/Result.java`

**Interfaces:**
- Produces: `Result<T>` 统一响应类，`schema.sql` 建表脚本

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    <groupId>com.swim</groupId>
    <artifactId>swim-server</artifactId>
    <version>1.0.0</version>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.6</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建 application.yml**

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:swim}
    username: ${DB_USERNAME:your-username}
    password: ${DB_PASSWORD:your-password}
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
wx:
  appid: ${WX_APPID:your-appid}
  secret: ${WX_SECRET:your-secret}
jwt:
  secret: ${JWT_SECRET:please-set-a-long-random-secret-at-least-256-bits}
  expire-days: 7
```

- [ ] **Step 3: 创建 schema.sql**

```sql
CREATE DATABASE IF NOT EXISTS swim DEFAULT CHARSET utf8mb4;
USE swim;

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL UNIQUE,
  phone VARCHAR(11),
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workout (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  date DATE NOT NULL,
  stroke VARCHAR(20) NOT NULL,
  pool_type TINYINT NOT NULL,
  total_distance INT NOT NULL,
  total_duration INT NOT NULL,
  avg_pace DECIMAL(6,2),
  note VARCHAR(200),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_date (user_id, date)
);

CREATE TABLE split (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workout_id BIGINT NOT NULL,
  seq INT NOT NULL,
  stroke VARCHAR(20) NOT NULL,
  distance INT NOT NULL,
  duration INT NOT NULL,
  pace DECIMAL(6,2),
  INDEX idx_workout_seq (workout_id, seq)
);
```

- [ ] **Step 4: 创建 Result.java 和启动类**

```java
// server/src/main/java/com/swim/common/Result.java
package com.swim.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.msg = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
```

```java
// server/src/main/java/com/swim/SwimApplication.java
package com.swim;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.swim.mapper")
public class SwimApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwimApplication.class, args);
    }
}
```

- [ ] **Step 5: 执行建表并验证启动**

Run: `mysql -u root -p < server/src/main/resources/db/schema.sql`
Run: `cd server && mvn spring-boot:run`
Expected: 启动成功，端口 8080 监听

- [ ] **Step 6: Commit**

```bash
git add server/
git commit -m "feat: scaffold Spring Boot backend with DB schema"
```

---

### Task 2: 配速计算工具（TDD）

**Files:**
- Create: `server/src/main/java/com/swim/util/PaceCalculator.java`
- Create: `server/src/test/java/com/swim/util/PaceCalculatorTest.java`

**Interfaces:**
- Produces: `PaceCalculator.calcPace(int distance, int duration) -> BigDecimal`
- Produces: `PaceCalculator.aggregate(List<SplitRequest>) -> {totalDistance, totalDuration, avgPace}`

- [ ] **Step 1: 写失败测试**

```java
package com.swim.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaceCalculatorTest {

    @Test
    void calcPace_normal() {
        BigDecimal pace = PaceCalculator.calcPace(200, 240);
        assertEquals(new BigDecimal("120.00"), pace);
    }

    @Test
    void calcPace_zeroDistance() {
        assertNull(PaceCalculator.calcPace(0, 240));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd server && mvn test -Dtest=PaceCalculatorTest`
Expected: FAIL "PaceCalculator not found"

- [ ] **Step 3: 实现 PaceCalculator**

```java
package com.swim.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PaceCalculator {

    public static BigDecimal calcPace(int distance, int duration) {
        if (distance <= 0) return null;
        return BigDecimal.valueOf(duration)
                .divide(BigDecimal.valueOf(distance / 100.0), 2, RoundingMode.HALF_UP);
    }

    public record Aggregate(int totalDistance, int totalDuration, BigDecimal avgPace) {}

    public static Aggregate aggregate(int totalDistance, int totalDuration) {
        return new Aggregate(totalDistance, totalDuration, calcPace(totalDistance, totalDuration));
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd server && mvn test -Dtest=PaceCalculatorTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/src/main/java/com/swim/util/ server/src/test/
git commit -m "feat: add PaceCalculator with tests"
```

---

### Task 3: 实体 + Mapper + JWT 工具

**Files:**
- Create: `server/src/main/java/com/swim/entity/User.java`
- Create: `server/src/main/java/com/swim/entity/Workout.java`
- Create: `server/src/main/java/com/swim/entity/Split.java`
- Create: `server/src/main/java/com/swim/mapper/UserMapper.java`
- Create: `server/src/main/java/com/swim/mapper/WorkoutMapper.java`
- Create: `server/src/main/java/com/swim/mapper/SplitMapper.java`
- Create: `server/src/main/java/com/swim/util/JwtUtil.java`
- Create: `server/src/main/java/com/swim/config/JwtConfig.java`

**Interfaces:**
- Produces: `JwtUtil.generateToken(Long userId) -> String`
- Produces: `JwtUtil.parseUserId(String token) -> Long`
- Produces: Entity + Mapper CRUD

- [ ] **Step 1: 创建三个 Entity（Lombok @Data）**

```java
// User.java - fields: id, openid, phone, nickname, avatar, createdAt
// Workout.java - fields: id, userId, date, stroke, poolType, totalDistance, totalDuration, avgPace, note, createdAt
// Split.java - fields: id, workoutId, seq, stroke, distance, duration, pace
// 均使用 @TableName 注解
```

- [ ] **Step 2: 创建三个 Mapper 接口**

```java
package com.swim.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swim.entity.User;
public interface UserMapper extends BaseMapper<User> {}
// WorkoutMapper, SplitMapper 同理
```

- [ ] **Step 3: 实现 JwtUtil**

```java
package com.swim.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private final SecretKey key;
    private final long expireMs;

    public JwtUtil(String secret, int expireDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = (long) expireDays * 24 * 60 * 60 * 1000;
    }

    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }
}
```

- [ ] **Step 4: 创建 JwtConfig Bean + WebConfig 拦截器**

WebConfig 拦截 `/api/**` 排除 `/api/auth/**`，从 Header 解析 userId 放入 request attribute。

- [ ] **Step 5: Commit**

```bash
git add server/src/main/java/com/swim/
git commit -m "feat: add entities, mappers, JWT auth"
```

---

### Task 4: 微信登录 + 手机号绑定

**Files:**
- Create: `server/src/main/java/com/swim/service/WxService.java`
- Create: `server/src/main/java/com/swim/service/AuthService.java`
- Create: `server/src/main/java/com/swim/controller/AuthController.java`
- Create: `server/src/main/java/com/swim/dto/WxLoginRequest.java`
- Create: `server/src/main/java/com/swim/dto/BindPhoneRequest.java`
- Create: `server/src/main/java/com/swim/dto/LoginResponse.java`
- Create: `server/src/main/java/com/swim/config/WxConfig.java`
- Test: `server/src/test/java/com/swim/controller/AuthControllerTest.java`

**Interfaces:**
- Consumes: `JwtUtil`, `UserMapper`, `WxConfig`
- Produces: `POST /api/auth/wx-login` 返回 `{ token, needBindPhone }`
- Produces: `POST /api/auth/bind-phone` 返回 `{ phone }`

- [ ] **Step 1: 写 AuthController 集成测试（Mock WxService）**

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean AuthService authService;

    @Test
    void wxLogin_success() throws Exception {
        LoginResponse resp = new LoginResponse("token123", true);
        when(authService.wxLogin("code")).thenReturn(resp);
        mockMvc.perform(post("/api/auth/wx-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test-code\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.token").value("token123"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd server && mvn test -Dtest=AuthControllerTest`
Expected: FAIL

- [ ] **Step 3: 实现 WxService（调微信 code2session + getPhoneNumber）**

```java
// WxService.java
// code2Session: GET https://api.weixin.qq.com/sns/jscode2session?appid=&secret=&js_code=&grant_type=authorization_code
// getPhoneNumber: POST https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=
// access_token 通过 GET https://api.weixin.qq.com/cgi-bin/token 获取并缓存
```

- [ ] **Step 4: 实现 AuthService + AuthController**

```java
// AuthService.wxLogin(code):
//   1. wxService.code2Session(code) -> openid
//   2. 查 user by openid，不存在则 insert
//   3. 生成 JWT，返回 LoginResponse(token, phone==null)

// AuthService.bindPhone(userId, code):
//   1. wxService.getPhoneNumber(code) -> phone
//   2. 更新 user.phone
//   3. 返回 phone
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd server && mvn test -Dtest=AuthControllerTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add server/
git commit -m "feat: add WeChat login and phone binding"
```

---

### Task 5: 训练记录 CRUD

**Files:**
- Create: `server/src/main/java/com/swim/service/WorkoutService.java`
- Create: `server/src/main/java/com/swim/controller/WorkoutController.java`
- Create: `server/src/main/java/com/swim/dto/WorkoutRequest.java`
- Create: `server/src/main/java/com/swim/dto/SplitRequest.java`
- Create: `server/src/main/java/com/swim/common/BusinessException.java`
- Create: `server/src/main/java/com/swim/common/GlobalExceptionHandler.java`
- Test: `server/src/test/java/com/swim/service/WorkoutServiceTest.java`

**Interfaces:**
- Consumes: `PaceCalculator`, `WorkoutMapper`, `SplitMapper`
- Produces: CRUD endpoints `/api/workouts`

- [ ] **Step 1: 写 WorkoutServiceTest**

```java
@SpringBootTest
@Transactional
class WorkoutServiceTest {
    @Autowired WorkoutService workoutService;
    @Autowired UserMapper userMapper;

    @Test
    void createWorkout_calculatesPace() {
        User user = new User();
        user.setOpenid("test-openid");
        userMapper.insert(user);

        WorkoutRequest req = new WorkoutRequest();
        req.setDate(LocalDate.of(2026, 6, 29));
        req.setStroke("自由泳");
        req.setPoolType(1);
        SplitRequest s1 = new SplitRequest();
        s1.setSeq(1); s1.setStroke("自由泳"); s1.setDistance(200); s1.setDuration(240);
        req.setSplits(List.of(s1));

        Workout result = workoutService.create(user.getId(), req);
        assertEquals(200, result.getTotalDistance());
        assertEquals(240, result.getTotalDuration());
        assertEquals(new BigDecimal("120.00"), result.getAvgPace());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd server && mvn test -Dtest=WorkoutServiceTest`
Expected: FAIL

- [ ] **Step 3: 实现 WorkoutService**

```java
@Transactional
public Workout create(Long userId, WorkoutRequest req) {
    validate(req);
    int totalDist = req.getSplits().stream().mapToInt(SplitRequest::getDistance).sum();
    int totalDur = req.getSplits().stream().mapToInt(SplitRequest::getDuration).sum();
    var agg = PaceCalculator.aggregate(totalDist, totalDur);

    Workout w = new Workout();
    w.setUserId(userId);
    w.setDate(req.getDate());
    w.setStroke(req.getStroke());
    w.setPoolType(req.getPoolType());
    w.setNote(req.getNote());
    w.setTotalDistance(agg.totalDistance());
    w.setTotalDuration(agg.totalDuration());
    w.setAvgPace(agg.avgPace());
    workoutMapper.insert(w);

    for (SplitRequest sr : req.getSplits()) {
        Split split = new Split();
        split.setWorkoutId(w.getId());
        split.setSeq(sr.getSeq());
        split.setStroke(sr.getStroke());
        split.setDistance(sr.getDistance());
        split.setDuration(sr.getDuration());
        split.setPace(PaceCalculator.calcPace(sr.getDistance(), sr.getDuration()));
        splitMapper.insert(split);
    }
    return w;
}

private void validate(WorkoutRequest req) {
    if (req.getSplits() == null || req.getSplits().isEmpty())
        throw new BusinessException(400, "至少1段");
    for (SplitRequest s : req.getSplits()) {
        if (s.getDistance() <= 0 || s.getDuration() <= 0)
            throw new BusinessException(400, "距离和时长必须大于0");
    }
}
```

- [ ] **Step 4: 实现 WorkoutController（list/create/get/update/delete）**

所有接口从 request attribute 取 userId，update/delete 校验 workout.userId == userId。

- [ ] **Step 5: 运行测试确认通过**

Run: `cd server && mvn test -Dtest=WorkoutServiceTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add server/
git commit -m "feat: add workout CRUD with pace calculation"
```

---

### Task 6: 统计 API

**Files:**
- Create: `server/src/main/java/com/swim/service/StatsService.java`
- Create: `server/src/main/java/com/swim/controller/StatsController.java`

**Interfaces:**
- Consumes: `WorkoutMapper`, `SplitMapper`
- Produces:
  - `GET /api/stats/summary?period=week|month` → `{ totalDistance, totalDuration, count }`
  - `GET /api/stats/trend?days=30` → `[{ date, distance }]`
  - `GET /api/stats/pr` → `[{ stroke, bestPace, maxDistance }]`
  - `GET /api/stats/heatmap?year=2026` → `[{ date, distance }]`

- [ ] **Step 1: 实现 StatsService 四个方法**

```java
// summary: 按 period 算 date >= startDate 的 SUM(total_distance), SUM(total_duration), COUNT(*)
// trend: GROUP BY date ORDER BY date, 最近 days 天
// pr: 从 split 表 GROUP BY stroke, MIN(pace), MAX(distance)
// heatmap: GROUP BY date for year, 返回 date+distance
```

- [ ] **Step 2: 实现 StatsController 四个 GET 端点**

- [ ] **Step 3: 手动验证**

Run: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/stats/summary?period=week`
Expected: `{ "code": 0, "data": { "totalDistance": ..., "totalDuration": ..., "count": ... } }`

- [ ] **Step 4: Commit**

```bash
git add server/
git commit -m "feat: add stats API (summary, trend, pr, heatmap)"
```

---

### Task 6.5: 交互原型设计稿（用户确认后再开发）

**Files:**
- Create: `prototype/index.html`

**Interfaces:**
- Produces: 6 个可交互页面原型（登录/首页/新增/详情/统计/分享）

- [ ] **Step 1: 打开原型**

Run: 浏览器打开 `prototype/index.html`
Expected: 右侧导航可切换 6 个页面，手机框内可点击交互

- [ ] **Step 2: 用户确认设计稿**

用户逐页确认布局、配色、交互流程，提出修改意见

- [ ] **Step 3: 根据反馈修改原型**

修改 `prototype/index.html` 直到用户确认

- [ ] **Step 4: 用户签字确认后才开始 Task 7**

**⛔ 此 Task 未完成前，禁止开始 uni-app 开发**

---

### Task 7: uni-app 脚手架 + 请求封装

**Files:**
- Create: `miniprogram/` (通过 HBuilderX 或 CLI 创建 uni-app Vue3+TS 项目)
- Create: `miniprogram/utils/request.ts`
- Create: `miniprogram/utils/pace.ts`
- Create: `miniprogram/store/user.ts`
- Modify: `miniprogram/pages.json`

**Interfaces:**
- Produces: `request<T>(options)` 自动带 token、401 跳登录
- Produces: `formatPace(seconds: number) -> string` 如 "2'00\"/100m"

- [ ] **Step 1: 创建 uni-app 项目**

Run: `npx degit dcloudio/uni-preset-vue#vite-ts miniprogram`
Run: `cd miniprogram && npm install && npm install pinia @dcloudio/uni-ui`

- [ ] **Step 2: 配置 pages.json TabBar + 五个页面路由**

```json
{
  "pages": [
    { "path": "pages/login/index", "style": { "navigationBarTitleText": "登录" } },
    { "path": "pages/index/index", "style": { "navigationBarTitleText": "训练记录" } },
    { "path": "pages/record/index", "style": { "navigationBarTitleText": "新增训练" } },
    { "path": "pages/detail/index", "style": { "navigationBarTitleText": "训练详情" } },
    { "path": "pages/stats/index", "style": { "navigationBarTitleText": "统计" } }
  ],
  "tabBar": {
    "list": [
      { "pagePath": "pages/index/index", "text": "首页", "iconPath": "static/home.png", "selectedIconPath": "static/home-active.png" },
      { "pagePath": "pages/stats/index", "text": "统计", "iconPath": "static/stats.png", "selectedIconPath": "static/stats-active.png" }
    ]
  }
}
```

- [ ] **Step 3: 实现 request.ts**

```typescript
const BASE_URL = 'https://your-domain.com'

export function request<T>(options: { url: string; method?: string; data?: any }): Promise<T> {
  const token = uni.getStorageSync('token')
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: { Authorization: token ? `Bearer ${token}` : '' },
      success: (res: any) => {
        if (res.data.code === 0) resolve(res.data.data)
        else if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(res.data)
        } else {
          uni.showToast({ title: res.data.msg, icon: 'none' })
          reject(res.data)
        }
      },
      fail: () => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(new Error('network'))
      }
    })
  })
}
```

- [ ] **Step 4: 实现 pace.ts**

```typescript
export function calcPace(distance: number, duration: number): number | null {
  if (distance <= 0) return null
  return Math.round((duration / (distance / 100)) * 100) / 100
}

export function formatPace(pace: number): string {
  const min = Math.floor(pace / 60)
  const sec = Math.round(pace % 60)
  return `${min}'${sec.toString().padStart(2, '0')}"/100m`
}
```

- [ ] **Step 5: Commit**

```bash
git add miniprogram/
git commit -m "feat: scaffold uni-app with request util and pace helper"
```

---

### Task 8: 登录页 + 隐私弹窗

**Files:**
- Create: `miniprogram/pages/login/index.vue`
- Create: `miniprogram/components/privacy-popup.vue`
- Create: `miniprogram/api/auth.ts`

**Interfaces:**
- Consumes: `request`, `store/user`
- Produces: 登录成功后存 token 跳首页

- [ ] **Step 1: 实现 api/auth.ts**

```typescript
import { request } from '@/utils/request'

export function wxLogin(code: string) {
  return request<{ token: string; needBindPhone: boolean }>({
    url: '/api/auth/wx-login', method: 'POST', data: { code }
  })
}

export function bindPhone(code: string) {
  return request<{ phone: string }>({
    url: '/api/auth/bind-phone', method: 'POST', data: { code }
  })
}
```

- [ ] **Step 2: 实现 privacy-popup.vue**

首次打开检查 `uni.getStorageSync('privacyAgreed')`，未同意则弹窗展示协议文本+同意按钮。

- [ ] **Step 3: 实现 login/index.vue**

```vue
<!-- 流程: privacy-popup → uni.login() → wxLogin → 若 needBindPhone 显示 getPhoneNumber 按钮 → bindPhone → 存 token → reLaunch 首页 -->
<button open-type="getPhoneNumber" @getphonenumber="onGetPhone">授权手机号</button>
```

- [ ] **Step 4: 微信开发者工具验证登录流程**

- [ ] **Step 5: Commit**

```bash
git add miniprogram/
git commit -m "feat: add login page with privacy popup"
```

---

### Task 9: 首页 + 新增/编辑训练

**Files:**
- Create: `miniprogram/pages/index/index.vue`
- Create: `miniprogram/pages/record/index.vue`
- Create: `miniprogram/components/split-form.vue`
- Create: `miniprogram/api/workout.ts`

**Interfaces:**
- Consumes: `request`, `calcPace`, `formatPace`
- Produces: 列表页、表单页（支持新增和编辑 mode）

- [ ] **Step 1: 实现 api/workout.ts**

```typescript
export function getWorkouts(page = 1, size = 20) {
  return request<{ records: Workout[]; total: number }>({
    url: `/api/workouts?page=${page}&size=${size}`
  })
}
export function createWorkout(data: WorkoutForm) {
  return request({ url: '/api/workouts', method: 'POST', data })
}
export function updateWorkout(id: number, data: WorkoutForm) {
  return request({ url: `/api/workouts/${id}`, method: 'PUT', data })
}
export function deleteWorkout(id: number) {
  return request({ url: `/api/workouts/${id}`, method: 'DELETE' })
}
```

- [ ] **Step 2: 实现 split-form.vue 组件**

props: splits 数组，emit: update:splits。每行：泳姿 picker + 距离 input + 时长 input + 删除按钮。底部"添加分段"按钮。

- [ ] **Step 3: 实现 record/index.vue**

表单字段：日期 picker、主泳姿 picker、泳池类型 radio(25m/50m)、split-form、备注、实时显示汇总配速。onLoad 检查 id 参数决定新增/编辑。

- [ ] **Step 4: 实现 index/index.vue**

泳姿进步区：四张卡片可长按拖动排序（`touchstart/touchmove/touchend` 或 `movable-area`），顺序存 `stroke_order`，登录后 `PUT /api/user/stroke-order` 同步。训练列表区：卡片列表，下拉刷新，上拉加载，FAB 跳 record，点击跳 detail。

- [ ] **Step 5: 创建 components/stroke-card-list.vue**

props: `strokes[]`，emit: `reorder`。长按 500ms 进入拖动模式，拖动时卡片半透明+阴影，松手更新顺序并 `uni.setStorageSync('stroke_order', order)`。

- [ ] **Step 6: 后端 user 表加 stroke_order 字段 + API**

```sql
ALTER TABLE user ADD stroke_order VARCHAR(100) DEFAULT '["自由泳","蛙泳","仰泳","蝶泳"]';
```

`PUT /api/user/stroke-order` body: `{ "order": ["蛙泳","自由泳","仰泳","蝶泳"] }`

- [ ] **Step 7: 微信开发者工具验证拖动排序 + CRUD**

- [ ] **Step 8: Commit**

```bash
git add miniprogram/
git commit -m "feat: add workout list and record form pages"
```

---

### Task 10: 详情页 + 分享

**Files:**
- Create: `miniprogram/pages/detail/index.vue`
- Create: `miniprogram/utils/share-card.ts`

**Interfaces:**
- Consumes: `getWorkout(id)`, `formatPace`
- Produces: 详情展示 + canvas 分享卡片

- [ ] **Step 1: 实现 detail/index.vue**

展示 workout 全部字段 + splits 表格。底部：编辑按钮、删除按钮（confirm 后 deleteWorkout）、分享按钮。

- [ ] **Step 2: 实现 share-card.ts**

```typescript
export function drawShareCard(canvasId: string, workout: Workout): Promise<string> {
  // canvas 绘制：日期、泳姿、泳池类型、总距离、配速
  // 返回临时图片路径
  return new Promise((resolve) => {
    const ctx = uni.createCanvasContext(canvasId)
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, 300, 400)
    ctx.setFillStyle('#333')
    ctx.setFontSize(16)
    ctx.fillText(`${workout.date} ${workout.stroke}`, 20, 40)
    ctx.fillText(`${workout.totalDistance}m / ${formatPace(workout.avgPace)}`, 20, 80)
    ctx.draw(false, () => {
      uni.canvasToTempFilePath({ canvasId, success: (res) => resolve(res.tempFilePath) })
    })
  })
}
```

- [ ] **Step 3: 分享按钮调 uni.share 或 onShareAppMessage**

- [ ] **Step 4: 验证分享卡片生成**

- [ ] **Step 5: Commit**

```bash
git add miniprogram/
git commit -m "feat: add workout detail page with share card"
```

---

### Task 11: 统计页 + 图表

**Files:**
- Create: `miniprogram/pages/stats/index.vue`
- Create: `miniprogram/api/stats.ts`
- Install: uCharts

**Interfaces:**
- Consumes: stats API 四个端点
- Produces: 周/月切换、折线图、PR 列表、热力图

- [ ] **Step 1: 安装 uCharts**

Run: `cd miniprogram && npm install @qiun/ucharts`

- [ ] **Step 2: 实现 api/stats.ts**

四个 API 封装函数对应后端四个端点。

- [ ] **Step 3: 实现 stats/index.vue**

顶部 tab 切换 week/month → 调 summary API 显示三个数字卡片。
中部：qiun-data-charts 折线图绑 trend 数据。
下部：PR 列表（泳姿 + 最快配速 + 最长距离）。
底部：热力图（用 canvas 或 uCharts 日历热力）。

- [ ] **Step 4: 微信开发者工具验证图表渲染**

- [ ] **Step 5: Commit**

```bash
git add miniprogram/
git commit -m "feat: add stats page with charts and heatmap"
```

---

## Spec Coverage Check

| Spec 需求 | 对应 Task |
|-----------|-----------|
| 微信登录 + 手机号 | Task 4, 8 |
| 训练记录 CRUD | Task 5, 9 |
| 泳姿 + 分段 + 泳池类型 | Task 2, 5, 9 |
| 配速自动计算 | Task 2, 5 |
| 统计报表 | Task 6, 11 |
| 趋势曲线 + PR + 热力图 | Task 6, 11 |
| 分享 | Task 10 |
| 隐私合规 | Task 8 |
| JWT 鉴权 | Task 3 |
| 统一错误处理 | Task 5 |
| 401 跳登录 | Task 7 |

无遗漏。
