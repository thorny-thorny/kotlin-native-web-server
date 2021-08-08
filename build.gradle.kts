plugins {
  kotlin("multiplatform") version "1.5.21"
}

repositories {
  mavenCentral()
}

kotlin {
  val hostOs = System.getProperty("os.name")
  // TODO: this target
  // val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    // isMingwX64 -> mingwX64("native") 
    else -> throw GradleException("Host OS \"$hostOs\" is not supported (supported: MacOS, Linux; 64bit only)")
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
