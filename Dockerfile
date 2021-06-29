# Dependencies
FROM maven:3-jdk-11 AS maven
WORKDIR /app
COPY pom.xml .
RUN mvn -e -B dependency:resolve

# Plugins
RUN mvn -e -B dependency:resolve-plugins

# Classes
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources
RUN mvn -e -B clean package -DskipTests -Dmaven.javadoc.skip=true

FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine

# Copy the jar and build image
FROM gcr.io/distroless/java-debian10:11
COPY --from=maven /app/target/*.jar /app/app.jar
WORKDIR /app
EXPOSE 8081
USER nonroot
ENTRYPOINT ["java","-jar","app.jar"]
