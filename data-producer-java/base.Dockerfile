FROM maven:3-openjdk-11-slim as base

WORKDIR /opt/data-producer-java

COPY pom.xml /opt/data-producer-java/

# TODO: Is it possible to not have the source in the base Docker image?
COPY src /opt/data-producer-java/src

RUN mvn -B -ntp package -DskipTests
