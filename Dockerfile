# ==========================================
# Stage 1: Build
# ==========================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and project configuration
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Give Maven wrapper execute permission
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests


# ==========================================
# Stage 2: Run
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated Spring Boot JAR
COPY --from=build /app/target/real-estate-backend-v1.jar app.jar

# Application port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]