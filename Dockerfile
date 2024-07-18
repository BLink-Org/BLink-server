FROM openjdk:17-jdk-slim
WORKDIR /spring
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} /spring/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/spring/app.jar"]