FROM maven:3-openjdk-11-slim

VOLUME /opt/data-consumer-java
WORKDIR /opt/data-consumer-java

# mvn package must be run in the mounted volume first
CMD ["java",  "-jar", "/opt/data-consumer-java/target/data-consumer-java-0.0.1-jar-with-dependencies.jar"]
