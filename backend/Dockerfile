# Stage 1: Build the Spring Boot application using Maven and Java 23
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the packaged application in a lightweight Java 23 JRE environment
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/target/gym-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
