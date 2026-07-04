# ===== 构建阶段：用 maven 镜像编译 jar，云主机无需安装 java/maven =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷 pom.xml，利用层缓存提前下载依赖（后续改代码不会重复下载）
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 拷源码并打包（跳过测试，CI/部署阶段一般不跑测试）
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ===== 运行阶段：精简 JRE 镜像，只跑 jar =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区设为东八区，日志时间正确
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# 用 SECONDS + 内存参数，生产可按服务器内存调整
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
