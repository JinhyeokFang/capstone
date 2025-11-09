FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY common-domain/build.gradle.kts common-domain/
COPY common-util/build.gradle.kts common-util/
COPY api/build.gradle.kts api/

RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies --parallel || true

COPY . .

RUN ./gradlew --no-daemon :api:bootJar -x test --parallel --build-cache

RUN cd api/build/libs && \
    JAR_NAME=$(ls -1 *.jar | grep -v 'plain' | head -n 1) && \
    mv "$JAR_NAME" app.jar

FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/api/build/libs/app.jar /app/app.jar

RUN groupadd -r spring && useradd -r -g spring spring && \
    chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

