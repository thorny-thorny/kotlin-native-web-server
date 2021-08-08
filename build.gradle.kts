plugins {
  kotlin("multiplatform") version "1.5.21"
}

repositories {
  mavenCentral()
}

kotlin {
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native") // TODO: this target
    else -> throw GradleException("Host OS \"$hostOs\" is not supported (list of supported: MacOS, Linux, Windows; 64bit only)")
  }

  nativeTarget.apply {
    binaries {
      executable {
        entryPoint = "main"
        runTask?.standardInput = System.`in`
      }
    }

    compilations["main"].cinterops {
      create("libmicrohttpd")
    }

    sourceSets {
      commonMain {
        dependencies {
          implementation(kotlin("stdlib-common"))
        }
      }
    }
  }
}
