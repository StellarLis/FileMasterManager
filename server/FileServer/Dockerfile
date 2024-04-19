FROM openjdk:20
WORKDIR /app/target
COPY ./target/FileServer-jar-with-dependencies.jar .
WORKDIR /app/userfiles
COPY ./userfiles .
WORKDIR /app
EXPOSE 8080
CMD ["java", "-jar", "./target/FileServer-jar-with-dependencies.jar"]