FROM maven:3-jdk-8-alpine

# set timezone
RUN apk add --no-cache tzdata
RUN cp /usr/share/zoneinfo/GMT /etc/localtime

COPY . /arenaworker/
WORKDIR /arenaworker/java/arenaworker/
EXPOSE 3020
RUN ["mvn", "clean", "package"]
CMD ["java", "-cp", "/arenaworker/java/arenaworker/target/arenaworker-1.0.jar", "arenaworker.App"]
