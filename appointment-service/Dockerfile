FROM eclipse-temurin:17-jre
WORKDIR /app
# Requires target/appointment-service-0.0.1-SNAPSHOT.jar to exist (run mvn -DskipTests package locally)
COPY target/appointment-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","/app/app.jar"]
