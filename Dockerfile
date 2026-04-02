FROM eclipse-temurin:17-jdk

WORKDIR /app

ARG JAR_FILE=ubuntu-gateway.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080
EXPOSE 18080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
