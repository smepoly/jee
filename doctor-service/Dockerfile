FROM eclipse-temurin:17-jre
WORKDIR /app
# Requires target/doctor-service-0.0.1-SNAPSHOT.jar to exist (run mvn -DskipTests package locally)
COPY target/doctor-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]
