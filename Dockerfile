FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew --no-daemon :api:capstone-api:bootJar -x test --parallel --build-cache

RUN cd api/capstone-api/build/libs && \
    JAR_NAME=$(ls -1 *.jar | grep -v 'plain' | head -n 1) && \
    mv "$JAR_NAME" app.jar

FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/api/capstone-api/build/libs/app.jar /app/app.jar

RUN groupadd -r spring && useradd -r -g spring spring && \
    chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

