plugins {
  id("dev.frozenmilk.android-library") version "11.1.0-1.1.2"
}

android.namespace = "org.hexnibble.hexlib"

ftc {
  kotlin()
  sdk {
    compileOnly(RobotCore)
    compileOnly(Hardware)
  }
}

dependencies {
  implementation(libs.bundles.pedro)
  implementation(libs.ivy)
}