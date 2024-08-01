# Kotlin/Native web server

> [!WARNING]  
> This project is based on Kotlin 1.5 released in 2021, do not take it as an example of modern Kotlin/Native features

A simple proof of concept of Kotlin/Native app with a web server inside (C interop for [libmicrohttpd](https://www.gnu.org/software/libmicrohttpd/)). Libmicrohttpd is linked statically so the output executable file shouldn't require any external dependency. I've discovered more complex example of libmicrohttpd+Kotlin/Native project [here](https://github.com/Kotlin/kotlinconf-spinner/blob/master/httpserver/src/hostMain/kotlin/server/HttpServer.kt).

## Status

* MacOS: ✅
* Linux: ✅
* Windows: ✅
* Docker: ✅

## Command line options

* `-p <port>` specifies port number to listen, default: 8080
* `-d` runs program in daemon-like mode for docker where it gets terminated by POSIX signals instead of pressing enter, default: unset

## Docker

You can use docker to build and run an image with the web server automatically, see Dockerfile.

## Build requirements

* This is a gradle project with wrapper included, so you only need installed JRE for gradle to be able to run tasks
* Download, build and install [libmicrohttpd](https://www.gnu.org/software/libmicrohttpd/) (I've used MinGW64@[MSYS2](https://www.msys2.org/) for Windows build). I've used only `./configure; make; make install` without any flags or manual configuration.

## Build

After installing all requirements:

1. Open src/nativeInterop/libmicrohttpd.def
2. Look through your /usr and /opt dirs (or MSYS2 paths for Windows) for libmicrohttpd installation artifacts, make sure:
   * Path to folder containing microhttpd.h is specified at `compilerOpts`
   * Path to folder containing libmicrohttpd.a is specified at `libraryPaths`
3. `./gradlew linkReleaseExecutableNative`

## Run

After building run `./build/bin/native/releaseExecutable/kotlin-native-web-server.kexe` (or whatever executable file is inside). Output:
```
Server is listening port 8080
Press enter to stop
```
Open link [http://localhost:8080/any-url](http://localhost:8080/any-url) in browser:

![Browser screenshot](screenshot.png)
