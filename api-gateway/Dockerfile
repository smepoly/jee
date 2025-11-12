FROM eclipse-temurin:17-jre
WORKDIR /app
# Expect the built jar to be present in target/ (built on host). If not present, run `mvn -DskipTests package` in the service folder before building the image.
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]
