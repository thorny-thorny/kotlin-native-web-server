FROM debian:bookworm-20231120 AS build

RUN apt update && apt install build-essential openjdk-17-jre libtinfo5 -y
WORKDIR /build

# Build libmicrohttpd
ADD https://ftpmirror.gnu.org/libmicrohttpd/libmicrohttpd-1.0.1.tar.gz libmicrohttpd.tar.gz
RUN mkdir libmicrohttpd && tar -xzf libmicrohttpd.tar.gz -C libmicrohttpd --strip-components 1
WORKDIR libmicrohttpd
RUN ./configure && make && make install

# Build KNWS
WORKDIR /build/knws
COPY src src
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts .
RUN ./gradlew linkReleaseExecutableNative

# Lean image with build artifacts
FROM alpine:3.20.2
RUN apk update && apk add gcompat libgcc
RUN addgroup -S knwsgroup && adduser -S knwsuser -G knwsgroup
USER knwsuser
COPY --from=build /build/knws/build/bin/native/releaseExecutable/knws.kexe /knws/knws
EXPOSE 8080
ENTRYPOINT ["/knws/knws", "-d", "-p", "8080"]
