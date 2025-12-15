FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy compta-commons first (needs to be built)
COPY ../compta-commons /app/compta-commons
RUN cd /app/compta-commons && apk add --no-cache maven && mvn clean install -DskipTests

# Copy gateway files
COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    apk del maven

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
