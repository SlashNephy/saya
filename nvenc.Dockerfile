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
FROM nvidia/cuda:10.0-devel-ubuntu18.04
ENV SAYA_HWACCEL nvenc
ENV NVIDIA_VISIBLE_DEVICES all
ENV NVIDIA_DRIVER_CAPABILITIES video,compute,utility

# Java 8
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-8-jre \
        language-pack-ja \
    && locale-gen ja_JP.UTF-8
ENV LANG ja_JP.UTF-8

## ffmpeg build Stage
## ffmpeg version must be <4.2 for subtitle support.
## Refer issue https://github.com/EMWUI/EDCB_Material_WebUI/issues/17
ARG FFMPEG_VERSION=4.1.6
ARG CPUCORE=4
RUN apt-get install -y --no-install-recommends \
        curl \
        tar \
        git \
        ca-certificates \
        build-essential \
        nasm \
        yasm \
        libx264-dev \
        libfdk-aac-dev \
    # nv-codec-headers
    && cd /tmp \
    && git clone https://github.com/FFmpeg/nv-codec-headers \
    && cd nv-codec-headers \
    && make -j${CPUCORE} \
    && make install \
    # build
    && mkdir /tmp/ffmpeg \
    && cd /tmp/ffmpeg \
    && curl -sLO https://ffmpeg.org/releases/ffmpeg-${FFMPEG_VERSION}.tar.bz2 \
    && tar -jx --strip-components=1 -f ffmpeg-${FFMPEG_VERSION}.tar.bz2 \
    && ./configure \
        --extra-libs="-lpthread -lm" \
        --enable-small \
        --disable-debug \
        --disable-doc \
        --disable-ffplay \
        --disable-x86asm \
        # libx264
        --enable-libx264 \
        # aac
        --enable-libfdk-aac \
        --enable-gpl \
        --enable-nonfree \
        # nvenc
        --enable-cuda \
        --enable-cuvid \
        --extra-cflags=-I/usr/local/cuda/include \
        --extra-ldflags=-L/usr/local/cuda/lib64 \
        --enable-nvenc \
    && make -j${CPUCORE} \
    && make install \
    && make distclean \
    && ffmpeg -buildconf \
    && ffmpeg -encoders \
    \
    # cleaning
    && apt-get remove --purge -y \
        curl \
        git \
        build-essential \
        nasm \
        yasm \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf \
        /var/lib/apt/lists/* \
        /tmp/ffmpeg \
        /tmp/nv-codec-headers

COPY --from=build /app/build/libs/saya-all.jar /app/saya.jar
COPY docs/ /app/docs/

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/saya.jar"]
