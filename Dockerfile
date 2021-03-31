FROM maven:3.6.3-jdk-11 AS maven

COPY pom.xml /tmp/

WORKDIR tmp
RUN mvn verify clean --fail-never

COPY src /tmp/src/

RUN mvn clean package

FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine
RUN mkdir /app

COPY --from=maven /tmp/target/*.jar /app/configurationmanager-6.0.0-SNAPSHOT.jar

WORKDIR /app/

ENTRYPOINT ["java","-jar","configurationmanager-6.0.0-SNAPSHOT.jar"]
