FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.61.0-noble

WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends ffmpeg yt-dlp \
	&& rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/warisango-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV TIKTOK_HEADLESS=true
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar app.jar"]