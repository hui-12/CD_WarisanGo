FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.61.0-noble

WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends \
		ffmpeg \
		curl \
		unzip \
		python3 \
		nodejs \
		libglib2.0-0 \
		libnss3 \
		libnspr4 \
		libatk1.0-0 \
		libatk-bridge2.0-0 \
		libcups2 \
		libdrm2 \
		libdbus-1-3 \
		libxkbcommon0 \
		libatspi2.0-0 \
		libxcomposite1 \
		libxdamage1 \
		libxfixes3 \
		libxrandr2 \
		libgbm1 \
		libpango-1.0-0 \
		libcairo2 \
		libasound2t64 \
		libxshmfence1 \
	&& rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://deno.land/install.sh | DENO_INSTALL=/usr/local sh

ARG YT_DLP_BUILD_VERSION=2026-09-04

RUN curl -L --fail --silent --show-error \
		-o /usr/local/bin/yt-dlp \
		https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
	&& chmod a+rx /usr/local/bin/yt-dlp \
	&& python3 --version \
	&& node --version \
	&& deno --version \
	&& yt-dlp --version \
	&& ffmpeg -version | head -n 1

COPY --from=build /app/target/warisango-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV TIKTOK_HEADLESS=true
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar app.jar"]
