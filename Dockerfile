# Use an official Java runtime as base
FROM eclipse-temurin:24-jdk

# Set the working directory
WORKDIR /urlShort

# Copy the built JAR into the container
COPY target/urlShort-0.0.1-SNAPSHOT.jar UrlShortApplication.jar

# Expose the app port (customize as needed)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "UrlShortApplication.jar"]