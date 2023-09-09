FROM openjdk:20
FROM maven:latest
WORKDIR /app
COPY . .
RUN mvn clean compile assembly:single
WORKDIR /app/target
EXPOSE 8080
CMD ["java", "-jar", "FileServer-jar-with-dependencies.jar"]