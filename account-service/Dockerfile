# Use Java 17 JRE for running Spring Boot
FROM eclipse-temurin:17-jre-jammy

# Set working directory inside container
WORKDIR /app

# Copy the built JAR from target folder into the container
COPY target/Banking-Application-0.0.1-SNAPSHOT.jar app.jar

# Recommended JVM options
ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Expose the port (matches server.port in properties or env)
EXPOSE 8081

# Run the JAR
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
