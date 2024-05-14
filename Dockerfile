FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY .. /app/.
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/messenger-server/target/messenger-server-1.0-SNAPSHOT.jar /app/server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/server.jar"]