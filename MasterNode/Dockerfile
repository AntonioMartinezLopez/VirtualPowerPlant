
# get image for grade build
FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:8-jre-slim

#EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/MasterNode.jar /app/MasterNode.jar

ENTRYPOINT ["java", "-jar","/app/MasterNode.jar"]



####################################
## use openjdk 11 as baseimage
#FROM openjdk:11.0.11-jre-slim
#
## copy the packaged jar file into our docker image
#COPY build/libs/MasterNode.jar /opt/MasterNode.jar
#
## set the startup command to execute the jar
#CMD ["java", "-jar", "/opt/MasterNode.jar"]