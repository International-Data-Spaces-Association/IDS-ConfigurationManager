FROM maven:3-jdk-11 AS maven

COPY pom.xml /tmp/

WORKDIR tmp
RUN mvn verify clean --fail-never

COPY src /tmp/src/

RUN mvn clean package

FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine

LABEL org.opencontainers.image.source="https://github.com/International-Data-Spaces-Association/IDS-ConfigurationManager"

RUN mkdir /app

COPY --from=maven /tmp/target/*.jar /app/configurationmanager-7.1.0.jar

WORKDIR /app/

ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","configurationmanager-7.1.0.jar"]
