pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.dairy.foundation/releases")
    }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

//include(":FtcRobotController")
include(":TeamCode")
include(":HexLib")
