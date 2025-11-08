FROM eclipse-temurin:17-jdk-alpine

# Add maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom.xml and properties file
COPY pom.xml .
COPY src/main/resources/application-dev.properties src/main/resources/

RUN mvn dependency:go-offline
# Copy source
COPY src ./src

# Build
RUN mvn package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "target/backendapp-0.0.1-SNAPSHOT.jar"]