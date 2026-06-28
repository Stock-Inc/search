FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon --no-watch-fs
COPY src ./src
RUN gradle bootJar --no-daemon --no-watch-fs -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]