FROM eclipse-temurin:17-jre

WORKDIR /app

# 复制JAR文件
COPY target/gateway-service.jar app.jar

# 暴露端口
EXPOSE 8090

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
