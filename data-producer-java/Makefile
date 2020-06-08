.PHONY: build deploy

build:
	mvn com.coveo:fmt-maven-plugin:format
	mvn clean install -e -DskipTests
	
deploy: build
	docker-compose up --build   data-producer-java 