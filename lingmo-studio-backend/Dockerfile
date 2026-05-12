# 后端 Dockerfile
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8567
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
