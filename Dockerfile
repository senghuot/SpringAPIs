# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 as maven-builder
WORKDIR /etd
COPY pom.xml .
COPY src ./src
COPY src /etd/src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21-jdk-slim
WORKDIR /app
EXPOSE 8080
COPY --from=maven-builder /etd/target/SpringAPIs-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]