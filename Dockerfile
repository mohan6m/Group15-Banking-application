# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot application JAR file into the container
COPY target/bankapp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 9120

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]