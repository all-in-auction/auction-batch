FROM openjdk:17-jdk

CMD ["./gradlew", "clean", "build"]

VOLUME /tmp

ARG JAR_FILE=./build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]