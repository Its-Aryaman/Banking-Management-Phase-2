# Use Java 17 JRE for running Spring Boot
FROM eclipse-temurin:17-jre-jammy

# Set working directory inside container
WORKDIR /app

# Copy the built JAR from target folder into the container
COPY target/*.jar app.jar

# Recommended JVM options
ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Expose the port (matches server.port in properties or env)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

