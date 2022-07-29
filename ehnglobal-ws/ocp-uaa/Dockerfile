FROM maven:3.8.2-jdk-8-slim as build
WORKDIR /workspace
COPY . .
RUN ./gradlew clean build -x test

FROM tomcat:8-jre8 as release
ENV CLOUD_FOUNDRY_CONFIG_PATH /java/OCP_PROPS/uaa
VOLUME /java
COPY --from=build /workspace/uaa/build/libs/cloudfoundry-identity-uaa-4.8.0-11.war /usr/local/tomcat/webapps/
ENV DEFAULT_PROGRAM_ARGS --spring.profiles.active=application-default,docker
