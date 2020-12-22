# Gradle Cache Dependencies Stage
# This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
# Refer https://qiita.com/tkrplus/items/044790b4054bf644890a
FROM gradle:6.7.1-jdk8 AS builder
WORKDIR /app
COPY *.gradle.kts gradle.properties /app/
RUN gradle build --quiet --parallel

# Gradle Build Stage
# This stage builds saya, and generates fat jar.
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel

# Final Stage
FROM openjdk:8-jre-alpine

## ffmpeg build
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

COPY --from=builder /app/build/libs/saya-all.jar /app/saya.jar
COPY resources/ /app/resources/

WORKDIR /app
ENTRYPOINT ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "/app/saya.jar"]
