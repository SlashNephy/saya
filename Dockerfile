# Gradle Cache Dependencies Stage
# This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
# Gradle image erases GRADLE_USER_HOME each layer. So we need COPY GRADLE_USER_HOME.
# Refer https://stackoverflow.com/a/59022743
FROM gradle:jdk8 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
# Full build if there are any deps changes
RUN gradle shadowJar --parallel --no-daemon --quiet

# Gradle Build Stage
# This stage builds and generates fat jar.
FROM gradle:jdk8 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
# Stop printing Welcome
RUN gradle -version > /dev/null \
    && gradle shadowJar --parallel --no-daemon

# Final Stage
FROM openjdk:8-jre-alpine

## ffmpeg build Stage
## ffmpeg version must be <4.2 for subtitle support.
## Refer issue https://github.com/EMWUI/EDCB_Material_WebUI/issues/17
ARG FFMPEG_VERSION=4.1.6
ARG CPUCORE=4
RUN echo https://dl-cdn.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories \
    && apk add --update --no-cache --virtual .build-deps \
        build-base \
        curl \
        tar \
        coreutils \
        x264-dev \
        fdk-aac-dev \
    \
    # runtime
    && apk add --no-cache \
        x264 \
        x264-libs \
        fdk-aac \
    # build
    && mkdir /tmp/ffmpeg \
    && cd /tmp/ffmpeg \
    && curl -sLO https://ffmpeg.org/releases/ffmpeg-${FFMPEG_VERSION}.tar.bz2 \
    && tar -jx --strip-components=1 -f ffmpeg-${FFMPEG_VERSION}.tar.bz2 \
    && ./configure \
        --enable-small \
        --disable-debug \
        --disable-doc \
        --disable-ffplay \
        --disable-x86asm \
        # static build
        --disable-shared \
        --enable-static \
        --pkg-config-flags=--static \
        --extra-libs="-lpthread -lm" \
        # libx264
        --enable-libx264 \
        # aac
        --enable-libfdk-aac \
        --enable-gpl \
        --enable-nonfree \
    && make -j${CPUCORE} \
    && make install \
    && make distclean \
    \
    # cleaning
    && apk del --purge .build-deps \
    && rm -rf /tmp/ffmpeg

COPY --from=build /app/build/libs/saya-all.jar /app/saya.jar
COPY docs/ /app/docs/

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/saya.jar"]
