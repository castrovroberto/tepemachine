FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/apiwg-0.1.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"] 