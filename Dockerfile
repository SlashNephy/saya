# Gradle Cache Dependencies Stage
## This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
## Gradle image erases GRADLE_USER_HOME each layer. So we need COPY GRADLE_USER_HOME.
## Refer https://stackoverflow.com/a/59022743
FROM --platform=$BUILDPLATFORM gradle:jdk11 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
## Full build if there are any deps changes
RUN gradle shadowJar --parallel --no-daemon --quiet

# Gradle Build Stage
## This stage builds and generates fat jar.
FROM --platform=$BUILDPLATFORM gradle:jdk11 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
## Stop printing Welcome
RUN gradle -version > /dev/null \
    && gradle shadowJar --parallel --no-daemon

# Final Stage
FROM --platform=$TARGETPLATFORM adoptopenjdk:11-jre-hotspot

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

## Add user
RUN adduser --disabled-password --gecos "" saya

COPY --from=build /app/build/libs/saya-all.jar /app/saya.jar
COPY docs/ /app/docs/

USER saya
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/saya.jar"]
