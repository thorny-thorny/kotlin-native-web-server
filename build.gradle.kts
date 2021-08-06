plugins {
  kotlin("multiplatform") version "1.5.21"
}

repositories {
  mavenCentral()
}

kotlin {
  // TODO: linuxX64("native"), mingwX64("native")
  macosX64("native") {
    binaries {
      executable {
        entryPoint = "main"
        runTask?.standardInput = System.`in`
      }
    }

    compilations["main"].cinterops {
      create("libmicrohttpd")
    }
  }
}
