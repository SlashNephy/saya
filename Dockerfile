FROM gradle:6.7.1-jdk8 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
RUN mkdir -p /home/gradle/build_tmp
COPY build.gradle.kts settings.gradle.kts gradle.properties /home/gradle/build_tmp/
WORKDIR /home/gradle/build_tmp
RUN gradle clean build -i --stacktrace

FROM gradle:6.7.1-jdk8 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/build_home
WORKDIR /home/gradle/build_home
RUN gradle shadowJar -i --stacktrace

FROM openjdk:8-jre-alpine
RUN mkdir /app
WORKDIR /app
COPY --from=build /home/gradle/build_home/build/libs/saya-all.jar /app/saya.jar
COPY resources /app/resources
COPY channels.json /app/channels.json

ENTRYPOINT ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "/app/saya.jar"]
