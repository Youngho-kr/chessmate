FROM eclipse-temurin:25-jdk-jammy

RUN apt-get update %% ap-get install -y stockfish && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY *.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]