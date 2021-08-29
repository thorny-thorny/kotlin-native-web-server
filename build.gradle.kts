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
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS \"$hostOs\" is not supported (supported: MacOS, Linux, Windows; 64bit only)")
  }

  nativeTarget.apply {
    binaries {
      executable {
        entryPoint = "main"
        runTask?.standardInput = System.`in`
        if (isMingwX64) {
          linkerOpts("-lws2_32")
        }
      }
    }

    compilations["main"].cinterops {
      create("libmicrohttpd")
    }

    sourceSets {
      val commonMain by getting {
        dependencies {
          implementation(kotlin("stdlib-common"))
        }
      }

      val nixMain by creating {
      }

      val mingwMain by creating {
      }

      val nativeMain by getting {
        dependsOn(if (isMingwX64) mingwMain else nixMain)
      }
    }
  }
}
