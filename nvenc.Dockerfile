# Gradle Cache Dependencies Stage
## This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
## Gradle image erases GRADLE_USER_HOME each layer. So we need COPY GRADLE_USER_HOME.
## Refer https://stackoverflow.com/a/59022743
FROM gradle:jdk8 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
## Full build if there are any deps changes
RUN gradle shadowJar --parallel --no-daemon --quiet

# Gradle Build Stage
## This stage builds and generates fat jar.
FROM gradle:jdk8 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
## Stop printing Welcome
RUN gradle -version > /dev/null \
    && gradle shadowJar --parallel --no-daemon

# For mirakc-arib
FROM mirakc/mirakc:debian AS mirakc-image

# Final Stage
FROM slashnephy/dtv-ffmpeg-build:nvenc

## Add user
RUN adduser --disabled-password --gecos "" saya

## Install JRE 11
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-11-jre-headless \
    && apt-get clean \
    && rm -rf \
        /var/lib/apt/lists/*

## Copy mirakc-arib binary
COPY --from=mirakc-image /usr/local/bin/mirakc-arib /usr/local/bin/

COPY --from=build /app/build/libs/saya-all.jar /app/saya.jar
COPY docs/ /app/docs/

USER saya
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/saya.jar"]
