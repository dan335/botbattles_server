FROM maven:3-jdk-12-alpine

# set timezone
RUN apk add --no-cache tzdata
RUN cp /usr/share/zoneinfo/GMT /etc/localtime

ADD ./java/arenaworker/pom.xml /arenaworker/java/arenaworker/
WORKDIR /arenaworker/java/arenaworker/
RUN mvn verify clean --fail-never

COPY . /arenaworker/

EXPOSE 3020
RUN ["mvn", "clean", "package"]
CMD ["java", "-XX:+UseContainerSupport", "-cp", "/arenaworker/java/arenaworker/target/arenaworker-1.0.jar", "arenaworker.App"]
