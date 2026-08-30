pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.dairy.foundation/releases")
    }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

//include(":FtcRobotController")
include(":TeamCode")
