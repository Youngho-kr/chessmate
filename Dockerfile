FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

COPY *.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]