FROM openjdk:17

VOLUME /tmp
ARG JAR_FILE=/*.jar
COPY ./target/${JAR_FILE} finance-control.jar

ENTRYPOINT ["java", "-jar", "finance-control.jar"]