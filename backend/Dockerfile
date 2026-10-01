FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="wuye-points"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 目录创建与授权必须在切换到非 root 用户之前完成：
# Alpine 镜像中 / 为 root:root 755，appuser 无权创建 /data，否则构建阶段即失败。
RUN mkdir -p /data/uploads /opt/wuye-points/cert \
    && chown -R appuser:appgroup /data /opt/wuye-points

WORKDIR /app

COPY target/wuye-points.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENV TZ=Asia/Shanghai
# 使用容器内存百分比而非固定值，适配不同规格的部署环境；
# ExitOnOutOfMemoryError 保证 OOM 时进程退出由编排层重启，避免"假死"。
ENV JAVA_OPTS="-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Duser.timezone=Asia/Shanghai"

EXPOSE 8888

# 容器编排依赖健康检查判定实例可用性
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:8888/api/v1/actuator/health || exit 1

# exec 保证 java 进程接管 PID 1，SIGTERM 能被 Spring Boot 接收并执行优雅停机，
# 否则编排层停止容器时会直接杀进程，可能中断支付/分账事务。
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
