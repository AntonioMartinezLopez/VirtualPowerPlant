# get image for grade build
FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:8-jre-slim

#EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/ExternalClient.jar /app/ExternalClient.jar

ENTRYPOINT ["java", "-jar","/app/ExternalClient.jar"]