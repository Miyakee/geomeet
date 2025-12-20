# 后端 API Dockerfile - 支持从 GitHub 拉取代码构建
# 多阶段构建：构建阶段 + 运行阶段

# 构建阶段
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache git

# 复制 Gradle wrapper 和构建文件（利用 Docker 缓存）
COPY gradle/ ./gradle/
COPY gradlew ./
COPY gradlew.bat ./
COPY build.gradle ./
COPY settings.gradle ./

# 复制依赖配置文件（利用 Docker 缓存）
COPY src/ ./src/

# 给 gradlew 执行权限
RUN chmod +x ./gradlew

# 构建应用（跳过测试以加快构建速度）
RUN ./gradlew clean build -x test -x spotbugsMain -x checkstyleMain -x pmdMain -x checkstyleTest -x spotbugsTest -x pmdTest  --no-daemon

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/build/libs/api-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
