plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

// Official FTC application configuration
apply(from = "../build.common.gradle")

// Official FTC SDK dependencies
apply(from = "../build.dependencies.gradle")

android {
  namespace = "org.firstinspires.ftc.teamcode"

  packaging {
    jniLibs {
      useLegacyPackaging = true
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(
      org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
    )
  }
}

dependencies {
  // Local FTC SDK module
  implementation(project(":FtcRobotController"))

  // BlazeFTC
  implementation(libs.blaze.ftc)
  implementation(libs.blaze.ftc.pedro)

  implementation(libs.sloth)

  implementation(libs.bundles.pedro)
//  implementation(libs.bundles.nextftc)
  implementation(libs.nextftc.robot)

  implementation(libs.panels)

  // Kotlin reflection
  implementation(libs.kotlin.reflect)
}