FROM openjdk:20
WORKDIR /app
COPY ./target/FileServer-jar-with-dependencies.jar .
EXPOSE 8080
CMD ["java", "-jar", "FileServer-jar-with-dependencies.jar"]