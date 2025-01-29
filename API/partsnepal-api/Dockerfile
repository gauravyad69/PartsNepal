# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper files
COPY ./gradlew ./gradlew
COPY ./gradle ./gradle

# Copy the source code and build the application
COPY . /app

# Make Gradlew executable
RUN chmod +x ./gradlew

# Install dependencies and build the application
RUN ./gradlew build

# Expose the port your app will run on (default for Ktor)
EXPOSE 9090

# Run the application (replace with your Ktor app entry point if different)
CMD ["java", "-jar", "build/libs/np.com.parts.api-all.jar"]
