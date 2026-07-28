FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-jar", "/app/app.jar"]
