FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="wuye-points"

WORKDIR /app

COPY target/wuye-points.jar app.jar

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Duser.timezone=Asia/Shanghai"

RUN mkdir -p /data/uploads /opt/wuye-points/cert

EXPOSE 8888

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
