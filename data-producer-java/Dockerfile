FROM maven:3-openjdk-11-slim

VOLUME /opt/data-producer-java
WORKDIR /opt/data-producer-java

# mvn package must be run in the mounted volume first
CMD ["java",  "-jar", "/opt/data-producer-java/target/data-producer-java-0.0.1.jar"]
