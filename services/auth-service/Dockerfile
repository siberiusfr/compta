FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy compta-commons from local maven repo or parent context
COPY ../../compta-commons /app/compta-commons
RUN apk add --no-cache maven && \
    cd /app/compta-commons && \
    mvn clean install -DskipTests

# Copy auth-service files
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests && \
    apk del maven

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
