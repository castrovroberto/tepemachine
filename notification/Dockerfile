FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/notification-0.1.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"] 