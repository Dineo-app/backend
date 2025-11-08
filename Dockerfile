FROM eclipse-temurin:17-jdk-alpine

# Add maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (improves Docker layer caching)
RUN mvn dependency:go-offline -B

# Copy entire source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -B

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/backendapp-0.0.1-SNAPSHOT.jar"]