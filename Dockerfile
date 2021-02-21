# Gradle Cache Dependencies Stage
## This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
## Gradle image erases GRADLE_USER_HOME each layer. So we need COPY GRADLE_USER_HOME.
## Refer https://stackoverflow.com/a/59022743
FROM gradle:jdk8 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/graphql/ /app/src/main/graphql/
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
FROM mirakc/mirakc:alpine AS mirakc-image

# Final Stage
FROM slashnephy/dtv-ffmpeg-build:alpine

## Add user
RUN addgroup -S saya \
    && adduser -S saya -G saya

## Install JRE 11
RUN apk add --update --no-cache openjdk11-jre-headless

## Copy mirakc-arib binary
COPY --from=mirakc-image /usr/local/bin/mirakc-arib /usr/local/bin/
## Install gcc runtime
RUN apk add --update --no-cache libgcc libstdc++

COPY --from=build /app/build/libs/saya-all.jar /app/saya.jar
COPY docs/ /app/docs/

USER saya
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/saya.jar"]
