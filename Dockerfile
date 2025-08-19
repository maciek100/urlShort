# --- Runtime stage ---
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /urlShort

# Copy the built JAR
COPY target/urlShort-0.0.1-SNAPSHOT.jar UrlShortApplication.jar

# Expose HTTP port
EXPOSE 8080

# Optional: expose debug port if needed
# EXPOSE 5005

# Run the app (without remote debug by default)
ENTRYPOINT ["java", "-jar", "UrlShortApplication.jar"]
