# Stage 1: build the React frontend
FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: build the Spring Boot backend, embedding the frontend build as static resources
FROM maven:3.9.16-eclipse-temurin-21 AS backend-build
WORKDIR /backend
# Copy only the pom first and resolve dependencies, so this (slow) layer stays cached
# by Docker across rebuilds as long as pom.xml doesn't change - only source edits
# will re-trigger the steps below it.
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn -B clean package -DskipTests

# Stage 3: runtime - just a JRE and the built jar, nothing else
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /backend/target/proxy-manager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
